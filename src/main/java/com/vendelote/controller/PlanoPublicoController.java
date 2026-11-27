package com.vendelote.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.PlanoPublicoDTO;
import com.vendelote.service.PlanoPublicoService;

import lombok.RequiredArgsConstructor;

/**
 * Enlace público de solo lectura del plano. El token es opaco y no permite
 * crear, editar ni vender lotes.
 */
@RestController
@RequestMapping("/api/public/planos")
@RequiredArgsConstructor
public class PlanoPublicoController {

    private final PlanoPublicoService planoPublicoService;

    @GetMapping("/{token}")
    public ResponseEntity<PlanoPublicoDTO> obtener(@PathVariable String token) {
        return planoPublicoService.obtenerPorToken(token)
                .map(plano -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore())
                        .body(plano))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
