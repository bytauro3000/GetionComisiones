package com.vendelote.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * Información del access token guardada como details de la autenticación.
 * Permite que {@link TenantContext} (estático) acceda al tenant sin
 * volver a parsear el JWT.
 */
public record TokenInfo(WebAuthenticationDetails web, Integer idUsuario, Integer tid) {
}
