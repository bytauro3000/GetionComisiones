package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendelote.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByIdVendedorOrderByFechaCreacionDesc(Integer idVendedor);

    List<Notificacion> findByIdVendedorAndLeidaFalseOrderByFechaCreacionDesc(Integer idVendedor);

    long countByIdVendedorAndLeidaFalse(Integer idVendedor);

    Optional<Notificacion> findByIdAndIdVendedor(Integer id, Integer idVendedor);

    boolean existsByIdVendedorAndClave(Integer idVendedor, String clave);

    void deleteByIdVendedor(Integer idVendedor);
}
