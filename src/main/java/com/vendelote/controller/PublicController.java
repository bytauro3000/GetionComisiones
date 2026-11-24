package com.vendelote.controller;

import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de infraestructura.
 * /api/public/ping sirve para mantener despierto el servicio en Render (plan free),
 * que se duerme tras ~15 minutos de inactividad. Un cron externo puede llamarlo
 * cada 10-14 minutos; también acepta HEAD para que el monitor no reciba cuerpo.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @RequestMapping(
            value = "/ping",
            method = { RequestMethod.GET, RequestMethod.HEAD },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "status", "ok",
                        "servicio", "Closure"
                ));
    }
}
