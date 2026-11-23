package com.vendelote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vendelote.entity.Venta;
import com.vendelote.entity.VentaLote;
import com.vendelote.enums.EstadoVenta;

public interface VentaLoteRepository extends JpaRepository<VentaLote, Integer> {

    /** Existe una venta ACTIVA que incluya ese lote (no se puede vender dos veces). */
    boolean existsByVenta_IdVendedorAndIdLoteAndVenta_Estado(
            Integer idVendedor, Integer idLote, EstadoVenta estado);

    /** Ids de los lotes del vendedor que tienen venta ACTIVA (para marcar el switch). */
    @Query("SELECT DISTINCT vl.idLote FROM VentaLote vl "
            + "WHERE vl.venta.idVendedor = :idVendedor AND vl.venta.estado = :estado")
    List<Integer> findDistinctIdLoteConVentaActiva(@Param("idVendedor") Integer idVendedor,
                                                   @Param("estado") EstadoVenta estado);

    List<VentaLote> findByVentaIn(List<Venta> ventas);
}
