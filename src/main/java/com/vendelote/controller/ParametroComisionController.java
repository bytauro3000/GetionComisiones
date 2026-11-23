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

import com.vendelote.dto.ParametroComisionDTO;
import com.vendelote.entity.ParametroComision;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ParametroComisionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Parámetros de comisión del vendedor (tenant). */
@RestController
@RequestMapping("/api/parametros-comision")
@RequiredArgsConstructor
public class ParametroComisionController {

    private final ParametroComisionService parametroService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<ParametroComision>> listar() {
        return ResponseEntity.ok(parametroService.listar());
    }

    /** Parámetro que se aplicaría hoy (?idEmpresa= para alcance de empresa). */
    @GetMapping("/vigente")
    public ResponseEntity<ParametroComision> vigente(@RequestParam(required = false) Integer idEmpresa) {
        return ResponseEntity.ok(parametroService.resolver(idEmpresa));
    }

    @PostMapping
    public ResponseEntity<ParametroComision> crear(@Valid @RequestBody ParametroComisionDTO dto) {
        ParametroComision creado = parametroService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "ParametroComicion", creado.getId(),
                "Parámetro creado: " + creado.getPorcentajeComision() + " (empresa: "
                        + (creado.getIdEmpresa() == null ? "GENERAL" : creado.getIdEmpresa()) + ")");
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametroComision> editar(@PathVariable Integer id,
                                                    @Valid @RequestBody ParametroComisionDTO dto) {
        ParametroComision actualizado = parametroService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "ParametroComicion", id,
                "Parámetro editado: " + actualizado.getPorcentajeComision());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        parametroService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "ParametroComicion", id,
                "Parámetro eliminado");
        return ResponseEntity.noContent().build();
    }
}
