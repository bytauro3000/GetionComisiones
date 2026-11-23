package com.vendelote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vendelote.entity.SecuenciaProforma;

import jakarta.persistence.LockModeType;

public interface SecuenciaProformaRepository extends JpaRepository<SecuenciaProforma, Integer> {

    /** Bloqueo de fila para asignar correlativos sin repetir. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaProforma s WHERE s.idVendedor = :idVendedor AND s.anio = :anio")
    Optional<SecuenciaProforma> bloquear(@Param("idVendedor") Integer idVendedor,
                                         @Param("anio") Integer anio);

    void deleteByIdVendedor(Integer idVendedor);
}
