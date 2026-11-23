package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.PagoComision;
import com.vendelote.enums.EstadoPago;

public interface PagoComisionRepository extends JpaRepository<PagoComision, Integer> {

    Optional<PagoComision> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    /** Pagos de una comisión: anticipo/TOTAL (numero_cuota null) primero, luego cuotas 1..N. */
    List<PagoComision> findByIdComisionAndIdVendedorOrderByFechaProgramadaAscNumeroCuotaAsc(
            Integer idComision, Integer idVendedor);

    /** Pendientes de cobro (para el tablero). */
    List<PagoComision> findByIdVendedorAndEstadoOrderByFechaProgramadaAsc(Integer idVendedor, EstadoPago estado);

    /** Pagos cobrados aún sin factura (candidatos a comprobante). */
    List<PagoComision> findByIdVendedorAndEstadoOrderByFechaPagoDesc(Integer idVendedor, EstadoPago estado);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
