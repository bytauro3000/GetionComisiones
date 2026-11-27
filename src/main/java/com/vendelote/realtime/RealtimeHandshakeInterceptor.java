package com.vendelote.realtime;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.vendelote.repository.ProyectoRepository;
import com.vendelote.security.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * Acepta conexiones con un access token interno o con el token público de un
 * plano. No se envía información por el socket: solo el aviso de "algo cambió".
 */
@Component
@RequiredArgsConstructor
public class RealtimeHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final ProyectoRepository proyectoRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || token.isBlank()) {
            return false;
        }

        if (jwtUtil.isTokenValid(token)) {
            return true;
        }
        return proyectoRepository.findByPlanoPublicToken(token).isPresent();
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Sin estado adicional.
    }
}
