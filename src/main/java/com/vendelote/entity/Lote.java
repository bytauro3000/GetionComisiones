package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoLote;

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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lote (terreno) de una empresa. El nombre del programa/proyecto se guarda
 * aquí ("La Florida de Torre Blanca") porque se usa en la descripción de
 * las facturas al agrupar pagos de varios lotes.
 */
@Entity
@Table(name = "lote",
        uniqueConstraints = @UniqueConstraint(name = "uk_lote_tenant",
                columnNames = { "id_vendedor", "id_empresa", "proyecto", "manzana", "numero" }),
        indexes = @Index(name = "idx_lote_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lote implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    /** Nombre del programa/proyecto inmobiliario (copia del nombre del proyecto FK). */
    @Column(name = "proyecto", nullable = false, length = 120)
    private String proyecto;

    /** FK al proyecto (el texto anterior es una copia sincronizada). */
    @Column(name = "id_proyecto")
    private Integer idProyecto;

    @Column(name = "manzana", nullable = false, length = 10)
    private String manzana;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    /** Descripción breve del lote (ej. "Lote avenida"). */
    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    /** Precio por metro cuadrado en USD. */
    @Column(name = "precio_m2", precision = 10, scale = 2)
    private BigDecimal precioM2;

    /** Precio de lista / valor final en USD (la venta puede registrar otro precio negociado). */
    @Column(name = "precio", nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    /** Cuota inicial del plan de financiamiento del lote. */
    @Column(name = "inicial", precision = 14, scale = 2)
    private BigDecimal inicial;

    /** Saldo financiable = max(0, precio − inicial). Se recalcula al guardar. */
    @Column(name = "saldo", precision = 14, scale = 2)
    private BigDecimal saldo;

    /** Cantidad de cuotas del plan (usualmente 150, máximo 160). */
    @Column(name = "num_cuotas")
    private Integer numCuotas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    /**
     * Motivo del cambio de estado manual desde la tabla (switch "vendido").
     * Ej.: "Venta de otro vendedor, oficina actualizó el plano". Se limpia
     * al devolver el lote a DISPONIBLE. Las ventas creadas en el sistema
     * dejan este campo en null (el motivo ya está en la venta).
     */
    @Column(name = "motivo_estado", length = 300)
    private String motivoEstado;

    /**
     * Fecha de vencimiento de la separación (RESERVADO). Al vencer, el lote
     * vuelve a DISPONIBLE automáticamente. Null = no está separado.
     */
    @Column(name = "fecha_vence_separacion")
    private LocalDate fechaVinceSeparacion;

    /** Monto pagado por la separación (usual 15 días = $50, 1 mes = $100). */
    @Column(name = "monto_separacion", precision = 10, scale = 2)
    private BigDecimal montoSeparacion;

    /**
     * true = la separación la hizo OTRO vendedor (me la informó la oficina),
     * por lo que NO puedo vender ese lote. false = separación propia.
     */
    @Column(name = "separacion_ajena", nullable = false)
    private boolean separacionAjena = false;

    /** Cliente con quien se separó el lote (opcional, informativo). */
    @Column(name = "cliente_separacion", length = 150)
    private String clienteSeparacion;

    /** Nota libre de la separación (opcional). */
    @Column(name = "nota_separacion", length = 300)
    private String notaSeparacion;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * No persistido: calculado al listar. Indica que el lote tiene una venta
     * PROPIA ACTIVA en el sistema — su estado lo gobierna la venta (solo la
     * anulación lo libera). La UI oculta el switch de estado en ese caso.
     */
    @Transient
    private boolean ventaActiva;
}
