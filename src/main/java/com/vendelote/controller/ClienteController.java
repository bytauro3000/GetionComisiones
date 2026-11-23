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

import com.vendelote.dto.ClienteDTO;
import com.vendelote.entity.Cliente;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Clientes del vendedor autenticado (tenant). */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final AuditoriaService auditoriaService;

    /** Listado con búsqueda opcional por documento/nombres (?q=). */
    @GetMapping
    public ResponseEntity<List<Cliente>> listar(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(clienteService.listar(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDTO dto) {
        Cliente creado = clienteService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Cliente", creado.getId(),
                "Cliente creado: " + creado.getNumDocumento() + " - " + creado.getNombres());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> editar(@PathVariable Integer id, @Valid @RequestBody ClienteDTO dto) {
        Cliente actualizado = clienteService.editar(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Cliente", id,
                "Cliente editado: " + actualizado.getNumDocumento() + " - " + actualizado.getNombres());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Cliente cliente = clienteService.buscar(id);
        String detalle = "Cliente eliminado: " + cliente.getNumDocumento() + " - " + cliente.getNombres();
        clienteService.eliminar(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Cliente", id, detalle);
        return ResponseEntity.noContent().build();
    }
}
