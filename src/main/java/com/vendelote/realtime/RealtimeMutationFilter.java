package com.vendelote.realtime;

import java.io.IOException;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Avisa por WebSocket después de cualquier mutación exitosa de la API.
 * Se hace a nivel de filtro para no tener que recordar llamar al notificador
 * en cada controlador/servicio nuevo.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class RealtimeMutationFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_QUE_NO_CAMBIAN =
            Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final RealtimeWebSocketHandler realtimeHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);

        boolean mutacion = !METODOS_QUE_NO_CAMBIAN.contains(request.getMethod());
        boolean api = request.getRequestURI().startsWith("/api/");
        boolean exito = response.getStatus() < 400;

        if (mutacion && api && exito) {
            realtimeHandler.broadcastDataChanged();
        }
    }
}
