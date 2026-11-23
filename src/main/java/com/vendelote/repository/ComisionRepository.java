package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Comision;

public interface ComisionRepository extends JpaRepository<Comision, Integer> {

    Optional<Comision> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    /** 1:1 con la venta. */
    Optional<Comision> findByIdVentaAndIdVendedor(Integer idVenta, Integer idVendedor);

    List<Comision> findByIdVendedorOrderByIdDesc(Integer idVendedor);

    boolean existsByIdVendedorAndIdEmpresa(Integer idVendedor, Integer idEmpresa);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
