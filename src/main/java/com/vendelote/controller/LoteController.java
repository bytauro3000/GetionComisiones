package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.vendelote.dto.LoteDTO;
import com.vendelote.dto.LoteEstadoDTO;
import com.vendelote.dto.LoteSeparacionDTO;
import com.vendelote.entity.Lote;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.LoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Lotes del vendedor autenticado (tenant). */
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;
    private final AuditoriaService auditoriaService;

    /** Listado con filtros opcionales: ?idEmpresa=&idProyecto=&estado=DISPONIBLE|RESERVADO|VENDIDO */
    @GetMapping
    public ResponseEntity<List<Lote>> listar(@RequestParam(required = false) Integer idEmpresa,
                                             @RequestParam(required = false) Integer idProyecto,
                                             @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(loteService.listar(idEmpresa, idProyecto, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lote> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(loteService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Lote> crear(@Valid @RequestBody LoteDTO dto) {
        Lote creado = loteService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Lote", creado.getId(),
                "Lote creado: " + creado.getProyecto() + " Mz." + creado.getManzana()
                        + " Lt." + creado.getNumero());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lote> editar(@PathVariable Integer id, @Valid @RequestBody LoteDTO dto) {
        Lote actualizado = loteService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Lote", id,
                "Lote editado: " + actualizado.getProyecto() + " Mz." + actualizado.getManzana()
                        + " Lt." + actualizado.getNumero());
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Cambio rápido de estado desde la tabla (switch "vendido").
     * Body: { "estado": "VENDIDO|DISPONIBLE|RESERVADO", "motivo": "..." }.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Lote> cambiarEstado(@PathVariable Integer id,
                                              @Valid @RequestBody LoteEstadoDTO dto) {
        Lote actualizado = loteService.cambiarEstado(id, dto);
        String detalle = "Estado → " + actualizado.getEstado()
                + ": Mz." + actualizado.getManzana() + " Lt." + actualizado.getNumero()
                + (actualizado.getMotivoEstado() != null
                        ? " | motivo: " + actualizado.getMotivoEstado() : "");
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Lote", id, detalle);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Separar (reservar) un lote por un plazo. Las separaciones propias no
     * bloquean la venta; las ajenas sí.
     */
    @PatchMapping("/{id}/separacion")
    public ResponseEntity<Lote> separar(@PathVariable Integer id,
                                        @Valid @RequestBody LoteSeparacionDTO dto) {
        Lote actualizado = loteService.separar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Lote", id,
                "Lote separado hasta " + actualizado.getFechaVinceSeparacion()
                        + (actualizado.isSeparacionAjena() ? " (separación ajena)" : " (separación propia)"));
        return ResponseEntity.ok(actualizado);
    }

    /** Libera la separación: el lote vuelve a DISPONIBLE. */
    @DeleteMapping("/{id}/separacion")
    public ResponseEntity<Lote> liberarSeparacion(@PathVariable Integer id) {
        Lote actualizado = loteService.liberarSeparacion(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Lote", id,
                "Separación liberada: Mz." + actualizado.getManzana() + " Lt." + actualizado.getNumero());
        return ResponseEntity.ok(actualizado);
    }

    /** Lotes separados (RESERVADO) del vendedor con su fecha de vencimiento. */
    @GetMapping("/separaciones")
    public ResponseEntity<List<Lote>> listarSeparaciones() {
        return ResponseEntity.ok(loteService.listarSeparaciones());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Lote lote = loteService.buscar(id);
        String detalle = "Lote eliminado: " + lote.getProyecto() + " Mz." + lote.getManzana()
                + " Lt." + lote.getNumero();
        loteService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Lote", id, detalle);
        return ResponseEntity.noContent().build();
    }
}
