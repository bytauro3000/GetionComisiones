package com.vendelote.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.vendelote.dto.ResetPasswordDTO;
import com.vendelote.dto.UsuarioListadoDTO;
import com.vendelote.dto.UsuarioRegistroDTO;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.enums.RolUsuario;
import com.vendelote.security.UsuarioActualResolver;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Gestión de usuarios — SOLO ROLE_ADMINISTRADOR (regla en SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualResolver usuarioActualResolver;

    @GetMapping
    public ResponseEntity<List<UsuarioListadoDTO>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(estado));
    }

    /** Roles disponibles para el formulario de creación/edición. */
    @GetMapping("/roles")
    public ResponseEntity<List<String>> listarRoles() {
        return ResponseEntity.ok(List.of(RolUsuario.values()).stream()
                .map(Enum::name).toList());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRegistroDTO dto) {
        UsuarioListadoDTO creado = usuarioService.registrarUsuario(dto);
        auditoriaService.registrar(AccionAuditoria.CREAR, "Usuario", creado.getId(),
                "Usuario creado: " + creado.getCorreo() + " | Rol: " + creado.getRol());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Integer id,
                                    @Valid @RequestBody UsuarioRegistroDTO dto) {
        UsuarioListadoDTO actualizado = usuarioService.editarUsuario(id, dto);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Usuario", id,
                "Usuario editado: " + actualizado.getCorreo() + " | Rol: " + actualizado.getRol());
        return ResponseEntity.ok(actualizado);
    }

    /** Activa/desactiva el usuario (con protecciones contra el último admin). */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id) {
        UsuarioListadoDTO actualizado = usuarioService.cambiarEstadoUsuario(id);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZAR, "Usuario", id,
                "Estado cambiado a: " + actualizado.getEstado() + " | " + actualizado.getCorreo());
        return ResponseEntity.ok(actualizado);
    }

    /** Aprueba un auto-registro (pendiente → activo). */
    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id) {
        UsuarioListadoDTO actualizado = usuarioService.aprobarUsuario(id);
        auditoriaService.registrar(AccionAuditoria.APROBAR, "Usuario", id,
                "Cuenta aprobada: " + actualizado.getCorreo() + " | Por: " + usuarioActualResolver.email());
        return ResponseEntity.ok(actualizado);
    }

    /** Rechaza un auto-registro (pendiente → inactivo, sin sesiones). */
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Integer id) {
        UsuarioListadoDTO actualizado = usuarioService.rechazarUsuario(id);
        auditoriaService.registrar(AccionAuditoria.APROBAR, "Usuario", id,
                "Cuenta rechazada: " + actualizado.getCorreo() + " | Por: " + usuarioActualResolver.email());
        return ResponseEntity.ok(actualizado);
    }

    /** Reset de contraseña por administrador: cambia la clave y cierra las sesiones. */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        usuarioService.resetPassword(id, dto.getNuevaContrasena());
        auditoriaService.registrar(AccionAuditoria.RESET_PASSWORD, "Usuario", id,
                "Contraseña restablecida por " + usuarioActualResolver.email());
        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Contraseña restablecida correctamente"));
    }

    /** Elimina el usuario y en cascada todo su árbol de datos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> eliminar(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        auditoriaService.registrar(AccionAuditoria.ELIMINAR, "Usuario", id,
                "Usuario eliminado en cascada con todos sus datos | Por: " + usuarioActualResolver.email());
        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Usuario y todos sus datos asociados fueron eliminados"));
    }
}
