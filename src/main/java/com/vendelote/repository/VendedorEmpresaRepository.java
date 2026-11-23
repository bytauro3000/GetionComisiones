package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.VendedorEmpresa;

public interface VendedorEmpresaRepository extends JpaRepository<VendedorEmpresa, Integer> {

    Optional<VendedorEmpresa> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<VendedorEmpresa> findByIdVendedorOrderByIdDesc(Integer idVendedor);

    boolean existsByIdVendedorAndIdEmpresa(Integer idVendedor, Integer idEmpresa);

    void deleteByIdVendedorAndIdEmpresa(Integer idVendedor, Integer idEmpresa);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
