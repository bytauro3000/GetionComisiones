package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.ComprobanteCrearDTO;
import com.vendelote.dto.ComprobanteListadoDTO;
import com.vendelote.dto.PagoDisponibleDTO;
import com.vendelote.entity.Comprobante;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ComprobanteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Comprobantes electrónicos del vendedor (tenant).
 * La emisión usa las credenciales api-sunat del propio tenant; el PDF se
 * sirve por proxy para que el frontend nunca vea las claves.
 */
@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<ComprobanteListadoDTO>> listar() {
        return ResponseEntity.ok(comprobanteService.listar());
    }

    @GetMapping("/pagos-disponibles")
    public ResponseEntity<List<PagoDisponibleDTO>> pagosDisponibles() {
        return ResponseEntity.ok(comprobanteService.pagosDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteListadoDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(comprobanteService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ComprobanteListadoDTO> emitir(
            @Valid @RequestBody ComprobanteCrearDTO dto) {
        ComprobanteListadoDTO resultado = comprobanteService.emitir(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Comprobante", resultado.getId(),
                "Factura emitida: " + resultado.getSerie() + "-" + resultado.getCorrelativo()
                        + " | empresa #" + resultado.getIdEmpresa()
                        + " | total " + resultado.getTotal()
                        + " | pagos: " + dto.getPagos().size());
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(resultado);
    }

    @PostMapping("/{id}/sincronizar")
    public ResponseEntity<ComprobanteListadoDTO> sincronizar(@PathVariable Integer id) {
        ComprobanteListadoDTO resultado = comprobanteService.sincronizar(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Comprobante", id,
                "Estado sincronizado con SUNAT: " + resultado.getEstado());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        Comprobante comprobante = comprobanteService.buscar(id);
        byte[] pdf = comprobanteService.descargarPdf(id);
        String nombre = comprobanteService.nombreArchivoPdf(comprobante);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
