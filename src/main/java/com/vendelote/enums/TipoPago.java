package com.vendelote.enums;

/**
 * Tipo de pago de comisión:
 * - ANTICIPO: primer pago en financiado (30% de la inicial).
 * - CUOTA: cuota mensual de $50 desde el mes 8.
 * - TOTAL: pago único de la comisión completa (venta CONTADO).
 * - AJUSTE: regularización manual.
 */
public enum TipoPago {
    ANTICIPO,
    CUOTA,
    TOTAL,
    AJUSTE
}
