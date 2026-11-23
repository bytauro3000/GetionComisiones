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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vínculo N:M comprobante ↔ pago: QUÉ pagos cubre cada factura (pueden ser
 * de varios lotes distintos: "USD 1500 → Mz.B1 Lt.15 + Mz.C Lt.18").
 * De aquí sale la descripción automática del comprobante.
 */
@Entity
@Table(name = "comprobante_pago",
        uniqueConstraints = @UniqueConstraint(name = "uk_comp_pago",
                columnNames = { "id_comprobante", "id_pago" }),
        indexes = @Index(name = "idx_comp_pago_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePago implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante_pago")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_comprobante", nullable = false)
    private Integer idComprobante;

    @Column(name = "id_pago", nullable = false)
    private Integer idPago;

    /** Monto del pago cubierto por este comprobante (USD). */
    @Column(name = "monto_aplicado", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAplicado;
}
