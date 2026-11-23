package com.vendelote.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoSeguimiento;
import com.vendelote.enums.TipoDocumento;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comprador del terreno (persona que firma el contrato). Copia propia por
 * tenant: se autocompleta desde Declecta/RENIEC y se reutiliza en ventas futuras.
 */
@Entity
@Table(name = "cliente",
        uniqueConstraints = @UniqueConstraint(name = "uk_cliente_tenant_doc",
                columnNames = { "id_vendedor", "num_documento" }),
        indexes = @Index(name = "idx_cliente_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento = TipoDocumento.DNI;

    @Column(name = "num_documento", nullable = false, length = 15)
    private String numDocumento;

    @Column(name = "nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "apellidos", length = 120)
    private String apellidos;

    @Column(name = "telefono", length = 100)
    private String telefono;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "direccion", length = 200)
    private String direccion;

    /**
     * true = prospecto (datos básicos: documento, nombre y celular).
     * Se completa el registro completo cuando separa o compra.
     */
    @Column(name = "prospecto", nullable = false)
    private boolean prospecto = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_seguimiento", nullable = false, length = 20)
    private EstadoSeguimiento estadoSeguimiento = EstadoSeguimiento.NUEVO;

    @Column(name = "nota_seguimiento", length = 300)
    private String notaSeguimiento;

    /** Próxima fecha para contactar al prospecto/cliente. */
    @Column(name = "proximo_contacto")
    private LocalDate proximoContacto;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
