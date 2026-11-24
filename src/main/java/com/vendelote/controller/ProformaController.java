package com.vendelote.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.ProformaDTO;
import com.vendelote.dto.ProformaSeguimientoDTO;
import com.vendelote.entity.Proforma;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ProformaPdfService;
import com.vendelote.service.ProformaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Proformas del vendedor autenticado (tenant): CRUD + cambio de estado. */
@RestController
@RequestMapping("/api/proformas")
@RequiredArgsConstructor
public class ProformaController {

    private final ProformaService proformaService;
    private final ProformaPdfService proformaPdfService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Proforma>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity.ok(proformaService.listar(fechaDesde, fechaHasta));
    }

    /** Seguimiento: clientes/prospectos que solicitaron proforma en el rango. */
    @GetMapping("/seguimiento")
    public ResponseEntity<List<ProformaSeguimientoDTO>> seguimiento(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        return ResponseEntity.ok(proformaService.seguimiento(fechaDesde, fechaHasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proforma> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(proformaService.buscar(id));
    }

    /** PDF de la proforma (para descargar o compartir por WhatsApp). */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Integer id) {
        Proforma proforma = proformaService.buscar(id);
        byte[] pdf = proformaPdfService.generar(proforma);
        String nombre = "Proforma-" + (proforma.getNumero() != null ? proforma.getNumero() : id) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * PDF por enlace público (sin login) para enviarlo por WhatsApp.
     * El token es un UUID no adivinable guardado en la proforma.
     */
    @GetMapping("/pdf-publico/{token}")
    public ResponseEntity<byte[]> pdfPublico(@PathVariable String token) {
        Proforma proforma = proformaService.buscarPorTokenPublico(token);
        byte[] pdf = proformaPdfService.generar(proforma);
        String nombre = "Proforma-" + (proforma.getNumero() != null ? proforma.getNumero() : "doc") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping
    public ResponseEntity<Proforma> crear(@Valid @RequestBody ProformaDTO dto) {
        Proforma creada = proformaService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Proforma", creada.getId(),
                "Proforma creada: " + creada.getNumero());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proforma> editar(@PathVariable Integer id, @Valid @RequestBody ProformaDTO dto) {
        Proforma actualizada = proformaService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proforma", id,
                "Proforma editada: " + actualizada.getNumero());
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Proforma proforma = proformaService.buscar(id);
        String detalle = "Proforma eliminada: " + proforma.getNumero();
        proformaService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Proforma", id, detalle);
        return ResponseEntity.noContent().build();
    }

    /** Cambia el estado (EMITIDA → ACEPTADA/ANULADA; ACEPTADA → ANULADA). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Proforma> cambiarEstado(@PathVariable Integer id,
                                                  @RequestParam String estado) {
        Proforma actualizada = proformaService.cambiarEstado(id, estado);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Proforma", id,
                "Proforma " + actualizada.getNumero() + " → " + actualizada.getEstado());
        return ResponseEntity.ok(actualizada);
    }
}
