package com.vendelote.realtime;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Canal WebSocket único para avisos de cambios. No envía datos de negocio:
 * solo notifica que algo cambió para que el frontend recargue su vista.
 */
@Slf4j
@Component
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();
    private final AtomicLong version = new AtomicLong();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sesiones.add(session);
        log.debug("WebSocket conectado: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.remove(session);
        log.debug("WebSocket cerrado: {} ({})", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // El cliente envía "ping" periódicamente para mantener viva la conexión
        // detrás de proxies/Render.
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            enviar(session, "{\"type\":\"pong\"}");
        }
    }

    /** Avisa a todas las vistas internas y enlaces públicos conectados. */
    public void broadcastDataChanged() {
        long v = version.incrementAndGet();
        String payload = "{\"type\":\"data-changed\",\"version\":" + v + "}";
        for (WebSocketSession session : sesiones) {
            try {
                enviar(session, payload);
            } catch (Exception e) {
                sesiones.remove(session);
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ignored) {
                    // La sesión ya no es utilizable.
                }
            }
        }
    }

    private void enviar(WebSocketSession session, String payload) throws IOException {
        if (!session.isOpen()) {
            sesiones.remove(session);
            return;
        }
        synchronized (session) {
            session.sendMessage(new TextMessage(payload));
        }
    }
}
