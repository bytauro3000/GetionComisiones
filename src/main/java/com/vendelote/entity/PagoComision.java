package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoPago;
import com.vendelote.enums.FormaPago;
import com.vendelote.enums.TipoPago;

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
 * Pago de una comisión (programado o recibido).
 *
 * - fechaProgramada: cuándo TOCABA pagar (anticipo: fechaVenta; cuotas:
 *   desde el mes 8). Queda fija aunque la empresa se demore.
 * - fechaPago: cuándo se recibió REALMENTE (a veces +1, +3 o +7 días).
 *   Vacía + estado PENDIENTE = la empresa aún no paga.
 * - Al marcar PAGADO se actualiza comision.montoPagado y su estado.
 */
@Entity
@Table(name = "pago_comision",
        indexes = {
            @Index(name = "idx_pago_tenant", columnList = "id_vendedor"),
            @Index(name = "idx_pago_tenant_comision", columnList = "id_vendedor,id_comision")
        })
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoComision implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_comision", nullable = false)
    private Integer idComision;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPago tipo;

    /** Solo para tipo CUOTA: 1..N. */
    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    /** Fecha real de cobro (vacía mientras esté PENDIENTE). */
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", length = 20)
    private FormaPago formaPago;

    /** Número de operación / referencia del depósito. */
    @Column(name = "referencia", length = 60)
    private String referencia;

    @Column(name = "notas", length = 200)
    private String notas;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
