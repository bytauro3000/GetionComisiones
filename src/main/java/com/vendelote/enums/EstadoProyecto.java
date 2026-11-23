package com.vendelote.enums;

/**
 * Estado de un proyecto. Se persiste como String (EnumType.STRING) para que
 * futuras altas de enum no rompan filas históricas.
 */
public enum EstadoProyecto {
    ACTIVO,
    INACTIVO
}
