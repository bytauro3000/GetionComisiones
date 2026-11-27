package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoVenta;
import com.vendelote.enums.ModalidadVenta;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Venta de uno o varios lotes a uno o varios clientes (venta agrupada:
 * todos los lotes y clientes quedan relacionados a la MISMA venta).
 *
 * Reglas de negocio acordadas:
 * - Moneda: TODO en USD (sin columna de moneda a propósito).
 * - fechaVenta = día de firma de contrato = día de pago de la inicial del
 *   cliente. El conteo de los 8 meses de espera arranca AQUÍ, aunque la
 *   empresa nos pague el anticipo días después.
 * - CONTADO: la empresa paga la totalidad de la comisión de una vez
 *   (pago tipo TOTAL) y puede ser inferior al 3% → comisionManual.
 * - comisionManual: si se informa, es el monto total de la comisión en USD
 *   y sobreescribe el % del parámetro vigente (sirve para contado negociado).
 * - precioVenta = TOTAL de la venta (suma de los precios de lista de los
 *   lotes seleccionados, editable por si hay descuento).
 */
@Entity
@Table(name = "venta",
        indexes = @Index(name = "idx_venta_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    /** Asociado que originó/atiende la venta (null = venta directa del jefe). */
    @Column(name = "id_vendedor_asociado")
    private Integer idVendedorAsociado;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    /** Lotes agrupados en esta venta (todos de la misma empresa). */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<VentaLote> lotes = new ArrayList<>();

    /** Compradores relacionados a esta venta. */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<VentaCliente> clientes = new ArrayList<>();

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(name = "precio_venta", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVenta;

    /** Inicial pagada por el cliente el mismo día de la firma (0 en CONTADO si cubre todo). */
    @Column(name = "monto_inicial", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 20)
    private ModalidadVenta modalidad;

    /** Monto total de comisión acordado (USD). Null = calcular con el % del parámetro. */
    @Column(name = "comision_manual", precision = 14, scale = 2)
    private BigDecimal comisionManual;

    /** fechaVenta + mesesEspera del parámetro vigente (null en CONTADO). */
    @Column(name = "fecha_inicio_cuotas")
    private LocalDate fechaInicioCuotas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.ACTIVA;

    @Column(name = "notas", length = 300)
    private String notas;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
