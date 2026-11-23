package com.vendelote.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.LoteDTO;
import com.vendelote.dto.LoteEstadoDTO;
import com.vendelote.dto.LoteSeparacionDTO;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Lote;
import com.vendelote.entity.Proyecto;
import com.vendelote.enums.EstadoLote;
import com.vendelote.enums.EstadoVenta;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.VentaLoteRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/** Lotes del tenant (filtros por empresa/proyecto/estado resueltos en memoria). */
@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final VentaLoteRepository ventaLoteRepository;
    private final EmpresaService empresaService;
    private final ProyectoService proyectoService;

    @Transactional
    public List<Lote> listar(Integer idEmpresa, Integer idProyecto, String estado) {
        Integer idV = TenantContext.idVendedorRequerido();
        List<Lote> todos = loteRepository.findByIdVendedorOrderByFechaRegistroDesc(idV);
        Set<Integer> conVenta = lotesConVentaActiva(idV);

        return todos.stream()
                .peek(l -> l.setVentaActiva(conVenta.contains(l.getId())))
                .filter(l -> idEmpresa == null || l.getIdEmpresa().equals(idEmpresa))
                .filter(l -> idProyecto == null || idProyecto.equals(l.getIdProyecto()))
                .filter(l -> estado == null || estado.isBlank()
                        || l.getEstado().name().equalsIgnoreCase(estado.trim()))
                .toList();
    }

    @Transactional
    public Lote crear(LoteDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();

        Empresa empresa = empresaService.buscar(dto.getIdEmpresa());
        Proyecto proyecto = proyectoService.buscarDeEmpresa(dto.getIdProyecto(), empresa.getId());

        if (loteRepository.existsByIdVendedorAndIdEmpresaAndProyectoAndManzanaAndNumero(
                idV, empresa.getId(), proyecto.getNombre(), dto.getManzana().trim(),
                dto.getNumero().trim())) {
            throw new NegocioException("Ya existe ese lote en esa empresa");
        }

        Lote lote = new Lote();
        lote.setIdVendedor(idV);
        aplicar(lote, dto, empresa, proyecto);
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            lote.setEstado(estadoDesde(dto.getEstado()));
        }
        return loteRepository.save(lote);
    }

    @Transactional
    public Lote editar(Integer id, LoteDTO dto) {
        Lote lote = buscar(id);
        Empresa empresa = empresaService.buscar(dto.getIdEmpresa());
        Proyecto proyecto = proyectoService.buscarDeEmpresa(dto.getIdProyecto(), empresa.getId());

        if (loteRepository.existsByIdVendedorAndIdEmpresaAndProyectoAndManzanaAndNumeroAndIdNot(
                lote.getIdVendedor(), empresa.getId(), proyecto.getNombre(),
                dto.getManzana().trim(), dto.getNumero().trim(), lote.getId())) {
            throw new NegocioException("Ya existe ese lote en esa empresa");
        }

        // Un lote con venta propia activa solo se libera anulando la venta.
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            EstadoLote nuevo = estadoDesde(dto.getEstado());
            if (nuevo != EstadoLote.VENDIDO && tieneVentaActiva(lote)) {
                throw new NegocioException(
                        "El lote tiene una venta activa: anule la venta para devolverlo a DISPONIBLE");
            }
        }

        aplicar(lote, dto, empresa, proyecto);
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            lote.setEstado(estadoDesde(dto.getEstado()));
        }
        return loteRepository.save(lote);
    }

    @Transactional
    public void eliminar(Integer id) {
        Lote lote = buscar(id);
        if (ventaLoteRepository.existsByVenta_IdVendedorAndIdLoteAndVenta_Estado(
                lote.getIdVendedor(), lote.getId(), com.vendelote.enums.EstadoVenta.ACTIVA)) {
            throw new NegocioException("No se puede eliminar: el lote tiene una venta activa");
        }
        loteRepository.delete(lote);
    }

    /**
     * Cambio rápido de estado desde la tabla (switch "vendido"), sin venta.
     * Sirve para enterarse de ventas hechas fuera del sistema (otro vendedor
     * / oficina) y sacar el lote del inventario disponible.
     *
     * Regla: si el lote tiene UNA VENTA PROPIA ACTIVA, su estado lo gobierna
     * la venta — no se puede devolver a DISPONIBLE/RESERVADO por acá (hay que
     * anular la venta, que ya devuelve los lotes a DISPONIBLE).
     */
    /**
     * Separar (reservar) un lote por un plazo. Las separaciones propias NO
     * bloquean la venta (el vendedor puede cerrar con su cliente dentro del
     * plazo); las ajenas SÍ bloquean la venta.
     */
    @Transactional
    public Lote separar(Integer id, LoteSeparacionDTO dto) {
        Lote lote = buscar(id);
        if (tieneVentaActiva(lote)) {
            throw new NegocioException(
                    "El lote tiene una venta activa: anule la venta antes de separarlo");
        }
        if (dto.getFechaVence().isBefore(LocalDate.now())) {
            throw new NegocioException("La fecha de vencimiento ya pasó");
        }

        lote.setEstado(EstadoLote.RESERVADO);
        lote.setFechaVinceSeparacion(dto.getFechaVence());
        lote.setMontoSeparacion(dto.getMonto());
        lote.setSeparacionAjena(dto.isAjena());
        lote.setClienteSeparacion(limpiar(dto.getCliente()));
        lote.setNotaSeparacion(limpiar(dto.getNota()));
        lote.setMotivoEstado(null);
        return loteRepository.save(lote);
    }

    /** Libera una separación: el lote vuelve a DISPONIBLE. */
    @Transactional
    public Lote liberarSeparacion(Integer id) {
        Lote lote = buscar(id);
        if (tieneVentaActiva(lote)) {
            throw new NegocioException(
                    "El lote tiene una venta activa: anule la venta antes de liberarlo");
        }
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setFechaVinceSeparacion(null);
        lote.setMontoSeparacion(null);
        lote.setSeparacionAjena(false);
        lote.setClienteSeparacion(null);
        lote.setNotaSeparacion(null);
        lote.setMotivoEstado(null);
        return loteRepository.save(lote);
    }

    /** Lotes separados (RESERVADO) del vendedor, con días restantes y aviso de vencimiento. */
    @Transactional(readOnly = true)
    public List<Lote> listarSeparaciones() {
        Integer idV = TenantContext.idVendedorRequerido();
        Set<Integer> conVenta = lotesConVentaActiva(idV);
        LocalDate hoy = LocalDate.now();

        return loteRepository.findByIdVendedorOrderByFechaRegistroDesc(idV).stream()
                .peek(l -> l.setVentaActiva(conVenta.contains(l.getId())))
                .filter(l -> l.getEstado() == EstadoLote.RESERVADO)
                .sorted(Comparator.comparing(Lote::getFechaVinceSeparacion,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public Lote cambiarEstado(Integer id, LoteEstadoDTO dto) {
        Lote lote = buscar(id);
        EstadoLote nuevo = estadoDesde(dto.getEstado());

        boolean ventaActiva = tieneVentaActiva(lote);
        if (ventaActiva && nuevo != EstadoLote.VENDIDO) {
            throw new NegocioException(
                    "El lote tiene una venta activa: anule la venta para devolverlo a DISPONIBLE");
        }

        if (nuevo == EstadoLote.RESERVADO) {
            // RESERVADO solo se alcanza por la acción "Separar" (con fecha y monto).
            if (lote.getFechaVinceSeparacion() == null) {
                throw new NegocioException(
                        "Para separar un lote use la acción Separar (indica el plazo y el monto)");
            }
        } else {
            // Al salir de RESERVADO se limpian los datos de la separación.
            lote.setFechaVinceSeparacion(null);
            lote.setMontoSeparacion(null);
            lote.setSeparacionAjena(false);
            lote.setClienteSeparacion(null);
            lote.setNotaSeparacion(null);
        }

        lote.setEstado(nuevo);
        if (nuevo == EstadoLote.VENDIDO) {
            String motivo = dto.getMotivo() != null ? dto.getMotivo().trim() : null;
            lote.setMotivoEstado(motivo == null || motivo.isEmpty() ? null : motivo);
        } else {
            lote.setMotivoEstado(null); // el motivo aplica solo a VENDIDO
        }
        lote.setVentaActiva(ventaActiva);
        return loteRepository.save(lote);
    }

    @Transactional(readOnly = true)
    public Lote buscar(Integer id) {
        Lote lote = loteRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Lote no encontrado"));
        lote.setVentaActiva(tieneVentaActiva(lote));
        return lote;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** ¿El lote tiene una venta propia ACTIVA? (entonces solo la anulación lo libera). */
    private boolean tieneVentaActiva(Lote lote) {
        return ventaLoteRepository.existsByVenta_IdVendedorAndIdLoteAndVenta_Estado(
                lote.getIdVendedor(), lote.getId(), EstadoVenta.ACTIVA);
    }

    /** Ids de los lotes del vendedor con venta ACTIVA (evita una consulta por lote). */
    private Set<Integer> lotesConVentaActiva(Integer idVendedor) {
        return new HashSet<>(ventaLoteRepository.findDistinctIdLoteConVentaActiva(
                idVendedor, EstadoVenta.ACTIVA));
    }

    private void aplicar(Lote lote, LoteDTO dto, Empresa empresa, Proyecto proyecto) {
        lote.setIdVendedor(TenantContext.idVendedorRequerido());
        lote.setIdEmpresa(empresa.getId());
        lote.setIdProyecto(proyecto.getId());
        lote.setProyecto(proyecto.getNombre()); // copia sincronizada (facturación la usa)
        lote.setManzana(dto.getManzana().trim());
        lote.setNumero(dto.getNumero().trim());
        lote.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        lote.setAreaM2(dto.getAreaM2());
        lote.setPrecioM2(dto.getPrecioM2());
        lote.setPrecio(dto.getPrecio());
        lote.setInicial(dto.getInicial());
        lote.setNumCuotas(dto.getNumCuotas());
        lote.setSaldo(calcularSaldo(dto.getPrecio(), dto.getInicial()));
    }

    /**
     * Saldo financiable = max(0, precio − inicial). Misma fórmula que la vista
     * "registrar contrato" del sistema InmobiliariaIvan (actualizarSaldo).
     */
    private BigDecimal calcularSaldo(BigDecimal precio, BigDecimal inicial) {
        BigDecimal base = inicial != null ? inicial : BigDecimal.ZERO;
        BigDecimal saldo = precio.subtract(base);
        return saldo.signum() < 0 ? BigDecimal.ZERO : saldo;
    }

    private EstadoLote estadoDesde(String valor) {
        try {
            return EstadoLote.valueOf(valor.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Estado no válido: use DISPONIBLE, RESERVADO o VENDIDO");
        }
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
