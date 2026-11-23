package com.vendelote.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ComprobanteCrearDTO;
import com.vendelote.dto.ComprobanteListadoDTO;
import com.vendelote.dto.PagoComisionDTO;
import com.vendelote.dto.PagoDisponibleDTO;
import com.vendelote.entity.Comision;
import com.vendelote.entity.Comprobante;
import com.vendelote.entity.ComprobantePago;
import com.vendelote.entity.DetalleComprobante;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.IntegracionSunat;
import com.vendelote.entity.Lote;
import com.vendelote.entity.PagoComision;
import com.vendelote.enums.EstadoComprobante;
import com.vendelote.enums.EstadoPago;
import com.vendelote.enums.TipoComprobante;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ComisionRepository;
import com.vendelote.repository.ComprobantePagoRepository;
import com.vendelote.repository.ComprobanteRepository;
import com.vendelote.repository.DetalleComprobanteRepository;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.PagoComisionRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Emisión de comprobantes electrónicos (MVP: FACTURA 01).
 *
 * Flujo: pagos PAGADOS seleccionados (1..N lotes, SIEMPRE de una misma
 * empresa) → subtotal = suma de pagos → IGV 18% → items agrupados por lote
 * con descripción "Comisión de intermediación - {proyecto} Mz.{X} Lt.{Y}" →
 * detracción si total > umbral y está habilitada → POST /facturas de
 * api-sunat con las credenciales del tenant → se persiste todo localmente.
 */
