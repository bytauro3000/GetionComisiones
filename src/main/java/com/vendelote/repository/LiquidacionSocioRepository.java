package com.vendelote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.LiquidacionSocio;

public interface LiquidacionSocioRepository extends JpaRepository<LiquidacionSocio, Integer> {

    List<LiquidacionSocio> findByIdVendedorOrderByFechaRegistroDesc(Integer idVendedor);

    List<LiquidacionSocio> findByIdVendedorAndIdSocioOrderByFechaRegistroDesc(
            Integer idVendedor, Integer idSocio);

    void deleteByIdVendedor(Integer idVendedor);
}
