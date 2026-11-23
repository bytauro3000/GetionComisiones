package com.vendelote.entity;

import com.vendelote.security.TenantContext;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Red de seguridad multi-tenant en JPA:
 * - Antes de INSERTAR, si la entidad no trae dueño, lo toma del token.
 * - Antes de ACTUALIZAR, el dueño es inmutable: si otro tenant toca la fila, falla.
 *
 * El filtrado de lecturas se hace en los repositorios/servicios con
 * TenantContext; este listener es la última línea de defensa.
 */
public class TenantAwareListener {

    @PrePersist
    public void antesDePersistir(Object entidad) {
        if (entidad instanceof TenantEntity tenant && tenant.getIdVendedor() == null) {
            tenant.setIdVendedor(TenantContext.idVendedorRequerido());
        }
    }

    @PreUpdate
    public void antesDeActualizar(Object entidad) {
        if (entidad instanceof TenantEntity tenant && tenant.getIdVendedor() != null) {
            Integer actual = TenantContext.idVendedor();
            if (actual != null && !actual.equals(tenant.getIdVendedor())) {
                throw new com.vendelote.exception.TenantException(
                        "No tiene permiso sobre este registro");
            }
        }
    }
}
