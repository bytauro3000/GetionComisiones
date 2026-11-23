package com.vendelote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fila de venta ↔ cliente: una venta puede tener varios compradores
 * relacionados a la misma venta.
 */
@Entity
@Table(name = "venta_cliente",
        uniqueConstraints = @UniqueConstraint(name = "uk_venta_cliente",
                columnNames = { "id_venta", "id_cliente" }),
        indexes = @Index(name = "idx_venta_cliente_cliente", columnList = "id_cliente"))
@Getter
@Setter
@NoArgsConstructor
public class VentaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta_cliente")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    @JsonIgnore
    private Venta venta;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;
}
