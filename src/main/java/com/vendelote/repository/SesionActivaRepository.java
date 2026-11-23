package com.vendelote.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vendelote.entity.SesionActiva;

@Repository
public interface SesionActivaRepository extends JpaRepository<SesionActiva, Long> {

    List<SesionActiva> findByUsuarioIdAndActivaTrue(Integer usuarioId);

    @Query("SELECT s FROM SesionActiva s JOIN FETCH s.usuario u WHERE s.activa = true ORDER BY s.ultimoRefresh DESC")
    List<SesionActiva> findActivasConUsuario();

    @Query("SELECT COUNT(s) FROM SesionActiva s WHERE s.fechaLogueo BETWEEN :inicio AND :fin")
    long countVisitasEntre(LocalDateTime inicio, LocalDateTime fin);

    /** Borrado en cascada: elimina las sesiones del usuario. */
    void deleteByUsuarioId(Integer idUsuario);
}
