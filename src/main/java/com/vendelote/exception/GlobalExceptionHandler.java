package com.vendelote.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vendelote.dto.MensajeErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<MensajeErrorDTO> handleNegocio(NegocioException e) {
        return ResponseEntity.badRequest().body(new MensajeErrorDTO(e.getMessage()));
    }

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<MensajeErrorDTO> handleTenant(TenantException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MensajeErrorDTO(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MensajeErrorDTO> handleAuth(AuthenticationException e) {
        String mensaje = (e instanceof DisabledException)
                ? "Usuario inactivo: contacte al administrador"
                : "Correo o contraseña incorrectos";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensajeErrorDTO(mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeErrorDTO> handleValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(new MensajeErrorDTO(mensaje));
    }

    /** Ruta inexistente: 404 (antes caía en el catch-all como 500). */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<MensajeErrorDTO> handleNoEncontrado(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeErrorDTO("Recurso no encontrado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeErrorDTO> handleGeneral(Exception e) {
        log.error("Error interno no controlado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeErrorDTO("Error interno del servidor"));
    }
}
