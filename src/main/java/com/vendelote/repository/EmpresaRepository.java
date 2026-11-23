package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Empresa> findByIdVendedorOrderByRazonSocialAsc(Integer idVendedor);

    boolean existsByIdVendedorAndRuc(Integer idVendedor, String ruc);

    boolean existsByIdVendedorAndRucAndIdNot(Integer idVendedor, String ruc, Integer id);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
