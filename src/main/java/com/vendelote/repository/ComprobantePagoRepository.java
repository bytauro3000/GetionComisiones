package com.vendelote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.ComprobantePago;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    List<ComprobantePago> findByIdComprobanteAndIdVendedor(Integer idComprobante, Integer idVendedor);

    /** ¿El pago ya está cubierto por alguna factura? */
    boolean existsByIdPagoAndIdVendedor(Integer idPago, Integer idVendedor);


    /** Borrado en cascada: elimina las filas del vendedor indicado. */
    void deleteByIdVendedor(Integer idVendedor);
}
