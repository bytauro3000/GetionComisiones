package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Socio;

public interface SocioRepository extends JpaRepository<Socio, Integer> {

    Optional<Socio> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Socio> findByIdVendedorOrderByNombresAsc(Integer idVendedor);

    List<Socio> findByIdVendedorAndActivoTrueOrderByNombresAsc(Integer idVendedor);

    boolean existsByIdVendedorAndDocumento(Integer idVendedor, String documento);

    /** Borrado en cascada del tenant. */
    void deleteByIdVendedor(Integer idVendedor);
}
