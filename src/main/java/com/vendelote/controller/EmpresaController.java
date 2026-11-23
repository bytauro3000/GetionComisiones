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
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.EmpresaDTO;
import com.vendelote.entity.Empresa;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.EmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Empresas del vendedor autenticado (tenant). */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(empresaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody EmpresaDTO dto) {
        Empresa creada = empresaService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Empresa", creada.getId(),
                "Empresa creada: " + creada.getRuc() + " - " + creada.getRazonSocial());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> editar(@PathVariable Integer id, @Valid @RequestBody EmpresaDTO dto) {
        Empresa actualizada = empresaService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Empresa", id,
                "Empresa editada: " + actualizada.getRuc() + " - " + actualizada.getRazonSocial());
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Empresa empresa = empresaService.buscar(id);
        String detalle = "Empresa eliminada: " + empresa.getRuc() + " - " + empresa.getRazonSocial();
        empresaService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Empresa", id, detalle);
        return ResponseEntity.noContent().build();
    }
}
