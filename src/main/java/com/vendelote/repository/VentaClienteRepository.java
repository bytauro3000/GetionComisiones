package com.vendelote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Venta;
import com.vendelote.entity.VentaCliente;

public interface VentaClienteRepository extends JpaRepository<VentaCliente, Integer> {

    /** ¿El cliente participa en alguna venta del vendedor? (evita borrarlo). */
    boolean existsByVenta_IdVendedorAndIdCliente(Integer idVendedor, Integer idCliente);

    List<VentaCliente> findByVentaIn(List<Venta> ventas);
}
