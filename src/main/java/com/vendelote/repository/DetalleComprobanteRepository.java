package com.vendelote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.DetalleComprobante;

public interface DetalleComprobanteRepository extends JpaRepository<DetalleComprobante, Integer> {

    List<DetalleComprobante> findByIdComprobanteAndIdVendedorOrderByIdAsc(Integer idComprobante, Integer idVendedor);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
