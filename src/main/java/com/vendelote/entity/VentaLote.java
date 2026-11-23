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
 * Fila de venta ↔ lote: una venta puede agrupar varios lotes y todos quedan
 * ligados a la MISMA venta (no se generan ventas por separado).
 */
@Entity
@Table(name = "venta_lote",
        uniqueConstraints = @UniqueConstraint(name = "uk_venta_lote",
                columnNames = { "id_venta", "id_lote" }),
        indexes = @Index(name = "idx_venta_lote_lote", columnList = "id_lote"))
@Getter
@Setter
@NoArgsConstructor
public class VentaLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta_lote")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    @JsonIgnore
    private Venta venta;

    @Column(name = "id_lote", nullable = false)
    private Integer idLote;
}
