package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Venta> findByIdVendedorOrderByFechaVentaDesc(Integer idVendedor);

    boolean existsByIdVendedorAndIdEmpresa(Integer idVendedor, Integer idEmpresa);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
