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
 * Reparto de UNA comisión con UN socio. Una comisión puede tener hasta 2 socios
 * (vendedor + 2 socios = 3 partes). El reparto tiene dos componentes
 * independientes: el pago inicial (anticipo/total) y las letras (cuotas).
 *
 * En cada componente el socio puede participar o no, y su parte se define por
 * porcentaje o por monto fijo (si el monto está presente, prevalece).
 */
@Entity
@Table(name = "comision_reparto",
        uniqueConstraints = @UniqueConstraint(name = "uk_reparto_comision_socio",
                columnNames = { "id_comision", "id_socio" }),
        indexes = {
            @Index(name = "idx_reparto_tenant", columnList = "id_vendedor"),
            @Index(name = "idx_reparto_comision", columnList = "id_comision")
        })
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComisionReparto implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reparto")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_comision", nullable = false)
    private Integer idComision;

    @Column(name = "id_socio", nullable = false)
    private Integer idSocio;

    /** ¿Participa en el pago inicial (anticipo en financiado o total en contado)? */
    @Column(name = "participa_inicial", nullable = false)
    private boolean participaInicial;

    /** % del pago inicial (0..1). Se usa si montoInicial es null. */
    @Column(name = "porcentaje_inicial", precision = 5, scale = 4)
    private BigDecimal porcentajeInicial;

    /** Monto fijo del pago inicial; si no es null, prevalece sobre el %. */
    @Column(name = "monto_inicial", precision = 14, scale = 2)
    private BigDecimal montoInicial;

    /** ¿Participa en las letras (cuotas de la comisión)? */
    @Column(name = "participa_cuotas", nullable = false)
    private boolean participaCuotas;

    /** % de cada cuota (0..1). Se usa si montoCuotaSocio es null. */
    @Column(name = "porcentaje_cuotas", precision = 5, scale = 4)
    private BigDecimal porcentajeCuotas;

    /** Monto fijo POR CADA cuota; si no es null, prevalece sobre el %. */
    @Column(name = "monto_cuota_socio", precision = 14, scale = 2)
    private BigDecimal montoCuotaSocio;
}
