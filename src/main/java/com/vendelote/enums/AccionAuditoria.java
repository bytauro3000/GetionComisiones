package com.vendelote.enums;

/**
 * Acciones registradas en el módulo de auditoría.
 * Se persisten como String (EnumType.STRING) para que futuras altas de
 * enum no rompan filas históricas.
 */
public enum AccionAuditoria {
    LOGIN,
    LOGOUT,
    CREAR,
    ACTUALIZAR,
    ELIMINAR,
    RESET_PASSWORD,
    /** Auto-registro público de un vendedor (queda pendiente de aprobación). */
    REGISTRO,
    /** Aprobación o rechazo de un auto-registro por el administrador. */
    APROBAR
}
