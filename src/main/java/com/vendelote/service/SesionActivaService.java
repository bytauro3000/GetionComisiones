package com.vendelote.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.SesionResumenDTO;
import com.vendelote.dto.UsuarioActivoDTO;
import com.vendelote.entity.SesionActiva;
import com.vendelote.entity.Usuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.SesionActivaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SesionActivaService {

    private final SesionActivaRepository repository;

    @Transactional
    public void registrarLogin(Usuario usuario, String ip, String userAgent) {
        // Desactivar sesiones previas activas del mismo usuario
        List<SesionActiva> activas = repository.findByUsuarioIdAndActivaTrue(usuario.getId());
        activas.forEach(s -> s.setActiva(false));
        repository.saveAll(activas);

        SesionActiva sesion = new SesionActiva();
        sesion.setUsuario(usuario);
        sesion.setUltimoRefresh(LocalDateTime.now());
        sesion.setIp(ip);
        sesion.setUserAgent(userAgent);
        sesion.setActiva(true);
        sesion.setFechaLogueo(LocalDateTime.now());
        repository.save(sesion);
    }

    @Transactional
    public void actualizarRefresh(Integer usuarioId) {
        List<SesionActiva> activas = repository.findByUsuarioIdAndActivaTrue(usuarioId);
        if (!activas.isEmpty()) {
            SesionActiva ultima = activas.get(activas.size() - 1);
            ultima.setUltimoRefresh(LocalDateTime.now());
            repository.save(ultima);
        }
    }

    @Transactional
    public void desactivarSesion(Integer usuarioId) {
        List<SesionActiva> activas = repository.findByUsuarioIdAndActivaTrue(usuarioId);
        activas.forEach(s -> s.setActiva(false));
        repository.saveAll(activas);
    }

    /** Cierra una sesión desde el panel de administración. */
    @Transactional
    public void cerrarSesion(Long idSesion) {
        SesionActiva sesion = repository.findById(idSesion)
                .orElseThrow(() -> new NegocioException("Sesión no encontrada"));
        sesion.setActiva(false);
        repository.save(sesion);
    }

    public SesionResumenDTO obtenerResumen() {
        List<SesionActiva> activas = repository.findActivasConUsuario();
        LocalDate hoy = LocalDate.now();
        long visitasHoy = repository.countVisitasEntre(hoy.atStartOfDay(), hoy.atTime(LocalTime.MAX));

        List<UsuarioActivoDTO> sesiones = activas.stream().map(s -> {
            Usuario u = s.getUsuario();
            return new UsuarioActivoDTO(
                    s.getId(), u.getId(),
                    u.getNombres() + " " + u.getApellidos(),
                    u.getCorreo(), s.getIp(), s.getUserAgent(),
                    s.getFechaLogueo(), s.getUltimoRefresh()
            );
        }).toList();

        return new SesionResumenDTO(activas.size(), visitasHoy, sesiones);
    }
}
