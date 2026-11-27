package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoComision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Comisión de una venta (1:1 al registrar la venta). Se generan también sus
 * pagos programados: ANTICIPO (fechaVenta) + CUOTAS de $50 desde
 * fechaInicioCuotas (= fechaVenta + mesesEspera), o un único TOTAL en CONTADO.
 *
 * Toda la información de la venta se "snapshot"ea aquí (empresa, lote,
 * montos, cuota usada) para que cambios futuros no alteren lo ya pactado.
 */
@Entity
@Table(name = "comision",
        indexes = {
            @Index(name = "idx_comision_tenant", columnList = "id_vendedor"),
            @Index(name = "idx_comision_tenant_venta", columnList = "id_vendedor,id_venta")
        })
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comision implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comision")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    /** Asociado atribuido a la venta; su 50% es informativo, se paga fuera del sistema. */
    @Column(name = "id_vendedor_asociado")
    private Integer idVendedorAsociado;

    @Column(name = "id_venta", nullable = false)
    private Integer idVenta;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "id_lote", nullable = false)
    private Integer idLote;

    /** % usado para el cálculo (null cuando se aplicó comisionManual). */
    @Column(name = "porcentaje_aplicado", precision = 5, scale = 4)
    private BigDecimal porcentajeAplicado;

    /** Total de la comisión en USD. */
    @Column(name = "monto_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoTotal;

    /** % del inicial del cliente aplicado al anticipo (snapshot; null si monto fijo). */
    @Column(name = "porcentaje_anticipo", precision = 5, scale = 4)
    private BigDecimal porcentajeAnticipo;

    /** Anticipo (financiado) o 0 (contado). */
    @Column(name = "monto_anticipo", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAnticipo = BigDecimal.ZERO;

    /** saldo = total − anticipo (0 en CONTADO, que se cobra con un solo pago TOTAL). */
    @Column(name = "monto_saldo", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoSaldo = BigDecimal.ZERO;

    /** Acumulado ya cobrado (cache; se actualiza al registrar pagos). */
    @Column(name = "monto_pagado", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    /** Monto de la cuota congelado al crear la comisión (los parámetros pueden cambiar). */
    @Column(name = "monto_cuota", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoCuota;

    /** Ceil(saldo / montoCuota); la última cuota absorbe el residuo. */
    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas = 0;

    /** Primer día de cobro de cuotas (fechaVenta + mesesEspera). */
    @Column(name = "fecha_inicio_cuotas", nullable = false)
    private LocalDate fechaInicioCuotas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoComision estado = EstadoComision.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
