package com.vendelote.entity;

/**
 * Marcador para entidades con dueño (id_vendedor = tenant).
 * Todas las tablas de negocio la implementan y usan
 * {@link TenantAwareListener} para el relleno automático.
 */
public interface TenantEntity {

    Integer getIdVendedor();

    void setIdVendedor(Integer idVendedor);
}
