package com.vendelote.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.AsociadoDTO;
import com.vendelote.dto.AsociadoResumenDTO;
import com.vendelote.dto.ResetPasswordDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.security.UsuarioActualResolver;
import com.vendelote.service.AsociadoService;
import com.vendelote.service.AuditoriaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Cuentas de vendedores asociados creadas por el vendedor jefe. */
@RestController
@RequestMapping("/api/asociados")
@PreAuthorize("hasAuthority('ROLE_VENDEDOR')")
@RequiredArgsConstructor
public class AsociadoController {

    private final AsociadoService asociadoService;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualResolver usuarioActualResolver;

    @GetMapping
    public ResponseEntity<List<AsociadoResumenDTO>> listar() {
        return ResponseEntity.ok(asociadoService.listar());
    }

    @PostMapping
    public ResponseEntity<AsociadoResumenDTO> crear(@Valid @RequestBody AsociadoDTO dto) {
        AsociadoResumenDTO creado = asociadoService.crear(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Usuario", creado.id(),
                "Vendedor asociado creado: " + creado.correo());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<AsociadoResumenDTO> cambiarEstado(@PathVariable Integer id,
                                                            @RequestParam String estado) {
        AsociadoResumenDTO actualizado = asociadoService.cambiarEstado(id, estado);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Usuario", id,
                "Estado de asociado: " + actualizado.estado() + " | " + actualizado.correo());
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        asociadoService.resetPassword(id, dto.getNuevaContrasena());
        auditoriaService.registrar(AccionAuditoria.RESET_PASSWORD, "Usuario", id,
                "Contraseña de asociado restablecida por " + usuarioActualResolver.email());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida correctamente"));
    }
}
