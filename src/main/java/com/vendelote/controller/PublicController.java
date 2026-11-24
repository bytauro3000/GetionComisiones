package com.vendelote.controller;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de infraestructura.
 * /api/public/ping sirve para mantener despierto el servicio en Render (plan free),
 * que se duerme tras ~15 minutos de inactividad. Un cron externo puede llamarlo
 * cada 10-14 minutos.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "servicio", "VendeLote",
                "hora", OffsetDateTime.now().toString()
        ));
    }
}
