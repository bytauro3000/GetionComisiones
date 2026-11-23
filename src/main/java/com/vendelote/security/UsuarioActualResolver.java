package com.vendelote.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Punto ÚNICO de resolución del usuario actual.
 * Nunca lanza excepción: si no hay contexto (job/scheduler), devuelve
 * valores de sistema.
 */
@Component
public class UsuarioActualResolver {

    private static final String ANONIMO = "sistema";

    /** Email del usuario autenticado o "sistema" fuera de contexto HTTP. */
    public String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null
                || auth.getName().isBlank()) {
            return ANONIMO;
        }
        return auth.getName();
    }

    /** Primera autoridad del usuario (ej: ROLE_ADMINISTRADOR) o null. */
    public String rol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return null;
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    /** IP real del cliente (detrás de proxy se entrega en X-Forwarded-For). */
    public String ip() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
