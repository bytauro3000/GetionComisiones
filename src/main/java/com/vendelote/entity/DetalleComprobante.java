package com.vendelote.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea de detalle del comprobante: un lote/pago que se está cobrando.
 * El monto es SIN IGV (el IGV se calcula al total del comprobante).
 */
@Entity
@Table(name = "detalle_comprobante",
        indexes = @Index(name = "idx_detalle_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleComprobante implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;

    /** Ej: "Comisión de intermediación - Mz.B1 Lt.15 (La Florida de Torre Blanca)". */
    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    /** Monto de la línea SIN IGV (USD). */
    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;
}
