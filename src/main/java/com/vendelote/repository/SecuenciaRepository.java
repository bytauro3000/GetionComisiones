package com.vendelote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vendelote.entity.Secuencia;

import jakarta.persistence.LockModeType;

public interface SecuenciaRepository extends JpaRepository<Secuencia, Integer> {

    /** Lee la secuencia con bloqueo de fila para asignar correlativos sin repetir. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Secuencia s WHERE s.idEmpresa = :idEmpresa AND s.anio = :anio")
    Optional<Secuencia> bloquear(@Param("idEmpresa") Integer idEmpresa, @Param("anio") Integer anio);

    /** Borrado en cascada: elimina la numeración de proformas de la empresa. */
    void deleteByIdEmpresa(Integer idEmpresa);
}
