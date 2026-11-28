package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.VentaCrearDTO;
import com.vendelote.dto.VentaDetalleDTO;
import com.vendelote.dto.VentaListadoDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Ventas del vendedor autenticado (tenant). Al crear se genera la comisión y
 * su plan de pagos automáticos.
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<VentaListadoDTO>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaDetalleDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<VentaDetalleDTO> crear(@Valid @RequestBody VentaCrearDTO dto) {
        VentaDetalleDTO creada = ventaService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Venta", creada.getId(),
                "Venta creada: " + creada.getLoteEtiqueta() + " | " + creada.getModalidad()
                        + " | USD " + creada.getPrecioVenta()
                        + " | comisión: " + (creada.getComision() != null
                                ? creada.getComision().getMontoTotal() : "-"));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<VentaDetalleDTO> aprobar(@PathVariable Integer id) {
        VentaDetalleDTO aprobada = ventaService.aprobar(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Venta", id,
                "Venta aprobada: " + aprobada.getLoteEtiqueta());
        return ResponseEntity.ok(aprobada);
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<VentaDetalleDTO> rechazar(@PathVariable Integer id) {
        VentaDetalleDTO rechazada = ventaService.rechazar(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Venta", id,
                "Venta rechazada: " + rechazada.getLoteEtiqueta());
        return ResponseEntity.ok(rechazada);
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<VentaDetalleDTO> anular(@PathVariable Integer id) {
        VentaDetalleDTO anulada = ventaService.anular(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Venta", id,
                "Venta anulada: " + anulada.getLoteEtiqueta());
        return ResponseEntity.ok(anulada);
    }
}
