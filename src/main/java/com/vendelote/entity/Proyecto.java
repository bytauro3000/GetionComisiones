package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vendelote.enums.EstadoProyecto;

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
 * Proyecto inmobiliario (urbanización/programa) de una empresa. El lote
 * referencia este proyecto por FK y además guarda el nombre como texto
 * (sincronizado) para no romper facturación/ventas/comisiones.
 */
@Entity
@Table(name = "proyecto",
        uniqueConstraints = @UniqueConstraint(name = "uk_proyecto",
                columnNames = { "id_vendedor", "id_empresa", "nombre" }),
        indexes = @Index(name = "idx_proyecto_v", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proyecto implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proyecto")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    /** Dirección legible (la rellena el mapa o el usuario). */
    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    /** Nombre del archivo del plano en uploads/planos (no se expone en la API). */
    @JsonIgnore
    @Column(name = "plano_ruta")
    private String planoRuta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoProyecto estado = EstadoProyecto.ACTIVO;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** true si hay plano subido (propiedad calculada para el frontend). */
    @Transient
    public boolean isTienePlano() {
        return planoRuta != null && !planoRuta.isBlank();
    }
}
