package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Comprobante;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

    Optional<Comprobante> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Comprobante> findByIdVendedorOrderByFechaEmisionDesc(Integer idVendedor);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
