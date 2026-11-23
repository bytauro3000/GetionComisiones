package com.vendelote.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.SocioDTO;
import com.vendelote.entity.Socio;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ComisionRepartoRepository;
import com.vendelote.repository.SocioRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/** CRUD de socios del vendedor (tenant). No inician sesión. */
@Service
@RequiredArgsConstructor
public class SocioService {

    private final SocioRepository socioRepository;
    private final ComisionRepartoRepository repartoRepository;

    @Transactional(readOnly = true)
    public List<SocioDTO> listar(Boolean soloActivos) {
        Integer idV = TenantContext.idVendedorRequerido();
        List<Socio> socios = Boolean.TRUE.equals(soloActivos)
                ? socioRepository.findByIdVendedorAndActivoTrueOrderByNombresAsc(idV)
                : socioRepository.findByIdVendedorOrderByNombresAsc(idV);
        return socios.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Socio buscar(Integer id) {
        return socioRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Socio no encontrado"));
    }

    @Transactional
    public SocioDTO crear(SocioDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Socio socio = new Socio();
        socio.setIdVendedor(idV);
        aplicar(socio, dto);
        return toDTO(socioRepository.save(socio));
    }

    @Transactional
    public SocioDTO editar(Integer id, SocioDTO dto) {
        Socio socio = buscar(id);
        aplicar(socio, dto);
        return toDTO(socioRepository.save(socio));
    }

    @Transactional
    public void eliminar(Integer id) {
        Socio socio = buscar(id);
        if (repartoRepository.existsByIdSocioAndIdVendedor(socio.getId(), socio.getIdVendedor())) {
            throw new NegocioException(
                    "No se puede eliminar: el socio tiene comisiones asociadas. Desactívelo en su lugar.");
        }
        socioRepository.delete(socio);
    }

    private void aplicar(Socio socio, SocioDTO dto) {
        socio.setNombres(dto.getNombres().trim());
        socio.setApellidos(limpiar(dto.getApellidos()));
        socio.setDocumento(limpiar(dto.getDocumento()));
        socio.setTelefono(limpiar(dto.getTelefono()));
        socio.setActivo(dto.getActivo() == null || dto.getActivo());
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private SocioDTO toDTO(Socio s) {
        SocioDTO dto = new SocioDTO();
        dto.setId(s.getId());
        dto.setNombres(s.getNombres());
        dto.setApellidos(s.getApellidos());
        dto.setDocumento(s.getDocumento());
        dto.setTelefono(s.getTelefono());
        dto.setActivo(s.isActivo());
        dto.setFechaRegistro(s.getFechaRegistro());
        return dto;
    }
}
