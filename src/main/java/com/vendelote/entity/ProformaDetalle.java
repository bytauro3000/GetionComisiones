package com.vendelote.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fila de proforma: un lote del programa con su precio de oferta.
 * Guarda descripción/área como copia (snapshot) para que la proforma
 * histórica no cambie si el lote se modifica o elimina después.
 */
@Entity
@Table(name = "proforma_detalle")
@Getter
@Setter
@NoArgsConstructor
public class ProformaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proforma_detalle")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proforma", nullable = false)
    @JsonIgnore
    private Proforma proforma;

    @Column(name = "id_lote")
    private Integer idLote;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;
}
