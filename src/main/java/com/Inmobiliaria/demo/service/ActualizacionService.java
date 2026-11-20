package com.Inmobiliaria.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

/**
 * Broadcast de actualizaciones de datos en tiempo real (STOMP).
 * Los clientes se suscriben a /grupal/actualizaciones y reciben
 * {entidad, accion, id} tras cada mutación relevante, para que las
 * listas abiertas en otras pestañas/usuarios se recarguen solas.
 *
 * El envío se posterga al commit de la transacción activa (si la hay)
 * para que al recibir el mensaje los datos ya sean visibles en BD.
 */
@Service
@RequiredArgsConstructor
public class ActualizacionService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publicar(String entidad, String accion, Object id) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("entidad", entidad);
        payload.put("accion", accion);
        payload.put("id", id != null ? id : 0);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(payload);
                }
            });
        } else {
            enviar(payload);
        }
    }

    private void enviar(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/grupal/actualizaciones", payload);
    }
}
