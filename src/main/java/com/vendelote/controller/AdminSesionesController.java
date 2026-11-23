package com.vendelote.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.SesionResumenDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.SesionActivaService;

import lombok.RequiredArgsConstructor;

/** Panel de administración: sesiones activas — SOLO ROLE_ADMINISTRADOR. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
@RequiredArgsConstructor
public class AdminSesionesController {

    private final SesionActivaService sesionActivaService;
    private final AuditoriaService auditoriaService;

    @GetMapping("/sesiones")
    public ResponseEntity<SesionResumenDTO> resumenSesiones() {
        return ResponseEntity.ok(sesionActivaService.obtenerResumen());
    }

    /** Cierra a la fuerza una sesión activa desde el panel. */
    @PostMapping("/sesiones/{id}/cerrar")
    public ResponseEntity<Map<String, String>> cerrarSesion(@PathVariable Long id) {
        sesionActivaService.cerrarSesion(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "SesionActiva", null,
                "Sesión cerrada por administración: id=" + id);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada"));
    }
}
