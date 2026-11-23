package com.vendelote.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.AuditoriaResponseDTO;
import com.vendelote.dto.PaginaDTO;
import com.vendelote.entity.Auditoria;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;

/** Historial de auditoría — SOLO ROLE_ADMINISTRADOR (regla en SecurityConfig). */
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    @GetMapping
    public ResponseEntity<PaginaDTO<AuditoriaResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            @RequestParam(required = false) AccionAuditoria accion,
            @RequestParam(required = false) String buscar) {

        int tamanoSeguro = Math.min(Math.max(tamano, 1), 100);
        Pageable pageable = PageRequest.of(pagina, tamanoSeguro);

        Page<Auditoria> page;
        boolean hayBuscar = buscar != null && !buscar.isBlank();
        boolean hayAccion = accion != null;

        if (hayAccion && hayBuscar) {
            page = auditoriaRepository.buscarPorAccionYUsuario(accion, buscar.trim(), pageable);
        } else if (hayAccion) {
            page = auditoriaRepository.findByAccionOrderByFechaHoraDesc(accion, pageable);
        } else if (hayBuscar) {
            page = auditoriaRepository.buscarPorUsuario(buscar.trim(), pageable);
        } else {
            page = auditoriaRepository.findAllByOrderByFechaHoraDesc(pageable);
        }

        PaginaDTO<AuditoriaResponseDTO> dto = new PaginaDTO<>(
                page.getContent().stream().map(this::toDTO).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());

        return ResponseEntity.ok(dto);
    }

    private AuditoriaResponseDTO toDTO(Auditoria a) {
        return new AuditoriaResponseDTO(
                a.getIdAuditoria(), a.getFechaHora(), a.getUsuarioEmail(),
                a.getUsuarioRol(), a.getAccion().name(), a.getEntidad(),
                a.getIdEntidad(), a.getDescripcion(), a.getIpOrigen());
    }
}
