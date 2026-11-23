package com.vendelote.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.PerfilDTO;
import com.vendelote.service.UsuarioService;

import lombok.RequiredArgsConstructor;

/**
 * Perfil del usuario autenticado (cualquier rol autenticado).
 * Ruta fuera de /api/auth/** porque esa zona es pública: aquí sí exige sesión.
 */
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PerfilDTO> perfil() {
        return ResponseEntity.ok(usuarioService.perfilActual());
    }
}
