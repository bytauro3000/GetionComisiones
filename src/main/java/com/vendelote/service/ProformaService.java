package com.vendelote.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.DetalleProformaDTO;
import com.vendelote.dto.ProformaDTO;
import com.vendelote.dto.ProformaSeguimientoDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Lote;
import com.vendelote.entity.Proforma;
import com.vendelote.entity.ProformaDetalle;
import com.vendelote.entity.SecuenciaProforma;
import com.vendelote.enums.EstadoLote;
import com.vendelote.enums.EstadoProforma;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.ProformaRepository;
import com.vendelote.repository.SecuenciaProformaRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Proformas comerciales del tenant.
 * - La proforma es un documento del VENDEDOR (no de una empresa): puede incluir
 *   lotes de distintas empresas/programas.
 * - El cliente puede ser un prospecto (datos básicos) y se completa al comprar.
 * - Correlativo PF-{anio}-{0001} POR VENDEDOR y año.
 * - No modifica el estado de los lotes (son inventario).
 */
@Service
@RequiredArgsConstructor
public class ProformaService {

    private final ProformaRepository proformaRepository;
    private final SecuenciaProformaRepository secuenciaRepository;
    private final LoteRepository loteRepository;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteService clienteService;
    private final EmpresaService empresaService;
    private final AsociadoService asociadoService;

    @Transactional(readOnly = true)
    public List<Proforma> listar() {
        return listar(null, null);
    }

    /** Listado filtrado por rango de fechas (inclusive). */
    @Transactional(readOnly = true)
    public List<Proforma> listar(LocalDate desde, LocalDate hasta) {
        Integer idV = TenantContext.idVendedorRequerido();
        Integer idAsociado = TenantContext.esAsociado()
                ? TenantContext.idUsuarioActual() : null;
        return proformaRepository.findByIdVendedorOrderByFechaDesc(idV).stream()
                .filter(p -> idAsociado == null
                        || idAsociado.equals(p.getIdVendedorAsociado()))
                .filter(p -> enRango(p, desde, hasta))
                .toList();
    }

    @Transactional(readOnly = true)
    public Proforma buscar(Integer id) {
        Proforma proforma = proformaRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Proforma no encontrada"));
        if (TenantContext.esAsociado()
                && !TenantContext.idUsuarioActual().equals(proforma.getIdVendedorAsociado())) {
            throw new NegocioException("Proforma no encontrada");
        }
        return proforma;
    }

    /** Carga una proforma por su token público (enlace del PDF, sin login). */
    @Transactional(readOnly = true)
    public Proforma buscarPorTokenPublico(String token) {
        if (token == null || token.isBlank()) {
            throw new NegocioException("Enlace no válido");
        }
        return proformaRepository.findByPublicToken(token.trim())
                .orElseThrow(() -> new NegocioException("Enlace no válido o vencido"));
    }

    @Transactional
    public Proforma crear(ProformaDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Cliente cliente = clienteService.buscar(dto.getIdCliente());
        if (dto.getIdEmpresa() != null) {
            empresaService.buscar(dto.getIdEmpresa()); // valida tenant si viene empresa referencial
        }

        Proforma proforma = new Proforma();
        proforma.setIdVendedor(idV);
        proforma.setIdVendedorAsociado(
                asociadoService.resolverAsociado(dto.getIdVendedorAsociado()));
        proforma.setIdEmpresa(dto.getIdEmpresa());
        proforma.setIdCliente(cliente.getId());
        proforma.setPublicToken(UUID.randomUUID().toString());
        proforma.setObservaciones(limpiar(dto.getObservaciones()));
        aplicarDetalles(proforma, dto, idV); // valida lotes ANTES de consumir correlativo
        proforma.setNumero(siguienteNumero(idV, LocalDate.now().getYear()));
        return proformaRepository.save(proforma);
    }

