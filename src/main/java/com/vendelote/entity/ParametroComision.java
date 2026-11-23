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
 * Parámetros de cálculo de comisión por tenant (SaaS: cada vendedor tiene
 * los suyos). Dos niveles de alcance:
 * - idEmpresa = null  → GENERAL (aplica a todas las ventas del tenant)
 * - idEmpresa informe → EMPRESA (sobreescribe lo general para esa empresa)
 *
 * Resolución: se toma la fila VIGENTE más específica y más reciente.
 */
@Entity
@Table(name = "parametro_comision",
        indexes = @Index(name = "idx_param_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParametroComision implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    /** Null = alcance GENERAL para todo el tenant. */
    @Column(name = "id_empresa")
    private Integer idEmpresa;

    /** % de comisión sobre el precio de venta (FINANCIADO). Ej: 0.0300 = 3%. */
    @Column(name = "porcentaje_comision", nullable = false, precision = 5, scale = 4)
    private BigDecimal porcentajeComision = new BigDecimal("0.0300");

    /** % en CONTADO (puede ser menor: "es mucho dinero" pagar 3% de una). */
    @Column(name = "porcentaje_comision_contado", nullable = false, precision = 5, scale = 4)
    private BigDecimal porcentajeComisionContado = new BigDecimal("0.0300");

    /** % de anticipo sobre la inicial (0.3000 = 30%). */
    @Column(name = "anticipo_porcentaje", nullable = false, precision = 5, scale = 4)
    private BigDecimal anticipoPorcentaje = new BigDecimal("0.3000");

    /** Si está informado, el anticipo es este monto fijo (USD) y manda sobre el %. */
    @Column(name = "anticipo_monto_fijo", precision = 14, scale = 2)
    private BigDecimal anticipoMontoFijo;

    /** Monto de cada cuota mensual en USD (default 50). */
    @Column(name = "monto_cuota", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoCuota = new BigDecimal("50.00");

    /** Meses de espera desde la fecha de venta hasta la 1ª cuota (default 8). */
    @Column(name = "meses_espera", nullable = false)
    private Integer mesesEspera = 8;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia;

    /** Null = vigente sin fecha fin. */
    @Column(name = "fecha_fin_vigencia")
    private LocalDate fechaFinVigencia;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
