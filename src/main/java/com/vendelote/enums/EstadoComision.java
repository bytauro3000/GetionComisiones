package com.vendelote.enums;

/** Estado del cobro de una comisión según sus pagos (se recalcula al registrar pagos). */
public enum EstadoComision {
    PENDIENTE,
    PAGADA_PARCIAL,
    PAGADA,
    ANULADA
}
