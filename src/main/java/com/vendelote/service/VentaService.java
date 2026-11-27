package com.vendelote.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ComisionResumenDTO;
import com.vendelote.dto.PagoComisionDTO;
import com.vendelote.dto.VentaCrearDTO;
import com.vendelote.dto.VentaDetalleDTO;
import com.vendelote.dto.VentaListadoDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.entity.Comision;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Lote;
import com.vendelote.entity.ParametroComision;
import com.vendelote.entity.PagoComision;
import com.vendelote.entity.Usuario;
import com.vendelote.entity.Venta;
import com.vendelote.entity.VentaCliente;
import com.vendelote.entity.VentaLote;
import com.vendelote.enums.EstadoComision;
import com.vendelote.enums.EstadoLote;
import com.vendelote.enums.EstadoPago;
import com.vendelote.enums.EstadoVenta;
import com.vendelote.enums.ModalidadVenta;
import com.vendelote.enums.TipoPago;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.ComisionRepository;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.PagoComisionRepository;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.repository.VentaClienteRepository;
import com.vendelote.repository.VentaLoteRepository;
import com.vendelote.repository.VentaRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Ventas AGRUPADAS (uno o varios lotes + uno o varios clientes en la MISMA
 * venta) + generación automática de comisión y plan de pagos.
 *
 * Reglas acordadas (SaaS, todo en USD):
 * - precioVenta = TOTAL de la venta (suma de precios de lista de los lotes,
 *   editable por si hay descuento).
 * - CONTADO: comisión total de una vez (pago tipo TOTAL en la fecha de venta);
 *   puede ser inferior al 3% → porcentaje_comision_contado o comisionManual.
 * - FINANCIADO: comisión = 3% × precio; anticipo = anticipoPorcentaje (30%)
 *   × INICIAL que pagó el cliente (o monto fijo si está configurado); saldo
 *   en cuotas de $50 desde fechaVenta + meses_espera (8). La última cuota
 *   absorbe el residuo.
 * - Si la empresa paga el anticipo días después, solo cambia la fechaPago
 *   real: la programación y el conteo de 8 meses no se mueven.
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final VentaLoteRepository ventaLoteRepository;
    private final VentaClienteRepository ventaClienteRepository;
    private final ComisionRepository comisionRepository;
    private final PagoComisionRepository pagoRepository;
    private final LoteRepository loteRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ParametroComisionService parametroService;
    private final LoteService loteService;
    private final ClienteService clienteService;
    private final ComisionRepartoService repartoService;
    private final AsociadoService asociadoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<VentaListadoDTO> listar() {
        Integer idV = TenantContext.idVendedorRequerido();
        Integer idAsociadoActual = TenantContext.esAsociado()
                ? TenantContext.idUsuarioActual() : null;
        List<Venta> ventas = ventaRepository.findByIdVendedorOrderByFechaVentaDesc(idV).stream()
                .filter(v -> idAsociadoActual == null
                        || idAsociadoActual.equals(v.getIdVendedorAsociado()))
                .toList();
        if (ventas.isEmpty()) {
            return List.of();
        }

        List<Integer> idsLotes = ventas.stream().flatMap(v -> v.getLotes().stream())
                .map(VentaLote::getIdLote).distinct().toList();
        List<Integer> idsClientes = ventas.stream().flatMap(v -> v.getClientes().stream())
                .map(VentaCliente::getIdCliente).distinct().toList();

        Map<Integer, Lote> lotes = porId(loteRepository.findAllById(idsLotes), Lote::getId);
        Map<Integer, Cliente> clientes = porId(clienteRepository.findAllById(idsClientes), Cliente::getId);
        Map<Integer, Empresa> empresas = porId(empresaRepository.findAllById(ids(ventas, Venta::getIdEmpresa)), Empresa::getId);
        Map<Integer, Comision> comisionesPorVenta = comisionRepository.findByIdVendedorOrderByIdDesc(idV).stream()
                .collect(Collectors.toMap(Comision::getIdVenta, c -> c, (a, b) -> a));
        Map<Integer, String> nombresAsociados = usuarioRepository.findAllById(
                        ventas.stream().map(Venta::getIdVendedorAsociado)
                                .filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Usuario::getId,
                        u -> (u.getNombres() + " " + (u.getApellidos() == null ? "" : u.getApellidos())).trim()));

        return ventas.stream().map(v -> {
            List<Lote> lotesVenta = v.getLotes().stream()
                    .map(vl -> lotes.get(vl.getIdLote()))
                    .filter(Objects::nonNull).toList();
            List<Cliente> clientesVenta = v.getClientes().stream()
                    .map(vc -> clientes.get(vc.getIdCliente()))
                    .filter(Objects::nonNull).toList();
            Empresa empresa = empresas.get(v.getIdEmpresa());
            Comision comision = comisionesPorVenta.get(v.getId());
            return new VentaListadoDTO(
                    v.getId(), v.getFechaVenta(), v.getModalidad().name(), v.getEstado().name(),
                    v.getPrecioVenta(), v.getMontoInicial(),
                    etiquetaLotes(lotesVenta), nombresClientes(clientesVenta),
                    v.getIdEmpresa(), empresa != null ? empresa.getRazonSocial() : "-",
                    v.getIdVendedorAsociado(), nombresAsociados.get(v.getIdVendedorAsociado()),
                    comision != null ? comision.getMontoTotal() : null,
                    comision != null ? comision.getMontoPagado() : null,
                    comision != null ? comision.getEstado().name() : null,
                    v.getFechaRegistro());
        }).toList();
    }

    @Transactional(readOnly = true)
    public VentaDetalleDTO obtener(Integer id) {
        return detalle(id);
    }

    /**
     * Registra la venta AGRUPADA (varios lotes + varios clientes en la misma
     * venta) y genera en la misma transacción la comisión con su plan de
     * pagos. Marca TODOS los lotes como VENDIDOS.
     */
    @Transactional
    public VentaDetalleDTO crear(VentaCrearDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();

        List<Integer> idsLotes = dto.getIdsLotes().stream().distinct().toList();
        List<Integer> idsClientes = dto.getIdsClientes().stream().distinct().toList();

        List<Lote> lotes = idsLotes.stream().map(loteService::buscar).toList();

        // La comisión se resuelve con el parámetro de la empresa: todos los
        // lotes deben ser de la MISMA empresa.
        if (lotes.stream().map(Lote::getIdEmpresa).distinct().count() > 1) {
            throw new NegocioException("Todos los lotes deben pertenecer a la misma empresa");
        }

        for (Lote lote : lotes) {
            if (lote.getEstado() == EstadoLote.VENDIDO) {
                throw new NegocioException("El lote Mz." + lote.getManzana()
                        + " Lt." + lote.getNumero() + " ya está vendido");
            }
            if (ventaLoteRepository.existsByVenta_IdVendedorAndIdLoteAndVenta_Estado(
                    idV, lote.getId(), EstadoVenta.ACTIVA)) {
                throw new NegocioException("El lote Mz." + lote.getManzana()
                        + " Lt." + lote.getNumero() + " ya tiene una venta activa");
            }
            // Separación AJENA vigente (la hizo otro vendedor): no se puede vender.
            // Las separaciones propias sí permiten cerrar la venta dentro del plazo.
            if (lote.isSeparacionAjena() && lote.getFechaVinceSeparacion() != null
                    && !lote.getFechaVinceSeparacion().isBefore(LocalDate.now())) {
                throw new NegocioException("El lote Mz." + lote.getManzana()
                        + " Lt." + lote.getNumero() + " está separado por otro vendedor hasta el "
                        + lote.getFechaVinceSeparacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        List<Cliente> clientes = idsClientes.stream().map(clienteService::buscar).toList();

        if (dto.getFechaVenta().isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha de venta no puede ser futura");
        }

        ModalidadVenta modalidad = modalidadDesde(dto.getModalidad());
        BigDecimal inicial;
        if (modalidad == ModalidadVenta.CONTADO) {
            inicial = dto.getPrecioVenta();
        } else {
            inicial = dto.getMontoInicial();
            if (inicial == null || inicial.signum() <= 0) {
                throw new NegocioException("En FINANCIADO la inicial debe ser mayor a 0");
            }
            if (inicial.compareTo(dto.getPrecioVenta()) >= 0) {
                throw new NegocioException("La inicial debe ser menor al precio de venta (use CONTADO si se paga todo)");
            }
        }
        if (dto.getComisionManual() != null && dto.getComisionManual().signum() < 0) {
            throw new NegocioException("La comisión manual no puede ser negativa");
        }

        ParametroComision parametro = parametroService.resolver(lotes.get(0).getIdEmpresa());

        Venta venta = new Venta();
        venta.setIdVendedor(idV);
        venta.setIdVendedorAsociado(
                asociadoService.resolverAsociado(dto.getIdVendedorAsociado()));
        venta.setIdEmpresa(lotes.get(0).getIdEmpresa());
        venta.setFechaVenta(dto.getFechaVenta());
        venta.setPrecioVenta(dto.getPrecioVenta());
        venta.setMontoInicial(inicial);
        venta.setModalidad(modalidad);
        venta.setComisionManual(dto.getComisionManual());
        venta.setNotas(dto.getNotas());
        if (modalidad == ModalidadVenta.FINANCIADO) {
            venta.setFechaInicioCuotas(dto.getFechaVenta().plusMonths(parametro.getMesesEspera()));
        }

        for (Lote lote : lotes) {
            VentaLote ventaLote = new VentaLote();
            ventaLote.setVenta(venta);
            ventaLote.setIdLote(lote.getId());
            venta.getLotes().add(ventaLote);
        }
        for (Cliente cliente : clientes) {
            VentaCliente ventaCliente = new VentaCliente();
            ventaCliente.setVenta(venta);
            ventaCliente.setIdCliente(cliente.getId());
            venta.getClientes().add(ventaCliente);
        }

        Venta guardada = ventaRepository.save(venta);

        generarComisionConPagos(guardada, parametro, lotes.get(0).getId(), dto);

        for (Lote lote : lotes) {
            lote.setEstado(EstadoLote.VENDIDO);
            loteRepository.save(lote);
        }

        return detalle(guardada.getId());
    }

    /** Anula la venta (solo si la comisión no tiene pagos) y libera el lote. */
    @Transactional
    public VentaDetalleDTO anular(Integer id) {
        Venta venta = buscar(id);
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new NegocioException("La venta ya está anulada");
        }

        Comision comision = comisionRepository.findByIdVentaAndIdVendedor(venta.getId(), venta.getIdVendedor())
                .orElse(null);
        if (comision != null) {
            if (comision.getMontoPagado().signum() > 0) {
                throw new NegocioException(
                        "No se puede anular: la comisión ya tiene pagos registrados (regularice primero)");
            }
            comision.setEstado(EstadoComision.ANULADA);
            comisionRepository.save(comision);
            for (PagoComision pago : pagoRepository
                    .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(
                            comision.getId(), comision.getIdVendedor())) {
                if (pago.getEstado() == EstadoPago.PENDIENTE) {
                    pago.setEstado(EstadoPago.ANULADO);
                    pagoRepository.save(pago);
                }
            }
        }

        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepository.save(venta);

        // Libera TODOS los lotes de la venta agrupada
        for (Integer idLote : venta.getLotes().stream().map(VentaLote::getIdLote).toList()) {
            Lote lote = loteRepository.findByIdAndIdVendedor(idLote, venta.getIdVendedor()).orElse(null);
            if (lote != null && lote.getEstado() == EstadoLote.VENDIDO) {
                lote.setEstado(EstadoLote.DISPONIBLE);
                loteRepository.save(lote);
            }
        }
        return detalle(venta.getId());
    }

    @Transactional(readOnly = true)
    public Venta buscar(Integer id) {
        Venta venta = ventaRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Venta no encontrada"));
        if (TenantContext.esAsociado()
                && !TenantContext.idUsuarioActual().equals(venta.getIdVendedorAsociado())) {
            throw new NegocioException("Venta no encontrada");
        }
        return venta;
    }

    // ── Generación de comisión + pagos ─────────────────────────────────────────

    private void generarComisionConPagos(Venta venta, ParametroComision parametro,
                                         Integer idPrimerLote, VentaCrearDTO dto) {
        boolean contado = venta.getModalidad() == ModalidadVenta.CONTADO;
        BigDecimal porcentaje = contado
                ? parametro.getPorcentajeComisionContado()
                : parametro.getPorcentajeComision();

        BigDecimal total;
        if (venta.getComisionManual() != null) {
            total = venta.getComisionManual();
        } else {
            total = venta.getPrecioVenta().multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        }
        if (total.signum() < 0) {
            throw new NegocioException("La comisión no puede ser negativa");
        }

        // Monto de cada letra (override por venta o parámetro).
        BigDecimal montoCuota = dto.getMontoCuota() != null
                ? dto.getMontoCuota()
                : parametro.getMontoCuota();

        // % del inicial del cliente aplicado al anticipo (override o parámetro).
        BigDecimal porcentajeAnticipo = contado ? null
                : (parametro.getAnticipoMontoFijo() != null ? null
                        : (dto.getPorcentajeAnticipo() != null
                                ? dto.getPorcentajeAnticipo()
                                : parametro.getAnticipoPorcentaje()));

        Comision comision = new Comision();
        comision.setIdVendedor(venta.getIdVendedor());
        comision.setIdVendedorAsociado(venta.getIdVendedorAsociado());
        comision.setIdVenta(venta.getId());
        comision.setIdEmpresa(venta.getIdEmpresa());
        // Es UNA comisión por venta agrupada; se referencia al primer lote
        comision.setIdLote(idPrimerLote);
        comision.setPorcentajeAplicado(venta.getComisionManual() == null ? porcentaje : null);
        comision.setMontoTotal(total);
        comision.setMontoCuota(montoCuota);
        comision.setPorcentajeAnticipo(porcentajeAnticipo);
        comision.setFechaInicioCuotas(contado ? venta.getFechaVenta() : venta.getFechaInicioCuotas());

        List<PagoComision> pagos = new ArrayList<>();
        BigDecimal inicialTotal;

        if (contado) {
            comision.setMontoAnticipo(BigDecimal.ZERO);
            comision.setMontoSaldo(BigDecimal.ZERO);
            comision.setCantidadCuotas(0);
            comision.setEstado(total.signum() == 0 ? EstadoComision.PAGADA : EstadoComision.PENDIENTE);
            inicialTotal = total;
            if (total.signum() > 0) {
                pagos.add(nuevoPago(venta, TipoPago.TOTAL, total, venta.getFechaVenta(), null));
            }
        } else {
            BigDecimal anticipo = calcularAnticipo(venta, parametro, total, dto.getPorcentajeAnticipo());
            BigDecimal saldo = total.subtract(anticipo);
            comision.setMontoAnticipo(anticipo);
            comision.setMontoSaldo(saldo);
            inicialTotal = anticipo;
            // Nace PENDIENTE: ningún pago está cobrado aún (el anticipo se cobra
            // después; el estado se recalcula en cada cobro/revertir).
            comision.setEstado(total.signum() == 0
                    ? EstadoComision.PAGADA : EstadoComision.PENDIENTE);

            if (anticipo.signum() > 0) {
                pagos.add(nuevoPago(venta, TipoPago.ANTICIPO, anticipo, venta.getFechaVenta(), null));
            }
            if (saldo.signum() > 0) {
                if (montoCuota.signum() <= 0) {
                    throw new NegocioException("El monto de cuota debe ser mayor a 0");
                }
                int cantidad = saldo.divide(montoCuota, 0, RoundingMode.CEILING).intValue();
                comision.setCantidadCuotas(cantidad);
                LocalDate inicio = venta.getFechaInicioCuotas();
                for (int i = 1; i <= cantidad; i++) {
                    BigDecimal monto = (i < cantidad)
                            ? montoCuota
                            : saldo.subtract(montoCuota.multiply(BigDecimal.valueOf(cantidad - 1)));
                    pagos.add(nuevoPago(venta, TipoPago.CUOTA, monto, inicio.plusMonths(i - 1), i));
                }
            } else {
                comision.setCantidadCuotas(0);
            }
        }

        Comision guardada = comisionRepository.save(comision);
        for (PagoComision pago : pagos) {
            pago.setIdComision(guardada.getId());
            pagoRepository.save(pago);
        }

        // Reparto con socios (si se informó). En CONTADO el "inicial" es el TOTAL.
        BigDecimal cuotaBase = guardada.getCantidadCuotas() != null && guardada.getCantidadCuotas() > 0
                ? montoCuota : null;
        repartoService.reemplazar(guardada, dto.getSocios(), inicialTotal, cuotaBase);
    }

    /**
     * Anticipo de la comisión en FINANCIADO: % (override o parámetro) ×
     * INICIAL que pagó el cliente, con tope en la comisión total.
     * (Antes se calculaba sobre la comisión total; el negocio lo hace sobre la inicial.)
     */
    private BigDecimal calcularAnticipo(Venta venta, ParametroComision parametro,
                                        BigDecimal total, BigDecimal porcentajeOverride) {
        BigDecimal anticipoBase;
        if (parametro.getAnticipoMontoFijo() != null) {
            anticipoBase = parametro.getAnticipoMontoFijo();
        } else {
            BigDecimal pct = porcentajeOverride != null
                    ? porcentajeOverride : parametro.getAnticipoPorcentaje();
            BigDecimal base = venta.getMontoInicial() != null
                    ? venta.getMontoInicial() : BigDecimal.ZERO;
            anticipoBase = base.multiply(pct).setScale(2, RoundingMode.HALF_UP);
        }
        if (anticipoBase.signum() < 0) {
            anticipoBase = BigDecimal.ZERO;
        }
        return anticipoBase.compareTo(total) > 0 ? total : anticipoBase;
    }

    private PagoComision nuevoPago(Venta venta, TipoPago tipo, BigDecimal monto,
                                   LocalDate fechaProgramada, Integer numeroCuota) {
        PagoComision pago = new PagoComision();
        pago.setIdVendedor(venta.getIdVendedor());
        pago.setTipo(tipo);
        pago.setNumeroCuota(numeroCuota);
        pago.setMonto(monto);
        pago.setFechaProgramada(fechaProgramada);
        pago.setEstado(EstadoPago.PENDIENTE);
        return pago;
    }

    // ── Mapeo a DTOs ───────────────────────────────────────────────────────────

    private VentaDetalleDTO detalle(Integer id) {
        Venta venta = buscar(id);

        List<Integer> idsLotes = venta.getLotes().stream().map(VentaLote::getIdLote).toList();
        List<Integer> idsClientes = venta.getClientes().stream().map(VentaCliente::getIdCliente).toList();
        Map<Integer, Lote> lotes = porId(loteRepository.findAllById(idsLotes), Lote::getId);
        Map<Integer, Cliente> clientes = porId(clienteRepository.findAllById(idsClientes), Cliente::getId);
        List<Lote> lotesVenta = idsLotes.stream().map(lotes::get).filter(Objects::nonNull).toList();
        List<Cliente> clientesVenta = idsClientes.stream().map(clientes::get).filter(Objects::nonNull).toList();

        Empresa empresa = empresaRepository.findByIdAndIdVendedor(venta.getIdEmpresa(), venta.getIdVendedor()).orElse(null);

        Comision comision = comisionRepository.findByIdVentaAndIdVendedor(venta.getId(), venta.getIdVendedor())
                .orElse(null);
        ComisionResumenDTO comisionDTO = null;
        List<PagoComisionDTO> pagos = List.of();
        if (comision != null) {
            comisionDTO = resumen(comision);
            pagos = pagoRepository
                    .findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(
                            comision.getId(), comision.getIdVendedor())
                    .stream().map(this::toPagoDTO).toList();
        }

        return new VentaDetalleDTO(
                venta.getId(), venta.getFechaVenta(), venta.getModalidad().name(),
                venta.getEstado().name(), venta.getPrecioVenta(), venta.getMontoInicial(),
                venta.getFechaInicioCuotas(), venta.getNotas(),
                etiquetaLotes(lotesVenta),
                venta.getIdEmpresa(), empresa != null ? empresa.getRazonSocial() : "-",
                nombresClientes(clientesVenta), documentosClientes(clientesVenta),
                comisionDTO, pagos,
                venta.getFechaRegistro());
    }

    public ComisionResumenDTO resumen(Comision c) {
        return new ComisionResumenDTO(
                c.getId(), c.getIdVenta(), c.getPorcentajeAplicado(), c.getMontoTotal(),
                c.getMontoAnticipo(), c.getMontoSaldo(), c.getMontoPagado(), c.getMontoCuota(),
                c.getCantidadCuotas(),
                c.getFechaInicioCuotas() != null ? c.getFechaInicioCuotas().toString() : null,
                c.getEstado().name());
    }

    public PagoComisionDTO toPagoDTO(PagoComision p) {
        return new PagoComisionDTO(
                p.getId(), p.getIdComision(), p.getTipo().name(), p.getNumeroCuota(), p.getMonto(),
                p.getFechaProgramada(), p.getFechaPago(), p.getEstado().name(),
                p.getFormaPago() != null ? p.getFormaPago().name() : null,
                p.getReferencia(), p.getNotas(), p.getFechaRegistro());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ModalidadVenta modalidadDesde(String valor) {
        try {
            return ModalidadVenta.valueOf(valor.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Modalidad no válida: use CONTADO o FINANCIADO");
        }
    }

    private String etiquetaLote(Lote lote) {
        if (lote == null) {
            return "-";
        }
        return lote.getProyecto() + " Mz." + lote.getManzana() + " Lt." + lote.getNumero();
    }

    /** Etiqueta de los lotes de la venta: 1 lote → completa; varios → "Mz.A Lt.1, Mz.B Lt.2 (N lotes)". */
    private String etiquetaLotes(List<Lote> lotes) {
        if (lotes.isEmpty()) {
            return "-";
        }
        if (lotes.size() == 1) {
            return etiquetaLote(lotes.get(0));
        }
        String joins = lotes.stream()
                .map(l -> "Mz." + l.getManzana() + " Lt." + l.getNumero())
                .collect(Collectors.joining(", "));
        return joins + " (" + lotes.size() + " lotes)";
    }

    private String nombresClientes(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            return "-";
        }
        return clientes.stream().map(this::nombreCliente).collect(Collectors.joining(", "));
    }

    private String documentosClientes(List<Cliente> clientes) {
        if (clientes.isEmpty()) {
            return null;
        }
        return clientes.stream()
                .map(c -> c.getTipoDocumento().name() + " " + c.getNumDocumento())
                .collect(Collectors.joining(", "));
    }

    private String nombreCliente(Cliente c) {
        if (c == null) {
            return "-";
        }
        String apellidos = c.getApellidos() == null ? "" : " " + c.getApellidos();
        return (c.getNombres() + apellidos).trim();
    }

    private <T> List<Integer> ids(List<Venta> ventas, Function<Venta, Integer> extractor) {
        return ventas.stream().map(extractor).distinct().toList();
    }

    private <T> Map<Integer, T> porId(List<T> entidades, Function<T, Integer> idExtractor) {
        return entidades.stream().collect(Collectors.toMap(idExtractor, e -> e, (a, b) -> a));
    }
}