@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("18.00");

    private final ComprobanteRepository comprobanteRepository;
    private final DetalleComprobanteRepository detalleRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final PagoComisionRepository pagoRepository;
    private final ComisionRepository comisionRepository;
    private final LoteRepository loteRepository;
    private final EmpresaRepository empresaRepository;
    private final IntegracionSunatService integracionService;
    private final SunatApiClient sunatApiClient;
    private final VentaService ventaService;

    @Transactional(readOnly = true)
    public List<ComprobanteListadoDTO> listar() {
        Integer idV = TenantContext.idVendedorRequerido();
        return comprobanteRepository.findByIdVendedorOrderByFechaEmisionDesc(idV).stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComprobanteListadoDTO obtener(Integer id) {
        return aDTO(buscar(id));
    }

    /** Pagos cobrados aún sin comprobante (candidatos para emitir). */
    @Transactional(readOnly = true)
    public List<PagoDisponibleDTO> pagosDisponibles() {
        Integer idV = TenantContext.idVendedorRequerido();
        List<PagoComision> pagos = pagoRepository
                .findByIdVendedorAndEstadoOrderByFechaPagoDesc(idV, EstadoPago.PAGADO).stream()
                .filter(p -> !comprobantePagoRepository.existsByIdPagoAndIdVendedor(p.getId(), idV))
                .toList();
        if (pagos.isEmpty()) {
            return List.of();
        }

        Map<Integer, Comision> comisiones = porId(pagos.stream()
                .map(PagoComision::getIdComision).distinct()
                .map(id -> comisionRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(c -> c != null).toList(), Comision::getId);
        Map<Integer, Lote> lotes = porId(comisiones.values().stream()
                .map(Comision::getIdLote).distinct()
                .map(id -> loteRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(l -> l != null).toList(), Lote::getId);
        Map<Integer, Empresa> empresas = porId(comisiones.values().stream()
                .map(Comision::getIdEmpresa).distinct()
                .map(id -> empresaRepository.findByIdAndIdVendedor(id, idV).orElse(null))
                .filter(e -> e != null).toList(), Empresa::getId);

        return pagos.stream().map(p -> {
            Comision comision = comisiones.get(p.getIdComision());
            if (comision == null) {
                return null;
            }
            Lote lote = lotes.get(comision.getIdLote());
            Empresa empresa = empresas.get(comision.getIdEmpresa());
            return new PagoDisponibleDTO(
                    p.getId(), p.getIdComision(), p.getTipo().name(), p.getNumeroCuota(), p.getMonto(),
                    p.getFechaPago(),
                    p.getFormaPago() != null ? p.getFormaPago().name() : null,
                    p.getReferencia(),
                    comision.getIdLote(),
                    lote != null ? lote.getProyecto() + " Mz." + lote.getManzana() + " Lt." + lote.getNumero() : "-",
                    comision.getIdEmpresa(),
                    empresa != null ? empresa.getRazonSocial() : "-");
        }).filter(d -> d != null).toList();
    }

    /**
     * Emite el comprobante ante api-sunat y lo persiste con sus líneas
     * (una por lote) y los vínculos a los pagos cubiertos.
     */
    @Transactional
    public ComprobanteListadoDTO emitir(ComprobanteCrearDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();

        TipoComprobante tipo;
        try {
            tipo = TipoComprobante.valueOf(dto.getTipo().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Tipo de comprobante no válido");
        }
        if (tipo != TipoComprobante.FACTURA) {
            throw new NegocioException("Por ahora solo está habilitado emitir FACTURAS (01)");
        }
        if (dto.getFechaEmision().isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha de emisión no puede ser futura");
        }

        IntegracionSunat config = integracionService.requerirParaEmitir();
        Empresa empresa = empresaRepository.findByIdAndIdVendedor(dto.getIdEmpresa(), idV)
                .orElseThrow(() -> new NegocioException("Empresa no encontrada"));
        if (empresa.getRuc() == null || !empresa.getRuc().matches("\\d{11}")) {
            throw new NegocioException("La empresa debe tener un RUC válido (11 dígitos) para facturarle");
        }

        // ── Cargar y validar los pagos seleccionados ──
        List<PagoComision> pagos = new ArrayList<>();
        Map<Integer, Comision> comisiones = new LinkedHashMap<>();
        for (Integer idPago : dto.getPagos()) {
            PagoComision pago = pagoRepository.findByIdAndIdVendedor(idPago, idV)
                    .orElseThrow(() -> new NegocioException("Pago no encontrado: #" + idPago));
            if (pago.getEstado() != EstadoPago.PAGADO) {
                throw new NegocioException("El pago #" + idPago + " no está cobrado (solo se facturan pagos cobrados)");
            }
            if (comprobantePagoRepository.existsByIdPagoAndIdVendedor(idPago, idV)) {
                throw new NegocioException("El pago #" + idPago + " ya está incluido en otro comprobante");
            }
            Comision comision = comisionRepository
                    .findByIdAndIdVendedor(pago.getIdComision(), idV)
                    .orElseThrow(() -> new NegocioException("Comisión del pago #" + idPago + " no encontrada"));
            if (!comision.getIdEmpresa().equals(dto.getIdEmpresa())) {
                throw new NegocioException(
                        "Los pagos seleccionados no pertenecen todos a la empresa "
                                + empresa.getRazonSocial()
                                + " (una factura solo puede emitirse a una empresa)");
            }
            pagos.add(pago);
            comisiones.put(comision.getId(), comision);
        }
        if (pagos.isEmpty()) {
            throw new NegocioException("Seleccione al menos un pago cobrado");
        }

        BigDecimal subtotal = pagos.stream()
                .map(PagoComision::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (subtotal.signum() <= 0) {
            throw new NegocioException("El total a facturar debe ser mayor a 0");
        }
        BigDecimal igv = subtotal.multiply(PORCENTAJE_IGV).divide(CIEN, 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        // ── Líneas agrupadas por lote + concepto automático ──
        Map<Integer, List<PagoComision>> porLote = pagos.stream()
                .collect(Collectors.groupingBy(p -> comisiones.get(p.getIdComision()).getIdLote(),
                        LinkedHashMap::new, Collectors.toList()));

        List<DetalleLinea> lineas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        for (Map.Entry<Integer, List<PagoComision>> entrada : porLote.entrySet()) {
            Lote lote = loteRepository.findByIdAndIdVendedor(entrada.getKey(), idV).orElse(null);
            String etiqueta = lote != null
                    ? lote.getProyecto() + " Mz." + lote.getManzana() + " Lt." + lote.getNumero()
                    : "Lote #" + entrada.getKey();
            etiquetas.add(etiqueta);
            BigDecimal montoLinea = entrada.getValue().stream()
                    .map(PagoComision::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            lineas.add(new DetalleLinea(
                    "Comisión de intermediación - " + etiqueta, montoLinea));
        }

        StringBuilder concepto = new StringBuilder("Servicio de intermediación - comisiones: ");
        concepto.append(String.join(", ", etiquetas));
        if (dto.getConcepto() != null && !dto.getConcepto().isBlank()) {
            concepto.append(" | ").append(dto.getConcepto().trim());
        }
        String conceptoFinal = concepto.length() > 300 ? concepto.substring(0, 300) : concepto.toString();

        // ── Detracción (si está habilitada y el total supera el umbral en USD) ──
        boolean aplicaDetraccion = config.isAplicarDetraccion()
                && total.compareTo(config.getUmbralDetraccion()) > 0;
        BigDecimal montoDetraccion = null;
        if (aplicaDetraccion) {
            montoDetraccion = total.multiply(config.getPorcentajeDetraccion())
                    .divide(CIEN, 2, RoundingMode.HALF_UP);
        }

        // ── Payload para api-sunat ──
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("serie", config.getSerieFactura());
        payload.put("fecha_emision", dto.getFechaEmision().toString());
        payload.put("tipo_moneda", "USD");
        payload.put("forma_pago", "Contado");
        payload.put("observacion", conceptoFinal);

        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipo_doc", "6");
        cliente.put("num_doc", empresa.getRuc());
        cliente.put("razon_social", empresa.getRazonSocial());
        String direccion = direccionCompleta(empresa);
        if (direccion != null) {
            cliente.put("direccion", direccion);
        }
        if (empresa.getCorreo() != null && !empresa.getCorreo().isBlank()) {
            cliente.put("email", empresa.getCorreo());
        }
        if (empresa.getTelefono() != null && !empresa.getTelefono().isBlank()) {
            // Solo el primer teléfono: la columna admite varios separados por coma
            cliente.put("telefono", empresa.getTelefono().split(",")[0].trim());
        }
        payload.put("cliente", cliente);

        List<Map<String, Object>> items = new ArrayList<>();
        int correlativoItem = 1;
        for (DetalleLinea linea : lineas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo", "C" + String.format("%03d", correlativoItem++));
            item.put("descripcion", linea.descripcion());
            item.put("unidad", "ZZ"); // servicios (UN/CEFACT)
            item.put("cantidad", 1);
            // precio_unitario CON IGV (regla de la API): sin IGV × 1.18
            item.put("precio_unitario",
                    linea.monto().multiply(PORCENTAJE_IGV.add(CIEN)).divide(CIEN, 2, RoundingMode.HALF_UP));
            item.put("tip_afe_igv", "10");
            item.put("porcentaje_igv", 18);
            items.add(item);
        }
        payload.put("items", items);

        if (aplicaDetraccion) {
            payload.put("tipo_operacion", "1001");
            Map<String, Object> detraccion = new LinkedHashMap<>();
            detraccion.put("cod_bien", config.getCodBienDetraccion());
            detraccion.put("porcentaje", config.getPorcentajeDetraccion().stripTrailingZeros().toPlainString());
            detraccion.put("cta_banco", config.getCtaBanco());
            detraccion.put("cod_medio_pago", config.getCodMedioPago());
            detraccion.put("monto", montoDetraccion);
            payload.put("detraccion", detraccion);
        }

        // ── Llamar a api-sunat PRIMERO (si falla, no se guarda nada local) ──
        var respuesta = sunatApiClient.crearFactura(
                sunatApiClient.urlBase(config.getApiUrl()),
                config.getApiKey(), config.getApiSecret(), payload);
        if (respuesta.getDatos() == null || respuesta.getDatos().getId() == null) {
            throw new NegocioException("api-sunat no devolvió el id del comprobante");
        }

        // ── Persistir todo localmente ──
        Comprobante comprobante = new Comprobante();
        comprobante.setIdVendedor(idV);
        comprobante.setTipo(tipo);
        comprobante.setFechaEmision(dto.getFechaEmision());
        comprobante.setIdEmpresa(empresa.getId());
        comprobante.setSubtotal(subtotal);
        comprobante.setIgv(igv);
        comprobante.setTotal(total);
        comprobante.setPorcentajeIgv(PORCENTAJE_IGV);
        comprobante.setConcepto(conceptoFinal);
        comprobante.setAplicaDetraccion(aplicaDetraccion);
        comprobante.setMontoDetraccion(montoDetraccion);
        comprobante.setIdApiSunat(String.valueOf(respuesta.getDatos().getId()));
        comprobante.setSerie(respuesta.getDatos().getSerie() != null
                ? respuesta.getDatos().getSerie() : config.getSerieFactura());
        comprobante.setCorrelativo(respuesta.getDatos().getCorrelativo());
        comprobante.setEstado(estadoDesdeSunat(respuesta.getDatos().getSunatStatus()));
        Comprobante guardado = comprobanteRepository.save(comprobante);

        for (DetalleLinea linea : lineas) {
            DetalleComprobante detalle = new DetalleComprobante();
            detalle.setIdVendedor(idV);
            detalle.setIdComprobante(guardado.getId());
            detalle.setCantidad(BigDecimal.ONE);
            detalle.setDescripcion(linea.descripcion());
            detalle.setMonto(linea.monto());
            detalleRepository.save(detalle);
        }
        for (PagoComision pago : pagos) {
            ComprobantePago vínculo = new ComprobantePago();
            vínculo.setIdVendedor(idV);
            vínculo.setIdComprobante(guardado.getId());
            vínculo.setIdPago(pago.getId());
            vínculo.setMontoAplicado(pago.getMonto());
            comprobantePagoRepository.save(vínculo);
        }

        return aDTO(guardado);
    }

    /** Consulta el estado real en api-sunat y actualiza el estado local. */
    @Transactional
    public ComprobanteListadoDTO sincronizar(Integer id) {
        Comprobante comprobante = buscar(id);
        if (comprobante.getIdApiSunat() == null || comprobante.getIdApiSunat().isBlank()) {
            throw new NegocioException("El comprobante no fue emitido ante api-sunat");
        }
        IntegracionSunat config = integracionService.requerirParaEmitir();
        var respuesta = sunatApiClient.obtenerFactura(
                sunatApiClient.urlBase(config.getApiUrl()),
                config.getApiKey(), config.getApiSecret(),
                Integer.valueOf(comprobante.getIdApiSunat()));

        if (respuesta.getDatos() != null) {
            comprobante.setEstado(estadoDesdeSunat(respuesta.getDatos().getSunatStatus()));
            if (comprobante.getSerie() == null && respuesta.getDatos().getSerie() != null) {
                comprobante.setSerie(respuesta.getDatos().getSerie());
            }
            if (comprobante.getCorrelativo() == null && respuesta.getDatos().getCorrelativo() != null) {
                comprobante.setCorrelativo(respuesta.getDatos().getCorrelativo());
            }
            comprobanteRepository.save(comprobante);
        }
        return aDTO(comprobante);
    }

    /** PDF del comprobante vía proxy (el frontend nunca ve credenciales). */
    public byte[] descargarPdf(Integer id) {
        Comprobante comprobante = buscar(id);
        if (comprobante.getIdApiSunat() == null || comprobante.getIdApiSunat().isBlank()) {
            throw new NegocioException("El comprobante no fue emitido ante api-sunat");
        }
        IntegracionSunat config = integracionService.requerirParaEmitir();
        return sunatApiClient.descargarPdf(
                sunatApiClient.urlBase(config.getApiUrl()),
                config.getApiKey(), config.getApiSecret(),
                Integer.valueOf(comprobante.getIdApiSunat()));
    }

    public String nombreArchivoPdf(Comprobante c) {
        return (c.getSerie() != null ? c.getSerie() : "F") + "-"
                + (c.getCorrelativo() != null ? c.getCorrelativo() : c.getId()) + ".pdf";
    }

    @Transactional(readOnly = true)
    public Comprobante buscar(Integer id) {
        return comprobanteRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Comprobante no encontrado"));
    }

    // ── Internos ───────────────────────────────────────────────────────────────

    private ComprobanteListadoDTO aDTO(Comprobante c) {
        Integer idV = c.getIdVendedor();
        Empresa empresa = empresaRepository.findByIdAndIdVendedor(c.getIdEmpresa(), idV).orElse(null);
        List<PagoComisionDTO> pagos = comprobantePagoRepository
                .findByIdComprobanteAndIdVendedor(c.getId(), idV).stream()
                .map(cp -> pagoRepository.findByIdAndIdVendedor(cp.getIdPago(), idV).orElse(null))
                .filter(p -> p != null)
                .map(ventaService::toPagoDTO)
                .toList();

        return new ComprobanteListadoDTO(
                c.getId(), c.getTipo().name(), c.getSerie(), c.getCorrelativo(), c.getIdApiSunat(),
                c.getFechaEmision(), c.getIdEmpresa(),
                empresa != null ? empresa.getRazonSocial() : "-",
                empresa != null ? empresa.getRuc() : "-",
                c.getSubtotal(), c.getIgv(), c.getTotal(), c.getConcepto(),
                c.isAplicaDetraccion(), c.getMontoDetraccion(), c.getEstado().name(),
                pagos, c.getFechaRegistro());
    }

    private EstadoComprobante estadoDesdeSunat(String sunatStatus) {
        if (sunatStatus == null) {
            return EstadoComprobante.EN_PROCESO;
        }
        return switch (sunatStatus.toLowerCase(java.util.Locale.ROOT)) {
            case "aceptado" -> EstadoComprobante.ACEPTADO;
            case "rechazado" -> EstadoComprobante.RECHAZADO;
            case "anulado" -> EstadoComprobante.ANULADO;
            default -> EstadoComprobante.EN_PROCESO; // pendiente | enviado | anulacion_en_proceso
        };
    }

    private String direccionCompleta(Empresa e) {
        List<String> partes = new ArrayList<>();
        if (e.getDireccion() != null && !e.getDireccion().isBlank()) {
            partes.add(e.getDireccion().trim());
        }
        for (String extra : new String[] { e.getDistrito(), e.getProvincia(), e.getDepartamento() }) {
            if (extra != null && !extra.isBlank() && !partes.stream().anyMatch(p -> p.contains(extra.trim()))) {
                partes.add(extra.trim());
            }
        }
        return partes.isEmpty() ? null : String.join(" - ", partes);
    }

    private <T> Map<Integer, T> porId(List<T> entidades, java.util.function.Function<T, Integer> idExtractor) {
        return entidades.stream().collect(Collectors.toMap(idExtractor, e -> e, (a, b) -> a));
    }

    private record DetalleLinea(String descripcion, BigDecimal monto) {
    }
}
