package com.vendelote.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.LiquidacionResumenDTO;
import com.vendelote.dto.LiquidacionSocioDTO;
import com.vendelote.dto.SaldoSocioDTO;
import com.vendelote.entity.Comision;
import com.vendelote.entity.ComisionReparto;
import com.vendelote.entity.Comprobante;
import com.vendelote.entity.ComprobantePago;
import com.vendelote.entity.LiquidacionSocio;
import com.vendelote.entity.PagoComision;
import com.vendelote.entity.Usuario;
import com.vendelote.enums.EstadoComision;
import com.vendelote.enums.RolUsuario;
import com.vendelote.enums.EstadoPago;
import com.vendelote.enums.TipoPago;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ComisionRepartoRepository;
import com.vendelote.repository.ComisionRepository;
import com.vendelote.repository.ComprobantePagoRepository;
import com.vendelote.repository.ComprobanteRepository;
import com.vendelote.repository.LiquidacionSocioRepository;
import com.vendelote.repository.PagoComisionRepository;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Liquidaciones a socios: calcula la parte bruta del socio sobre las comisiones
 * COBRADAS y le descuenta su proporción de IGV y detracción de las facturas.
 */
@Service
@RequiredArgsConstructor
public class LiquidacionSocioService {

    private final UsuarioRepository usuarioRepository;
    private final ComisionRepository comisionRepository;
    private final ComisionRepartoRepository repartoRepository;
    private final PagoComisionRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final LiquidacionSocioRepository liquidacionRepository;

