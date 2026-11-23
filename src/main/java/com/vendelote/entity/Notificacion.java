package com.vendelote.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Notificación interna del vendedor (avisos de separaciones por vencer, etc.). */
@Entity
@Table(name = "notificacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacion_tenant_clave",
                columnNames = { "id_vendedor", "clave" }),
        indexes = @Index(name = "idx_notificacion_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "mensaje", nullable = false, length = 300)
    private String mensaje;

    /** Id del registro relacionado (ej. id del lote). */
    @Column(name = "id_referencia")
    private Integer idReferencia;

    /** Clave idempotente para no duplicar el mismo aviso. */
    @Column(name = "clave", length = 100)
    private String clave;

    @Column(name = "leida", nullable = false)
    private boolean leida = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
