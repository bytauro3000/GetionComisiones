package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vendelote.dto.ProyectoDTO;
import com.vendelote.entity.Proyecto;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ProyectoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Proyectos del vendedor autenticado (tenant) + subida/lectura del plano. */
@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Proyecto>> listar() {
        return ResponseEntity.ok(proyectoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(proyectoService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Proyecto> crear(@Valid @RequestBody ProyectoDTO dto) {
        Proyecto creado = proyectoService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Proyecto", creado.getId(),
                "Proyecto creado: " + creado.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> editar(@PathVariable Integer id, @Valid @RequestBody ProyectoDTO dto) {
        Proyecto actualizado = proyectoService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proyecto", id,
                "Proyecto editado: " + actualizado.getNombre());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Proyecto proyecto = proyectoService.buscar(id);
        String detalle = "Proyecto eliminado: " + proyecto.getNombre();
        proyectoService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Proyecto", id, detalle);
        return ResponseEntity.noContent().build();
    }

    // ── Plano ─────────────────────────────────────────────────────────────────

    /** Sube (o reemplaza) el plano del proyecto: JPG, PNG, WebP o PDF, máx 10 MB. */
    @PostMapping(value = "/{id}/plano", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Proyecto> subirPlano(@PathVariable Integer id,
                                               @RequestPart("archivo") MultipartFile archivo) {
        proyectoService.guardarPlano(id, archivo);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proyecto", id,
                "Plano subido/reemplazado");
        return ResponseEntity.ok(proyectoService.buscar(id));
    }

    /** Sirve el plano con autenticación (tenant). El frontend lo pide como blob. */
    @GetMapping("/{id}/plano")
    public ResponseEntity<byte[]> obtenerPlano(@PathVariable Integer id) {
        java.util.Optional<ProyectoService.PlanoRecurso> plano = proyectoService.recursoPlano(id);
        if (plano.isEmpty()) {
            // Sin plano o archivo perdido (disco efímero): 204 en vez de 400.
            return ResponseEntity.noContent().build();
        }
        ProyectoService.PlanoRecurso recurso = plano.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(recurso.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"plano-" + id + "\"")
                .body(recurso.bytes());
    }

    @DeleteMapping("/{id}/plano")
    public ResponseEntity<Void> borrarPlano(@PathVariable Integer id) {
        proyectoService.borrarPlano(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proyecto", id,
                "Plano eliminado");
        return ResponseEntity.noContent().build();
    }

    /**
     * Migra los planos guardados en disco local del tenant a Cloudinary.
     * Solo tiene efecto si las variables de entorno de Cloudinary están seteadas.
     */
    @PostMapping("/planos/migrar-cloudinary")
    public ResponseEntity<java.util.Map<String, Object>> migrarPlanosACloudinary() {
        int migrados = proyectoService.migrarPlanosACloudinary();
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proyecto", null,
                "Migración de planos a Cloudinary: " + migrados + " archivo(s)");
        return ResponseEntity.ok(java.util.Map.of("migrados", migrados));
    }
}
