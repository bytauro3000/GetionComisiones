package com.vendelote.exception;

/** Acceso denegado por aislamiento de tenant (403). */
public class TenantException extends RuntimeException {

    public TenantException(String mensaje) {
        super(mensaje);
    }
}
