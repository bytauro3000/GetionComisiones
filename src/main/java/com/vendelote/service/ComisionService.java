package com.vendelote.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.CobrarPagosDTO;
import com.vendelote.dto.ComisionListadoDTO;
import com.vendelote.dto.ComisionRepartoDTO;
import com.vendelote.dto.ComisionRepartoSocioDTO;
import com.vendelote.dto.PagoComisionDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.entity.Comision;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Lote;
import com.vendelote.entity.PagoComision;
import com.vendelote.entity.Usuario;
import com.vendelote.entity.Venta;
import com.vendelote.entity.VentaCliente;
import com.vendelote.enums.EstadoComision;
import com.vendelote.enums.EstadoPago;
import com.vendelote.enums.FormaPago;
import com.vendelote.enums.ModalidadVenta;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.ComisionRepository;
import com.vendelote.repository.ComprobantePagoRepository;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.PagoComisionRepository;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.repository.VentaRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Comisiones y sus pagos: cobro (1 o varios pagos a la vez), reversión y
 * recálculo de estado (PENDIENTE → PAGADA_PARCIAL → PAGADA).
 */
@Service
@RequiredArgsConstructor
public class ComisionService {

    private final ComisionRepository comisionRepository;
    private final PagoComisionRepository pagoRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final VentaRepository ventaRepository;
    private final LoteRepository loteRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final VentaService ventaService;
    private final ComisionRepartoService repartoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ComisionListadoDTO> listar(String estado) {
        Integer idV = TenantContext.idVendedorRequerido();
        Integer idAsociadoActual = TenantContext.esAsociado()
                ? TenantContext.idUsuarioActual() : null;
        List<Comision> comisiones = comisionRepository.findByIdVendedorOrderByIdDesc(idV).stream()
                .filter(c -> idAsociadoActual == null
                        || idAsociadoActual.equals(c.getIdVendedorAsociado()))
                .toList();
        if (estado != null && !estado.isBlank()) {
            EstadoComision filtro;
            try {
                filtro = EstadoComision.valueOf(estado.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (Exception e) {
                throw new NegocioException("Estado no válido: use PENDIENTE, PAGADA_PARCIAL, PAGADA o ANULADA");
            }
            comisiones = comisiones.stream().filter(c -> c.getEstado() == filtro).toList();
        }
        if (comisiones.isEmpty()) {
            return List.of();
        }

        Map<Integer, Lote> lotes = comisiones.stream()
                .map(Comision::getIdLote).distinct()
                .map(id -> loteRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(l -> l != null)
                .collect(Collectors.toMap(Lote::getId, l -> l, (a, b) -> a));
        Map<Integer, Empresa> empresas = comisiones.stream()
                .map(Comision::getIdEmpresa).distinct()
                .map(id -> empresaRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(e -> e != null)
                .collect(Collectors.toMap(Empresa::getId, e -> e, (a, b) -> a));
        Map<Integer, Venta> ventas = comisiones.stream()
                .map(Comision::getIdVenta).distinct()
                .map(id -> ventaRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(v -> v != null)
                .collect(Collectors.toMap(Venta::getId, v -> v, (a, b) -> a));
        Map<Integer, Cliente> clientes = ventas.values().stream()
                .flatMap(v -> v.getClientes().stream())
                .map(VentaCliente::getIdCliente).distinct()
                .map(id -> clienteRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toMap(Cliente::getId, c -> c, (a, b) -> a));
        Map<Integer, String> nombresAsociados = usuarioRepository.findAllById(
                        comisiones.stream().map(Comision::getIdVendedorAsociado)
                                .filter(java.util.Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(
                        Usuario::getId,
                        u -> (u.getNombres() + " "
                                + (u.getApellidos() == null ? "" : u.getApellidos())).trim()));

        return comisiones.stream().map(c -> {
            List<PagoComision> pagos = pagoRepository
                    .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(c.getId(), idV);
            long pendientes = pagos.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).count();
            BigDecimal montoPendiente = pagos.stream()
                    .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                    .map(PagoComision::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Lote lote = lotes.get(c.getIdLote());
            Empresa empresa = empresas.get(c.getIdEmpresa());
            Venta venta = ventas.get(c.getIdVenta());
            String nombreCliente = "-";
            if (venta != null) {
                String unidos = venta.getClientes().stream()
                        .map(vc -> clientes.get(vc.getIdCliente()))
                        .filter(c2 -> c2 != null)
                        .map(c2 -> (c2.getNombres()
                                + (c2.getApellidos() != null ? " " + c2.getApellidos() : "")).trim())
                        .collect(Collectors.joining(", "));
                if (!unidos.isBlank()) {
                    nombreCliente = unidos;
                }
            }

            BigDecimal montoAsociado = BigDecimal.ZERO.setScale(2);
            BigDecimal montoJefe = c.getMontoTotal();
            if (c.getIdVendedorAsociado() != null) {
                montoAsociado = c.getMontoTotal()
                        .multiply(new BigDecimal("0.50"))
                        .setScale(2, RoundingMode.HALF_UP);
                montoJefe = c.getMontoTotal().subtract(montoAsociado);
            }

            return new ComisionListadoDTO(
                    c.getId(), c.getIdVenta(), c.getEstado().name(),
                    c.getMontoTotal(), c.getMontoPagado(), c.getMontoSaldo(), c.getMontoAnticipo(),
                    c.getPorcentajeAnticipo(), c.getMontoCuota(), c.getCantidadCuotas(),
                    c.getIdLote(),
                    lote != null ? lote.getProyecto() + " Mz." + lote.getManzana() + " Lt." + lote.getNumero() : "-",
                    c.getIdEmpresa(), empresa != null ? empresa.getRazonSocial() : "-",
                    nombreCliente,
                    c.getIdVendedorAsociado(), nombresAsociados.get(c.getIdVendedorAsociado()),
                    montoJefe, montoAsociado,
                    (int) pendientes, montoPendiente,
                    repartoService.listar(c.getId(), idV),
                    pagos.stream().map(ventaService::toPagoDTO).toList());
        }).toList();
    }

    @Transactional(readOnly = true)
    public ComisionListadoDTO obtener(Integer id) {
        Integer idV = TenantContext.idVendedorRequerido();
        Comision comision = buscar(id);
        return listar(null).stream()
                .filter(c -> c.getId().equals(comision.getId()))
                .findFirst()
                .orElseThrow(() -> new NegocioException("Comisión no encontrada"));
    }

    /**
     * Marca como PAGADO(S) los pagos seleccionados de la comisión y recalcula
     * su estado. Permite cobrar de a uno o varios (ej: la empresa avisa "pago
     * de USD 1500 por estos lotes" y se marcan las cuotas que cubre).
     */
    @Transactional
    public ComisionListadoDTO cobrar(Integer idComision, CobrarPagosDTO dto) {
        Comision comision = buscar(idComision);
        if (comision.getEstado() == EstadoComision.ANULADA) {
            throw new NegocioException("La comisión está anulada");
        }
        FormaPago formaPago = formaPagoDesde(dto.getFormaPago());
        if (dto.getFechaPago().isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha de pago no puede ser futura");
        }

        List<PagoComision> pagosComision = pagoRepository
                .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(
                        comision.getId(), comision.getIdVendedor());

        for (Integer idPago : dto.getPagos()) {
            PagoComision pago = pagosComision.stream()
                    .filter(p -> p.getId().equals(idPago))
                    .findFirst()
                    .orElseThrow(() -> new NegocioException("Pago no pertenece a esta comisión"));
            if (pago.getEstado() == EstadoPago.PAGADO) {
                throw new NegocioException("El pago #" + pago.getId() + " ya está cobrado");
            }
            if (pago.getEstado() == EstadoPago.ANULADO) {
                throw new NegocioException("El pago #" + pago.getId() + " está anulado");
            }
            pago.setEstado(EstadoPago.PAGADO);
            pago.setFechaPago(dto.getFechaPago());
            pago.setFormaPago(formaPago);
            pago.setReferencia(dto.getReferencia());
            pago.setNotas(dto.getNotas());
            pagoRepository.save(pago);
        }

        recalcularEstado(comision);
        return obtener(comision.getId());
    }

    /** Revierte un cobro (si la factura aún no lo cubre) y recalcula. */
    @Transactional
    public ComisionListadoDTO revertirCobro(Integer idComision, Integer idPago) {
        Comision comision = buscar(idComision);
        PagoComision pago = pagoRepository.findByIdAndIdVendedor(idPago, comision.getIdVendedor())
                .orElseThrow(() -> new NegocioException("Pago no encontrado"));
        if (!pago.getIdComision().equals(comision.getId())) {
            throw new NegocioException("El pago no pertenece a esta comisión");
        }
        if (pago.getEstado() != EstadoPago.PAGADO) {
            throw new NegocioException("El pago no está cobrado");
        }
        if (comprobantePagoRepository.existsByIdPagoAndIdVendedor(pago.getId(), pago.getIdVendedor())) {
            throw new NegocioException(
                    "No se puede revertir: el pago ya está incluido en un comprobante emitido");
        }
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaPago(null);
        pago.setFormaPago(null);
        pago.setReferencia(null);
        pagoRepository.save(pago);

        recalcularEstado(comision);
        return obtener(comision.getId());
    }

    @Transactional(readOnly = true)
    public Comision buscar(Integer id) {
        Comision comision = comisionRepository.findByIdAndIdVendedor(
                        id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Comisión no encontrada"));
        if (TenantContext.esAsociado()
                && !TenantContext.idUsuarioActual().equals(comision.getIdVendedorAsociado())) {
            throw new NegocioException("Comisión no encontrada");
        }
        return comision;
    }

    @Transactional(readOnly = true)
    public List<ComisionRepartoDTO> obtenerRepartos(Integer idComision) {
        Comision comision = buscar(idComision);
        return repartoService.listar(comision.getId(), comision.getIdVendedor());
    }

    /** Reemplaza el reparto de socios de una comisión existente. */
    @Transactional
    public ComisionListadoDTO actualizarReparto(Integer idComision,
                                                List<ComisionRepartoSocioDTO> socios) {
        Comision comision = buscar(idComision);
        if (comision.getEstado() == EstadoComision.ANULADA) {
            throw new NegocioException("La comisión está anulada");
        }
        Venta venta = ventaRepository
                .findByIdAndIdVendedor(comision.getIdVenta(), comision.getIdVendedor())
                .orElse(null);
        BigDecimal inicialTotal = (venta != null && venta.getModalidad() == ModalidadVenta.CONTADO)
                ? comision.getMontoTotal() : comision.getMontoAnticipo();
        BigDecimal cuotaBase = comision.getCantidadCuotas() != null
                && comision.getCantidadCuotas() > 0 ? comision.getMontoCuota() : null;
        repartoService.reemplazar(comision, socios, inicialTotal, cuotaBase);
        return obtener(idComision);
    }

    // ── Internos ───────────────────────────────────────────────────────────────

    private void recalcularEstado(Comision comision) {
        BigDecimal pagado = pagoRepository
                .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(
                        comision.getId(), comision.getIdVendedor())
                .stream()
                .filter(p -> p.getEstado() == EstadoPago.PAGADO)
                .map(PagoComision::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        comision.setMontoPagado(pagado);
        if (pagado.compareTo(comision.getMontoTotal()) >= 0) {
            comision.setEstado(EstadoComision.PAGADA);
        } else if (pagado.signum() > 0) {
            comision.setEstado(EstadoComision.PAGADA_PARCIAL);
        } else {
            comision.setEstado(EstadoComision.PENDIENTE);
        }
        comisionRepository.save(comision);
    }

    private FormaPago formaPagoDesde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return FormaPago.valueOf(valor.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Forma de pago no válida: use TRANSFERENCIA, DEPOSITO, EFECTIVO, YAPE, PLIN u OTRO");
        }
    }
}
