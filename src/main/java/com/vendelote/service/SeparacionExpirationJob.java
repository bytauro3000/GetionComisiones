package com.vendelote.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.entity.Lote;
import com.vendelote.enums.EstadoLote;
import com.vendelote.repository.LoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Libera automáticamente los lotes RESERVADO cuya fecha de separación ya venció,
 * devolviéndolos a DISPONIBLE. Antes esto solo ocurría si un vendedor lo hacía a mano.
 *
 * Corre a diario (configurable con app.separaciones.expiracion-cron). Es una tarea
 * de sistema: opera sobre todos los tenants y no depende del contexto de seguridad.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeparacionExpirationJob {

    private final LoteRepository loteRepository;

    @Scheduled(cron = "${app.separaciones.expiracion-cron:0 30 0 * * *}")
    @Transactional
    public void liberarSeparacionesVencidas() {
        LocalDate hoy = LocalDate.now();
        List<Lote> vencidos = loteRepository
                .findByEstadoAndFechaVinceSeparacionBefore(EstadoLote.RESERVADO, hoy);

        if (vencidos.isEmpty()) {
            return;
        }

        for (Lote lote : vencidos) {
            lote.setEstado(EstadoLote.DISPONIBLE);
            lote.setFechaVinceSeparacion(null);
            lote.setMontoSeparacion(null);
            lote.setSeparacionAjena(false);
            lote.setClienteSeparacion(null);
            lote.setNotaSeparacion(null);
            lote.setMotivoEstado(null);
        }
        loteRepository.saveAll(vencidos);

        log.info("Separaciones vencidas liberadas: {} lote(s)", vencidos.size());
    }
}
