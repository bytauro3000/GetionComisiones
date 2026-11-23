package com.vendelote.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vendelote.exception.TenantException;

/**
 * Contexto multi-tenant estático: tenant = vendedor (id_vendedor = id_usuario
 * del rol VENDEDOR, claim "tid" del JWT). El administrador de plataforma NO
 * tiene tenant (null): sus endpoints son globales.
 *
 * Uso: {@code Integer idVendedor = TenantContext.idVendedorRequerido();}
 * en servicios/entidades; sin necesidad de inyección ni ThreadLocal extra
 * (SecurityContextHolder ya es ThreadLocal y lo gestiona Spring Security).
 */
public final class TenantContext {

    private TenantContext() {
    }

    /** id del vendedor (= tenant) del token actual, o null si es admin/anónimo. */
    public static Integer idVendedor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof TokenInfo info)) {
            return null;
        }
        return info.tid();
    }

    /** Como idVendedor(), pero exige contexto de vendedor (lanza 403 si no hay). */
    public static Integer idVendedorRequerido() {
        Integer id = idVendedor();
        if (id == null) {
            throw new TenantException(
                    "Operación no permitida: se requiere una cuenta de vendedor activa");
        }
        return id;
    }

    /** id del usuario del token actual (vendedor o admin) o null fuera de HTTP. */
    public static Integer idUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof TokenInfo info)) {
            return null;
        }
        return info.idUsuario();
    }
}
