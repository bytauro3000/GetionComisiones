package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    Optional<Cliente> findByIdVendedorAndNumDocumento(Integer idVendedor, String numDocumento);

    List<Cliente> findByIdVendedorOrderByNombresAsc(Integer idVendedor);

    boolean existsByIdVendedorAndNumDocumento(Integer idVendedor, String numDocumento);

    boolean existsByIdVendedorAndNumDocumentoAndIdNot(Integer idVendedor, String numDocumento, Integer id);

    Optional<Cliente> findByIdVendedorAndIdVendedorAsociadoAndNumDocumento(
            Integer idVendedor, Integer idVendedorAsociado, String numDocumento);

    boolean existsByIdVendedorAndIdVendedorAsociadoAndNumDocumento(
            Integer idVendedor, Integer idVendedorAsociado, String numDocumento);

    boolean existsByIdVendedorAndIdVendedorAsociadoAndNumDocumentoAndIdNot(
            Integer idVendedor, Integer idVendedorAsociado, String numDocumento, Integer id);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
