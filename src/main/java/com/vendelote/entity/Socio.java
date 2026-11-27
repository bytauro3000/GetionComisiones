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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Socio con quien el vendedor (tenant) comparte la comisión de algunas ventas.
 * Es una referencia interna del tenant: NO inicia sesión ni es un tenant aparte.
 */
@Entity
@Table(name = "socio",
        indexes = @Index(name = "idx_socio_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Socio implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "apellidos", length = 120)
    private String apellidos;

    /** DNI | CE | RUC | OTRO. */
    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "documento", length = 15)
    private String documento;

    @Column(name = "telefono", length = 100)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
