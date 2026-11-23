package com.vendelote.enums;

/**
 * Roles del sistema. Se persisten como String (EnumType.STRING).
 * En el JWT y en Security se usan con prefijo: ROLE_ADMINISTRADOR, ROLE_VENDEDOR.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    VENDEDOR
}
