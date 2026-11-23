package com.vendelote.enums;

/**
 * Estado de un comprobante ante api-sunat:
 * - EMITIDO: registrado localmente (pendiente de envío a SUNAT).
 * - EN_PROCESO: enviado a la API, en cola de firma/envío.
 * - ACEPTADO / RECHAZADO: según CDR.
 * - ANULADO: anulado por el emisor.
 */
public enum EstadoComprobante {
    EMITIDO,
    EN_PROCESO,
    ACEPTADO,
    RECHAZADO,
    ANULADO
}
