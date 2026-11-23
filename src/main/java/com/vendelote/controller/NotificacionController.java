package com.vendelote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.NotificacionDTO;
import com.vendelote.service.NotificacionService;

import lombok.RequiredArgsConstructor;

/** Notificaciones internas del vendedor (tenant). */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listar(
            @RequestParam(defaultValue = "false") boolean soloNoLeidas) {
        return ResponseEntity.ok(notificacionService.listar(soloNoLeidas));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas()));
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Integer id) {
        notificacionService.marcarLeida(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodas() {
        notificacionService.marcarTodasLeidas();
        return ResponseEntity.noContent().build();
    }
}
