package com.vendelote.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vendelote.entity.Lote;
import com.vendelote.enums.EstadoLote;
import com.vendelote.repository.LoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Genera avisos (in-app) para las separaciones PROPIAS que vencen en 3 y 1 días.
 * Corre a diario a las 08:00 (configurable con app.notificaciones.cron).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionJob {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LoteRepository loteRepository;
    private final NotificacionService notificacionService;

    @Scheduled(cron = "${app.notificaciones.cron:0 0 8 * * *}")
    public void avisarSeparacionesPorVencer() {        LocalDate hoy = LocalDate.now();
        int creadas = 0;
        for (int dias : new int[] { 3, 1 }) {
            LocalDate fecha = hoy.plusDays(dias);
            List<Lote> lotes = loteRepository
                    .findByEstadoAndSeparacionAjenaFalseAndFechaVinceSeparacion(
                            EstadoLote.RESERVADO, fecha);
            for (Lote lote : lotes) {
                String clave = "SEP:" + dias + ":" + lote.getId() + ":" + fecha;
                String mensaje = "El lote " + lote.getProyecto()
                        + " Mz." + lote.getManzana() + " Lt." + lote.getNumero()
                        + " vence en " + dias + " día(s) (" + fecha.format(FMT) + ").";
                notificacionService.crear(lote.getIdVendedor(),
                        "SEPARACION_POR_VENCER", "Separación por vencer", mensaje,
                        lote.getId(), clave);
                creadas++;
            }
        }
        if (creadas > 0) {
            log.info("Avisos de separaciones evaluados: {} (se omiten duplicados)", creadas);
        }
    }

    /** Además del cron, se ejecuta al arrancar para generar avisos de inmediato. */
    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        avisarSeparacionesPorVencer();
    }
}
