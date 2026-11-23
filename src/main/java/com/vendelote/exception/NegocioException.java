package com.vendelote.exception;

/**
 * Excepción de negocio — su mensaje SÍ se muestra al cliente.
 * Usar cuando el error es causado por datos incorrectos del usuario o
 * reglas de negocio (correo duplicado, usuario bloqueado, etc.).
 */
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