    @Transactional(readOnly = true)
    public List<SaldoSocioDTO> saldos() {
        Integer idV = TenantContext.idVendedorRequerido();
        if (TenantContext.esAsociado()) {
            Integer propio = TenantContext.idUsuarioActual();
            LiquidacionResumenDTO r = calcular(propio, idV);
            return List.of(new SaldoSocioDTO(propio, r.getSocioNombre(),
                    r.getMontoBruto(), r.getMontoLiquidado(), r.getMontoPendiente()));
        }
        List<Usuario> socios = usuarioRepository.findByIdVendedorJefeOrderByNombresAsc(idV)
                .stream().filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO).toList();
        return socios.stream().map(s -> {
            LiquidacionResumenDTO r = calcular(s.getId(), idV);
            return new SaldoSocioDTO(s.getId(), r.getSocioNombre(),
                    r.getMontoBruto(), r.getMontoLiquidado(), r.getMontoPendiente());
        }).toList();
    }

    @Transactional(readOnly = true)
    public LiquidacionResumenDTO sugerir(Integer idSocio) {
        Integer idV = TenantContext.idVendedorRequerido();
        return calcular(idSocioEfectivo(idSocio), idV);
    }

    @Transactional(readOnly = true)
    public List<LiquidacionSocioDTO> listar(Integer idSocio) {
        Integer idV = TenantContext.idVendedorRequerido();
        Integer filtro = idSocioEfectivo(idSocio);
        List<LiquidacionSocio> lista = filtro != null
                ? liquidacionRepository.findByIdVendedorAndIdSocioOrderByFechaRegistroDesc(idV, filtro)
                : liquidacionRepository.findByIdVendedorOrderByFechaRegistroDesc(idV);
        Map<Integer, Usuario> socios = usuarioRepository.findByIdVendedorJefeOrderByNombresAsc(idV)
                .stream().filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                .collect(Collectors.toMap(Usuario::getId, Function.identity(), (a, b) -> a));
        return lista.stream().map(l -> toDTO(l, socios.get(l.getIdSocio()))).toList();
    }

    @Transactional
    public LiquidacionSocioDTO crear(LiquidacionSocioDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        if (dto.getIdSocio() == null) {
            throw new NegocioException("Debe indicar el socio");
        }
        Usuario socio = usuarioRepository.findByIdAndIdVendedorJefe(dto.getIdSocio(), idV)
                .filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                .orElseThrow(() -> new NegocioException("Vendedor socio no encontrado"));

        BigDecimal bruto = monto(dto.getMontoBruto());
        BigDecimal igv = monto(dto.getDescuentoIgv());
        BigDecimal detraccion = monto(dto.getDescuentoDetraccion());
        BigDecimal neto = bruto.subtract(igv).subtract(detraccion);
        if (neto.signum() < 0) {
            throw new NegocioException("Los descuentos no pueden superar el monto bruto");
        }

        LiquidacionSocio l = new LiquidacionSocio();
        l.setIdVendedor(idV);
        l.setIdSocio(socio.getId());
        l.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now());
        l.setMontoBruto(bruto);
        l.setDescuentoIgv(igv);
        l.setDescuentoDetraccion(detraccion);
        l.setMontoNeto(neto.setScale(2, RoundingMode.HALF_UP));
        l.setReferencia(limpiar(dto.getReferencia()));
        l.setNotas(limpiar(dto.getNotas()));
        return toDTO(liquidacionRepository.save(l), socio);
    }

    @Transactional
    public void eliminar(Integer id) {
        Integer idV = TenantContext.idVendedorRequerido();
        LiquidacionSocio l = liquidacionRepository.findById(id)
                .filter(x -> x.getIdVendedor().equals(idV))
                .orElseThrow(() -> new NegocioException("Liquidación no encontrada"));
        liquidacionRepository.delete(l);
    }

    // ── Cálculo ────────────────────────────────────────────────────────────────

    private LiquidacionResumenDTO calcular(Integer idSocio, Integer idV) {
        Usuario socio = usuarioRepository.findByIdAndIdVendedorJefe(idSocio, idV)
                .filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                .orElseThrow(() -> new NegocioException("Vendedor socio no encontrado"));

        List<Comision> comisiones = comisionRepository.findByIdVendedorOrderByIdDesc(idV);
        Map<Integer, ComisionReparto> repartoPorComision = repartoRepository
                .findByIdVendedor(idV).stream()
                .filter(r -> r.getIdSocio().equals(idSocio))
                .collect(Collectors.toMap(ComisionReparto::getIdComision, Function.identity(), (a, b) -> a));

        Map<Integer, PagoComision> pagosPorId = new HashMap<>();
        Map<Integer, BigDecimal> sharePorPago = new HashMap<>();
        BigDecimal bruto = BigDecimal.ZERO;

        for (Comision c : comisiones) {
            if (c.getEstado() == EstadoComision.ANULADA) {
                continue;
            }
            ComisionReparto reparto = repartoPorComision.get(c.getId());
            if (reparto == null) {
                continue;
            }
            List<PagoComision> pagos = pagoRepository
                    .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(c.getId(), idV);
            for (PagoComision p : pagos) {
                if (p.getEstado() != EstadoPago.PAGADO) {
                    continue;
                }
                BigDecimal share = shareSocio(reparto, c, p);
                if (share.signum() <= 0) {
                    continue;
                }
                pagosPorId.put(p.getId(), p);
                sharePorPago.put(p.getId(), share);
                bruto = bruto.add(share);
            }
        }

        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal detraccion = BigDecimal.ZERO;
        for (Comprobante comp : comprobanteRepository.findByIdVendedorOrderByFechaEmisionDesc(idV)) {
            if (comp.getSubtotal() == null || comp.getSubtotal().signum() <= 0) {
                continue;
            }
            BigDecimal socioSubtotal = BigDecimal.ZERO;
            for (ComprobantePago cp : comprobantePagoRepository
                    .findByIdComprobanteAndIdVendedor(comp.getId(), idV)) {
                BigDecimal share = sharePorPago.get(cp.getIdPago());
                if (share == null) {
                    continue;
                }
                PagoComision pago = pagosPorId.get(cp.getIdPago());
                BigDecimal pagoMonto = pago != null && pago.getMonto() != null
                        ? pago.getMonto() : BigDecimal.ZERO;
                if (pagoMonto.signum() <= 0) {
                    continue;
                }
                BigDecimal aplicado = cp.getMontoAplicado() != null
                        ? cp.getMontoAplicado() : pagoMonto;
                socioSubtotal = socioSubtotal.add(
                        share.multiply(aplicado).divide(pagoMonto, 2, RoundingMode.HALF_UP));
            }
            if (socioSubtotal.signum() <= 0) {
                continue;
            }
            BigDecimal ratio = socioSubtotal.divide(comp.getSubtotal(), 6, RoundingMode.HALF_UP);
            if (ratio.compareTo(BigDecimal.ONE) > 0) {
                ratio = BigDecimal.ONE;
            }
            if (comp.getIgv() != null) {
                igv = igv.add(comp.getIgv().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            }
            if (comp.getMontoDetraccion() != null) {
                detraccion = detraccion.add(
                        comp.getMontoDetraccion().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal neto = bruto.subtract(igv).subtract(detraccion);
        BigDecimal liquidado = liquidacionRepository
                .findByIdVendedorAndIdSocioOrderByFechaRegistroDesc(idV, idSocio).stream()
                .map(LiquidacionSocio::getMontoBruto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendiente = bruto.subtract(liquidado);

        return new LiquidacionResumenDTO(socio.getId(), nombre(socio),
                escalar(bruto), escalar(igv), escalar(detraccion), escalar(neto),
                escalar(liquidado), escalar(pendiente));
    }

    /** Parte del socio en un pago; según el modo (monto fijo o porcentaje). */
    private BigDecimal shareSocio(ComisionReparto r, Comision c, PagoComision p) {
        boolean esInicial = p.getTipo() == TipoPago.ANTICIPO || p.getTipo() == TipoPago.TOTAL;
        if (esInicial) {
            if (!r.isParticipaInicial()) {
                return BigDecimal.ZERO;
            }
            if (r.getMontoInicial() != null) {
                return r.getMontoInicial();
            }
            BigDecimal base = p.getTipo() == TipoPago.TOTAL ? c.getMontoTotal() : c.getMontoAnticipo();
            return pct(r.getPorcentajeInicial()).multiply(base == null ? BigDecimal.ZERO : base)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (!r.isParticipaCuotas()) {
            return BigDecimal.ZERO;
        }
        if (r.getMontoCuotaSocio() != null) {
            return r.getMontoCuotaSocio();
        }
        return pct(r.getPorcentajeCuotas())
                .multiply(p.getMonto() == null ? BigDecimal.ZERO : p.getMonto())
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** El vendedor socio solo puede consultar su propia liquidación. */
    private Integer idSocioEfectivo(Integer solicitado) {
        if (TenantContext.esAsociado()) {
            return TenantContext.idUsuarioActual();
        }
        return solicitado;
    }

    private BigDecimal pct(BigDecimal fraccion) {
        return fraccion == null ? BigDecimal.ZERO : fraccion;
    }

    private BigDecimal monto(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor.signum() < 0) {
            throw new NegocioException("Los montos de la liquidación no pueden ser negativos");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal escalar(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String nombre(Usuario s) {
        return (s.getNombres() + (s.getApellidos() != null ? " " + s.getApellidos() : "")).trim();
    }

    private LiquidacionSocioDTO toDTO(LiquidacionSocio l, Usuario socio) {
        LiquidacionSocioDTO dto = new LiquidacionSocioDTO();
        dto.setId(l.getId());
        dto.setIdSocio(l.getIdSocio());
        dto.setSocioNombre(socio != null ? nombre(socio) : "Socio");
        dto.setFecha(l.getFecha());
        dto.setMontoBruto(l.getMontoBruto());
        dto.setDescuentoIgv(l.getDescuentoIgv());
        dto.setDescuentoDetraccion(l.getDescuentoDetraccion());
        dto.setMontoNeto(l.getMontoNeto());
        dto.setReferencia(l.getReferencia());
        dto.setNotas(l.getNotas());
        dto.setFechaRegistro(l.getFechaRegistro());
        return dto;
    }
}
