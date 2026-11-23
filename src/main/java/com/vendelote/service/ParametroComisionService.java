package com.vendelote.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ParametroComisionDTO;
import com.vendelote.entity.ParametroComision;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ParametroComisionRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/** Parámetros de comisión del tenant + resolución del vigente (empresa > GENERAL). */
@Service
@RequiredArgsConstructor
public class ParametroComisionService {

    private final ParametroComisionRepository parametroRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public List<ParametroComision> listar() {
        return parametroRepository.findByIdVendedorOrderByIdDesc(TenantContext.idVendedorRequerido());
    }

    @Transactional
    public ParametroComision crear(ParametroComisionDTO dto) {
        validar(dto);
        ParametroComision parametro = new ParametroComision();
        parametro.setIdVendedor(TenantContext.idVendedorRequerido());
        aplicar(parametro, dto);
        return parametroRepository.save(parametro);
    }

    @Transactional
    public ParametroComision editar(Integer id, ParametroComisionDTO dto) {
        validar(dto);
        ParametroComision parametro = buscar(id);
        aplicar(parametro, dto);
        return parametroRepository.save(parametro);
    }

    @Transactional
    public void eliminar(Integer id) {
        parametroRepository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public ParametroComision buscar(Integer id) {
        return parametroRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Parámetro no encontrado"));
    }

    /**
     * Resuelve el parámetro vigente para una empresa: primero el específico de
     * la empresa; si no hay, el GENERAL (idEmpresa null). Se toma el más
     * reciente por fecha de inicio dentro de su ventana de vigencia.
     */
    @Transactional(readOnly = true)
    public ParametroComision resolver(Integer idEmpresa) {
        Integer idV = TenantContext.idVendedorRequerido();
        LocalDate hoy = LocalDate.now();

        if (idEmpresa != null) {
            ParametroComision porEmpresa = vigente(
                    parametroRepository.findByIdVendedorAndIdEmpresaOrderByFechaInicioVigenciaDesc(idV, idEmpresa), hoy);
            if (porEmpresa != null) {
                return porEmpresa;
            }
        }
        ParametroComision general = vigente(
                parametroRepository.findByIdVendedorAndIdEmpresaOrderByFechaInicioVigenciaDesc(idV, null), hoy);
        if (general == null) {
            throw new NegocioException(
                    "Configure primero los parámetros de comisión (alcance GENERAL o por empresa)");
        }
        return general;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ParametroComision vigente(List<ParametroComision> candidatos, LocalDate hoy) {
        return candidatos.stream() // ya vienen ordenados por fechaInicioVigencia desc
                .filter(p -> !p.getFechaInicioVigencia().isAfter(hoy))
                .filter(p -> p.getFechaFinVigencia() == null || !p.getFechaFinVigencia().isBefore(hoy))
                .findFirst()
                .orElse(null);
    }

    private void aplicar(ParametroComision parametro, ParametroComisionDTO dto) {
        Integer idEmpresa = dto.getIdEmpresa();
        if (idEmpresa != null) {
            // valida pertenencia al tenant
            idEmpresa = empresaService.buscar(idEmpresa).getId();
        }
        parametro.setIdEmpresa(idEmpresa);
        parametro.setPorcentajeComision(dto.getPorcentajeComision());
        parametro.setPorcentajeComisionContado(dto.getPorcentajeComisionContado());
        parametro.setAnticipoPorcentaje(dto.getAnticipoPorcentaje());
        parametro.setAnticipoMontoFijo(dto.getAnticipoMontoFijo());
        parametro.setMontoCuota(dto.getMontoCuota());
        parametro.setMesesEspera(dto.getMesesEspera());
        parametro.setFechaInicioVigencia(dto.getFechaInicioVigencia() != null
                ? dto.getFechaInicioVigencia() : LocalDate.now());
        parametro.setFechaFinVigencia(dto.getFechaFinVigencia());
    }

    private void validar(ParametroComisionDTO dto) {
        if (dto.getMontoCuota().signum() <= 0) {
            throw new NegocioException("El monto de cuota debe ser mayor a 0");
        }
        if (dto.getMesesEspera() == null || dto.getMesesEspera() < 0) {
            throw new NegocioException("Los meses de espera no pueden ser negativos");
        }
        if (dto.getFechaInicioVigencia() != null && dto.getFechaFinVigencia() != null
                && dto.getFechaFinVigencia().isBefore(dto.getFechaInicioVigencia())) {
            throw new NegocioException("La fecha fin no puede ser anterior a la fecha inicio");
        }
    }
}
