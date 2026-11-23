package com.vendelote.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.DetalleProformaDTO;
import com.vendelote.dto.ProformaDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Lote;
import com.vendelote.entity.Proforma;
import com.vendelote.entity.ProformaDetalle;
import com.vendelote.entity.Secuencia;
import com.vendelote.enums.EstadoLote;
import com.vendelote.enums.EstadoProforma;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.ProformaRepository;
import com.vendelote.repository.SecuenciaRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Proformas comerciales del tenant: CRUD con correlativo PF-{anio}-{0001}
 * por empresa y año. No modifica el estado de los lotes (son inventario).
 */
@Service
@RequiredArgsConstructor
public class ProformaService {

    private final ProformaRepository proformaRepository;
    private final SecuenciaRepository secuenciaRepository;
    private final LoteRepository loteRepository;
    private final ClienteService clienteService;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public List<Proforma> listar() {
        return proformaRepository.findByIdVendedorOrderByFechaDesc(TenantContext.idVendedorRequerido());
    }

    @Transactional(readOnly = true)
    public Proforma buscar(Integer id) {
        return proformaRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Proforma no encontrada"));
    }

    @Transactional
    public Proforma crear(ProformaDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Empresa empresa = empresaService.buscar(dto.getIdEmpresa());
        Cliente cliente = clienteService.buscar(dto.getIdCliente());

        Proforma proforma = new Proforma();
        proforma.setIdVendedor(idV);
        proforma.setIdEmpresa(empresa.getId());
        proforma.setIdCliente(cliente.getId());
        proforma.setObservaciones(dto.getObservaciones() != null && !dto.getObservaciones().isBlank()
                ? dto.getObservaciones().trim() : null);
        aplicarDetalles(proforma, dto, idV); // valida lotes ANTES de consumir correlativo
        proforma.setNumero(siguienteNumero(empresa.getId(), LocalDate.now().getYear()));
        return proformaRepository.save(proforma);
    }

    @Transactional
    public Proforma editar(Integer id, ProformaDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Proforma proforma = buscar(id);
        if (proforma.getEstado() != EstadoProforma.EMITIDA) {
            throw new NegocioException("Solo se pueden editar proformas emitidas");
        }
        empresaService.buscar(dto.getIdEmpresa());
        Cliente cliente = clienteService.buscar(dto.getIdCliente());

        proforma.setIdEmpresa(dto.getIdEmpresa());
        proforma.setIdCliente(cliente.getId());
        proforma.setObservaciones(dto.getObservaciones() != null && !dto.getObservaciones().isBlank()
                ? dto.getObservaciones().trim() : null);
        aplicarDetalles(proforma, dto, idV); // borra los viejos y agrega los nuevos (orphanRemoval)
        return proformaRepository.save(proforma);
    }

    @Transactional
    public void eliminar(Integer id) {
        Proforma proforma = buscar(id);
        proformaRepository.delete(proforma);
    }

    /** EMITIDA → ACEPTADA/ANULADA; ACEPTADA → ANULADA. ANULADA es terminal. */
    @Transactional
    public Proforma cambiarEstado(Integer id, String estadoNuevo) {
        Proforma proforma = buscar(id);
        EstadoProforma destino;
        try {
            destino = EstadoProforma.valueOf(estadoNuevo.trim().toUpperCase(java.util.Locale.ROOT));
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

    // ── Detalles y correlativo ────────────────────────────────────────────────

    private void aplicarDetalles(Proforma proforma, ProformaDTO dto, Integer idV) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new NegocioException("Agregue al menos un lote a la proforma");
        }
        List<ProformaDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleProformaDTO item : dto.getDetalles()) {
            Lote lote = loteRepository.findByIdAndIdVendedor(item.getIdLote(), idV)
                    .orElseThrow(() -> new NegocioException("Lote no encontrado"));
            if (!lote.getIdEmpresa().equals(dto.getIdEmpresa())) {
                throw new NegocioException("El lote " + lote.getManzana() + "-" + lote.getNumero()
                        + " no pertenece a esa empresa");
            }
            if (lote.getEstado() == EstadoLote.VENDIDO) {
                throw new NegocioException("El lote " + lote.getManzana() + "-" + lote.getNumero()
                        + " ya está vendido");
            }

            ProformaDetalle d = new ProformaDetalle();
            d.setProforma(proforma);
            d.setIdLote(lote.getId());
            d.setDescripcion(descripcionLote(lote));
            d.setAreaM2(lote.getAreaM2());
            d.setPrecioUnitario(item.getPrecioUnitario());
            detalles.add(d);
            total = total.add(item.getPrecioUnitario());
        }

        proforma.getDetalles().clear();
        proforma.getDetalles().addAll(detalles);
        proforma.setTotal(total);
    }

    private String descripcionLote(Lote lote) {
        String base = (lote.getProyecto() != null && !lote.getProyecto().isBlank()
                ? lote.getProyecto() + " · " : "")
                + "Mz " + lote.getManzana() + " · N° " + lote.getNumero();
        return base.length() > 200 ? base.substring(0, 200) : base;
    }

    /** PF-{anio}-{0001}: incrementa la secuencia de la empresa con bloqueo de fila. */
    private String siguienteNumero(Integer idEmpresa, int anio) {
        Secuencia secuencia = secuenciaRepository.bloquear(idEmpresa, anio).orElse(null);
        if (secuencia == null) {
            secuencia = new Secuencia();
            secuencia.setIdEmpresa(idEmpresa);
            secuencia.setAnio(anio);
            secuencia.setUltimoNumero(1);
        } else {
            secuencia.setUltimoNumero(secuencia.getUltimoNumero() + 1);
        }
        secuencia = secuenciaRepository.save(secuencia);
        return String.format("PF-%d-%04d", anio, secuencia.getUltimoNumero());
    }
}
