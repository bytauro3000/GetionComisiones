package com.vendelote.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.IntegracionSunatDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.IntegracionSunatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Configuración de facturación electrónica por vendedor (tenant).
 * GET nunca devuelve api_key/api_secret (solo indicadores "configurado");
 * en el PUT, claves vacías = conservar las existentes.
 */
@RestController
@RequestMapping("/api/integracion-sunat")
@RequiredArgsConstructor
public class IntegracionSunatController {

    private final IntegracionSunatService integracionService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<IntegracionSunatDTO> obtener() {
        return ResponseEntity.ok(integracionService.obtener());
    }

    @PutMapping
    public ResponseEntity<IntegracionSunatDTO> guardar(
            @Valid @RequestBody IntegracionSunatDTO dto) {
        IntegracionSunatDTO resultado = integracionService.guardar(dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "IntegracionSunat", null,
                "Configuración SUNAT actualizada | serie: " + resultado.getSerieFactura()
                        + " | url: " + (resultado.getApiUrl() != null ? resultado.getApiUrl() : "default")
                        + " | detracción: " + (resultado.isAplicarDetraccion() ? "activada" : "desactivada"));
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/probar")
    public ResponseEntity<Map<String, Boolean>> probar() {
        boolean conectado = integracionService.probar();
        return ResponseEntity.ok(Map.of("conectado", conectado));
    }
}
