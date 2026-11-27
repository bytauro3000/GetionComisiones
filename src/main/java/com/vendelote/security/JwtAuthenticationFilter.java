package com.vendelote.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro JWT del monolito: valida el token Bearer directamente
 * (sin gateway). Si el token es inválido o expiró, deja la petición
 * sin autenticación y Spring Security responde 401; el interceptor
 * del frontend intentará refrescar el access token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Rutas públicas: pasan sin token
        if (esRutaPublica(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtUtil.extractUsername(token);
                if (email != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Claims claims = jwtUtil.extractClaim(token, c -> c);
                    String rol = claims.get("rol", String.class);
                    Integer idUsuario = claims.get("id", Integer.class);
                    Integer tid = claims.get("tid", Integer.class);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    email, null,
                                    List.of(new SimpleGrantedAuthority(rol)));
                    // details con la info del token (tenant) para TenantContext
                    auth.setDetails(new TokenInfo(
                            new WebAuthenticationDetailsSource().buildDetails(request),
                            idUsuario, tid));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Token expirado o con firma inválida: se ignora y queda como anónimo
                log.debug("Token JWT descartado ({}): {}", path, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Rutas públicas — debe coincidir con el permitAll de SecurityConfig. */
    private boolean esRutaPublica(String path) {
        return path.contains("/api/auth/")
                || path.contains("/api/public/")
                || path.contains("/api/proformas/pdf-publico/")
                || path.startsWith("/ws")
                || path.startsWith("/actuator/")
                || path.equals("/favicon.ico")
                || path.equals("/favicon.png")
                || path.equals("/error")
                || path.equals("/");
    }
}