    @Transactional
    public Proforma editar(Integer id, ProformaDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Proforma proforma = buscar(id);
        if (proforma.getEstado() != EstadoProforma.EMITIDA) {
            throw new NegocioException("Solo se pueden editar proformas emitidas");
        }
        Cliente cliente = clienteService.buscar(dto.getIdCliente());
        if (dto.getIdEmpresa() != null) {
            empresaService.buscar(dto.getIdEmpresa());
        }

        proforma.setIdEmpresa(dto.getIdEmpresa());
        proforma.setIdCliente(cliente.getId());
        if (TenantContext.esAsociado()) {
            proforma.setIdVendedorAsociado(TenantContext.idUsuarioActual());
        } else if (dto.getIdVendedorAsociado() != null) {
            proforma.setIdVendedorAsociado(
                    asociadoService.resolverAsociado(dto.getIdVendedorAsociado()));
        }
        proforma.setObservaciones(limpiar(dto.getObservaciones()));
        aplicarDetalles(proforma, dto, idV); // orphanRemoval: reemplaza los detalles
        return proformaRepository.save(proforma);
    }

    @Transactional
    public void eliminar(Integer id) {
        proformaRepository.delete(buscar(id));
    }

    /** EMITIDA → ACEPTADA/ANULADA; ACEPTADA → ANULADA. ANULADA es terminal. */
    @Transactional
    public Proforma cambiarEstado(Integer id, String estadoNuevo) {
        Proforma proforma = buscar(id);
        EstadoProforma destino;
        try {
            destino = EstadoProforma.valueOf(estadoNuevo.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Estado no válido: use ACEPTADA o ANULADA");
        }
        if (destino == EstadoProforma.EMITIDA) {
            throw new NegocioException("Una proforma no puede volver a emitirse");
        }
        if (proforma.getEstado() == EstadoProforma.ANULADA) {
            throw new NegocioException("Una proforma anulada no se puede cambiar");
        }
        proforma.setEstado(destino);
        return proformaRepository.save(proforma);
    }

    // ── Seguimiento comercial ─────────────────────────────────────────────────

    /**
     * Agrupa por cliente/prospecto las proformas del rango de fechas, para
     * saber a quién se le envió una proforma y hacer seguimiento.
     */
    @Transactional(readOnly = true)
    public List<ProformaSeguimientoDTO> seguimiento(LocalDate desde, LocalDate hasta) {
        Integer idV = TenantContext.idVendedorRequerido();
        Map<Integer, List<Proforma>> porCliente = new HashMap<>();
        for (Proforma p : listar(desde, hasta)) {
            if (p.getIdCliente() != null) {
                porCliente.computeIfAbsent(p.getIdCliente(), k -> new ArrayList<>()).add(p);
            }
        }

        List<ProformaSeguimientoDTO> resultado = new ArrayList<>();
        for (Map.Entry<Integer, List<Proforma>> e : porCliente.entrySet()) {
            Cliente c = clienteRepository.findByIdAndIdVendedor(e.getKey(), idV).orElse(null);
            if (c == null) {
                continue;
            }
            List<Proforma> lista = e.getValue();
            Proforma ultima = lista.stream()
                    .filter(p -> p.getFecha() != null)
                    .max(Comparator.comparing(Proforma::getFecha))
                    .orElse(null);
            BigDecimal total = lista.stream()
                    .map(Proforma::getTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resultado.add(new ProformaSeguimientoDTO(
                    c.getId(), nombre(c), c.getNumDocumento(), c.getTelefono(),
                    c.isProspecto(), c.getEstadoSeguimiento(), c.getNotaSeguimiento(),
                    c.getProximoContacto(),
                    lista.size(),
                    ultima != null ? ultima.getFecha().toLocalDate() : null,
                    ultima != null ? ultima.getNumero() : null,
                    ultima != null ? ultima.getId() : null,
                    total));
        }

        resultado.sort(Comparator.comparing(ProformaSeguimientoDTO::getUltimaFecha,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return resultado;
    }

    // ── Detalles y correlativo ────────────────────────────────────────────────

    private void aplicarDetalles(Proforma proforma, ProformaDTO dto, Integer idV) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new NegocioException("Agregue al menos un lote a la proforma");
        }
        List<ProformaDetalle> detalles = new ArrayList<>();
        Map<Integer, String> empresas = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleProformaDTO item : dto.getDetalles()) {
            Lote lote = loteRepository.findByIdAndIdVendedor(item.getIdLote(), idV)
                    .orElseThrow(() -> new NegocioException("Lote no encontrado"));
            if (lote.getEstado() == EstadoLote.VENDIDO
                    || lote.getEstado() == EstadoLote.RESERVADO) {
                throw new NegocioException("El lote " + lote.getManzana() + "-" + lote.getNumero()
                        + " no está disponible (vendido o separado)");
            }

            String empresaNombre = empresas.computeIfAbsent(lote.getIdEmpresa(),
                    id -> empresaRepository.findByIdAndIdVendedor(id, idV)
                            .map(Empresa::getRazonSocial).orElse("Empresa"));

            ProformaDetalle d = new ProformaDetalle();
            d.setProforma(proforma);
            d.setIdLote(lote.getId());
            d.setIdEmpresa(lote.getIdEmpresa());
            d.setEmpresaNombre(empresaNombre);
            d.setProyecto(lote.getProyecto());
            d.setDescripcion(descripcionLote(lote));
            d.setAreaM2(lote.getAreaM2());
            d.setPrecioUnitario(item.getPrecioUnitario());

            BigDecimal inicial = item.getInicial() != null
                    ? item.getInicial() : lote.getInicial();
            if (inicial != null && inicial.compareTo(item.getPrecioUnitario()) > 0) {
                throw new NegocioException("La inicial del lote " + lote.getManzana()
                        + "-" + lote.getNumero() + " no puede superar su precio");
            }
            d.setInicial(inicial);
            d.setNumCuotas(item.getNumCuotas() != null
                    ? item.getNumCuotas() : lote.getNumCuotas());

            detalles.add(d);
            total = total.add(item.getPrecioUnitario());
        }

        proforma.getDetalles().clear();
        proforma.getDetalles().addAll(detalles);
        proforma.setTotal(total);
    }

    private boolean enRango(Proforma p, LocalDate desde, LocalDate hasta) {
        if (p.getFecha() == null) {
            return false;
        }
        LocalDate f = p.getFecha().toLocalDate();
        if (desde != null && f.isBefore(desde)) {
            return false;
        }
        return hasta == null || !f.isAfter(hasta);
    }

    private String descripcionLote(Lote lote) {
        String base = (lote.getProyecto() != null && !lote.getProyecto().isBlank()
                ? lote.getProyecto() + " · " : "")
                + "Mz " + lote.getManzana() + " · N° " + lote.getNumero();
        return base.length() > 200 ? base.substring(0, 200) : base;
    }

    /** PF-{anio}-{0001}: incrementa la secuencia del VENDEDOR con bloqueo de fila. */
    private String siguienteNumero(Integer idVendedor, int anio) {
        SecuenciaProforma secuencia = secuenciaRepository.bloquear(idVendedor, anio).orElse(null);
        if (secuencia == null) {
            secuencia = new SecuenciaProforma();
            secuencia.setIdVendedor(idVendedor);
            secuencia.setAnio(anio);
            secuencia.setUltimoNumero(maxNumeroExistente(idVendedor, anio));
        }
        secuencia.setUltimoNumero(secuencia.getUltimoNumero() + 1);
        secuenciaRepository.save(secuencia);
        return String.format("PF-%d-%04d", anio, secuencia.getUltimoNumero());
    }

    /** Continúa la numeración existente si la secuencia aún no tiene fila. */
    private int maxNumeroExistente(Integer idVendedor, int anio) {
        String prefijo = "PF-" + anio + "-";
        return proformaRepository.findByIdVendedorOrderByFechaDesc(idVendedor).stream()
                .map(Proforma::getNumero)
                .filter(n -> n != null && n.startsWith(prefijo))
                .map(n -> n.substring(prefijo.length()))
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);
    }

    private String nombre(Cliente c) {
        return (c.getNombres() + (c.getApellidos() != null ? " " + c.getApellidos() : "")).trim();
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
