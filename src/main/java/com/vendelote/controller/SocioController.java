package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.SocioDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.SocioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Socios con quienes se comparte la comisión (referencias del vendedor,
 * sin login). Solo del tenant autenticado.
 */
@RestController
@RequestMapping("/api/socios")
@RequiredArgsConstructor
public class SocioController {

    private final SocioService socioService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<SocioDTO>> listar(
            @RequestParam(required = false) Boolean soloActivos) {
        return ResponseEntity.ok(socioService.listar(soloActivos));
    }

    @PostMapping
    public ResponseEntity<SocioDTO> crear(@Valid @RequestBody SocioDTO dto) {
        SocioDTO creado = socioService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Socio", creado.getId(),
                "Socio creado: " + creado.getNombres());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocioDTO> editar(@PathVariable Integer id,
                                           @Valid @RequestBody SocioDTO dto) {
        SocioDTO actualizado = socioService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Socio", id,
                "Socio editado: " + actualizado.getNombres());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        socioService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Socio", id, "Socio eliminado");
        return ResponseEntity.noContent().build();
    }
}
