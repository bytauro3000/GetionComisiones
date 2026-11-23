package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    Optional<Proyecto> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<Proyecto> findByIdVendedorOrderByFechaRegistroDesc(Integer idVendedor);

    boolean existsByIdVendedorAndIdEmpresaAndNombre(Integer idVendedor, Integer idEmpresa, String nombre);

    boolean existsByIdVendedorAndIdEmpresaAndNombreAndIdNot(
            Integer idVendedor, Integer idEmpresa, String nombre, Integer id);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
