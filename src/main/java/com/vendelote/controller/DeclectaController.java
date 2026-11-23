package com.vendelote.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.DniConsultaDTO;
import com.vendelote.dto.MensajeErrorDTO;
import com.vendelote.dto.RucConsultaDTO;
import com.vendelote.service.DeclectaService;

import lombok.RequiredArgsConstructor;

/**
 * Proxy autenticado a Declecta: autocompletado de DNI (RENIEC) y RUC (SUNAT)
 * en los formularios. El token vive en el backend; el frontend nunca lo ve.
 */
@RestController
@RequestMapping("/api/decolecta")
@RequiredArgsConstructor
public class DeclectaController {

    private final DeclectaService declectaService;

    @GetMapping("/dni/{dni}")
    public ResponseEntity<?> consultarDni(@PathVariable String dni) {
        if (!dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body(new MensajeErrorDTO("El DNI debe tener 8 dígitos"));
        }
        return declectaService.buscarDni(dni)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MensajeErrorDTO("DNI no encontrado o servicio de consulta no disponible")));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<?> consultarRuc(@PathVariable String ruc) {
        if (!ruc.matches("\\d{11}")) {
            return ResponseEntity.badRequest().body(new MensajeErrorDTO("El RUC debe tener 11 dígitos"));
        }
        return declectaService.buscarRuc(ruc)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MensajeErrorDTO("RUC no encontrado o servicio de consulta no disponible")));
    }
}
