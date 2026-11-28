package com.vendelote.enums;

public enum EstadoVenta {
    /** Registrada por un vendedor socio; requiere confirmación del jefe. */
    PENDIENTE,
    ACTIVA,
    ANULADA,
    /** Rechazada por el jefe; no genera comisión. */
    RECHAZADA
}
