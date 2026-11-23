package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.LiquidacionResumenDTO;
import com.vendelote.dto.LiquidacionSocioDTO;
import com.vendelote.dto.SaldoSocioDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.LiquidacionSocioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Liquidaciones (pagos) a socios y saldos pendientes. Tenant-scoped. */
@RestController
@RequestMapping("/api/liquidaciones-socios")
@RequiredArgsConstructor
public class LiquidacionSocioController {

    private final LiquidacionSocioService liquidacionService;
    private final AuditoriaService auditoriaService;

    @GetMapping("/saldos")
    public ResponseEntity<List<SaldoSocioDTO>> saldos() {
        return ResponseEntity.ok(liquidacionService.saldos());
    }

    @GetMapping("/sugerir")
    public ResponseEntity<LiquidacionResumenDTO> sugerir(@RequestParam Integer idSocio) {
        return ResponseEntity.ok(liquidacionService.sugerir(idSocio));
    }

    @GetMapping
    public ResponseEntity<List<LiquidacionSocioDTO>> listar(
            @RequestParam(required = false) Integer idSocio) {
        return ResponseEntity.ok(liquidacionService.listar(idSocio));
    }

    @PostMapping
    public ResponseEntity<LiquidacionSocioDTO> crear(@Valid @RequestBody LiquidacionSocioDTO dto) {
        LiquidacionSocioDTO creada = liquidacionService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "LiquidacionSocio", creada.getId(),
                "Liquidación a socio " + creada.getSocioNombre() + " | neto: " + creada.getMontoNeto());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        liquidacionService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "LiquidacionSocio", id,
                "Liquidación eliminada");
        return ResponseEntity.noContent().build();
    }
}
