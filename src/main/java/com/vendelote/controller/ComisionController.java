package com.vendelote.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.CobrarPagosDTO;
import com.vendelote.dto.ComisionListadoDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ComisionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Comisiones y cobros del vendedor (tenant).
 * POST /{id}/pagos → cobrar 1..N pagos; POST /{id}/pagos/{idPago}/revertir → deshacer.
 */
@RestController
@RequestMapping("/api/comisiones")
@RequiredArgsConstructor
public class ComisionController {

    private final ComisionService comisionService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<ComisionListadoDTO>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(comisionService.listar(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComisionListadoDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(comisionService.obtener(id));
    }

    @PostMapping("/{id}/pagos")
    public ResponseEntity<ComisionListadoDTO> cobrar(@PathVariable Integer id,
                                                     @Valid @RequestBody CobrarPagosDTO dto) {
        ComisionListadoDTO resultado = comisionService.cobrar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Comision", id,
                "Cobro registrado: " + dto.getPagos().size() + " pago(s) | fecha: " + dto.getFechaPago()
                        + (dto.getFormaPago() != null ? " | " + dto.getFormaPago() : ""));
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{id}/pagos/{idPago}/revertir")
    public ResponseEntity<ComisionListadoDTO> revertir(@PathVariable Integer id,
                                                       @PathVariable Integer idPago) {
        ComisionListadoDTO resultado = comisionService.revertirCobro(id, idPago);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Comision", id,
                "Cobro revertido: pago #" + idPago);
        return ResponseEntity.ok(resultado);
    }
}
