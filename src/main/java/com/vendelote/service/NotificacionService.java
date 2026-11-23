package com.vendelote.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.NotificacionDTO;
import com.vendelote.entity.Notificacion;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.NotificacionRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional(readOnly = true)
    public List<NotificacionDTO> listar(boolean soloNoLeidas) {
        Integer idV = TenantContext.idVendedorRequerido();
        List<Notificacion> lista = soloNoLeidas
                ? notificacionRepository.findByIdVendedorAndLeidaFalseOrderByFechaCreacionDesc(idV)
                : notificacionRepository.findByIdVendedorOrderByFechaCreacionDesc(idV);
        return lista.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        return notificacionRepository.countByIdVendedorAndLeidaFalse(TenantContext.idVendedorRequerido());
    }

    @Transactional
    public void marcarLeida(Integer id) {
        Notificacion n = notificacionRepository
                .findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Notificación no encontrada"));
        n.setLeida(true);
        notificacionRepository.save(n);
    }

    @Transactional
    public void marcarTodasLeidas() {
        Integer idV = TenantContext.idVendedorRequerido();
        List<Notificacion> pendientes =
                notificacionRepository.findByIdVendedorAndLeidaFalseOrderByFechaCreacionDesc(idV);
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    /** Crea una notificación idempotente (por clave). Usado por jobs de sistema. */
    @Transactional
    public void crear(Integer idVendedor, String tipo, String titulo, String mensaje,
                      Integer idReferencia, String clave) {
        if (idVendedor == null) {
            return;
        }
        if (clave != null && notificacionRepository.existsByIdVendedorAndClave(idVendedor, clave)) {
            return;
        }
        Notificacion n = new Notificacion();
        n.setIdVendedor(idVendedor);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setIdReferencia(idReferencia);
        n.setClave(clave);
        notificacionRepository.save(n);
    }

    private NotificacionDTO toDTO(Notificacion n) {
        return new NotificacionDTO(n.getId(), n.getTipo(), n.getTitulo(), n.getMensaje(),
                n.getIdReferencia(), n.isLeida(), n.getFechaCreacion());
    }
}
