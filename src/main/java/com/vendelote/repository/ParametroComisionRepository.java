package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.ParametroComision;

public interface ParametroComisionRepository extends JpaRepository<ParametroComision, Integer> {

    Optional<ParametroComision> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    List<ParametroComision> findByIdVendedorOrderByIdDesc(Integer idVendedor);

    /**
     * Candidatos a parámetro vigente para un alcance (idEmpresa = null → GENERAL).
     * El servicio elige el más reciente vigente por fecha.
     */
    List<ParametroComision> findByIdVendedorAndIdEmpresaOrderByFechaInicioVigenciaDesc(
            Integer idVendedor, Integer idEmpresa);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
