package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
 * Liquidación (pago) que el vendedor hace a un socio por la parte que le
 * corresponde de las comisiones cobradas. Registra el bruto, los descuentos
 * de IGV y detracción, y el neto pagado.
 */
@Entity
@Table(name = "liquidacion_socio",
        indexes = @Index(name = "idx_liquidacion_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionSocio implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_socio", nullable = false)
    private Integer idSocio;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Parte bruta del socio (suma de sus participaciones cobradas). */
    @Column(name = "monto_bruto", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoBruto = BigDecimal.ZERO;

    @Column(name = "descuento_igv", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuentoIgv = BigDecimal.ZERO;

    @Column(name = "descuento_detraccion", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuentoDetraccion = BigDecimal.ZERO;

    /** montoBruto − IGV − detracción. */
    @Column(name = "monto_neto", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoNeto = BigDecimal.ZERO;

    @Column(name = "referencia", length = 60)
    private String referencia;

    @Column(name = "notas", length = 200)
    private String notas;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
