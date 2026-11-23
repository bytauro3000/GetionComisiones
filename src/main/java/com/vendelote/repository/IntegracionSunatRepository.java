package com.vendelote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.IntegracionSunat;

public interface IntegracionSunatRepository extends JpaRepository<IntegracionSunat, Integer> {

    Optional<IntegracionSunat> findByIdVendedor(Integer idVendedor);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
