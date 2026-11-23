package com.vendelote.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.ComisionReparto;

public interface ComisionRepartoRepository extends JpaRepository<ComisionReparto, Integer> {

    List<ComisionReparto> findByIdComisionAndIdVendedor(Integer idComision, Integer idVendedor);

    List<ComisionReparto> findByIdComisionInAndIdVendedor(
            Collection<Integer> idsComision, Integer idVendedor);

    List<ComisionReparto> findByIdVendedor(Integer idVendedor);

    boolean existsByIdSocioAndIdVendedor(Integer idSocio, Integer idVendedor);

    void deleteByIdComisionAndIdVendedor(Integer idComision, Integer idVendedor);

    /** Borrado en cascada del tenant. */
    void deleteByIdVendedor(Integer idVendedor);
}
