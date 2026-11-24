package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Proforma;

public interface ProformaRepository extends JpaRepository<Proforma, Integer> {

    List<Proforma> findByIdVendedorOrderByFechaDesc(Integer idVendedor);

    Optional<Proforma> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    Optional<Proforma> findByPublicToken(String publicToken);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
