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

    /** Snapshot de la empresa del lote (puede haber varias en una proforma). */
    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @Column(name = "empresa_nombre", length = 150)
    private String empresaNombre;

    /** Snapshot del programa/proyecto del lote. */
    @Column(name = "proyecto", length = 120)
    private String proyecto;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    /** Inicial pactada para este lote (puede reducirse respecto a la del lote). */
    @Column(name = "inicial", precision = 14, scale = 2)
    private BigDecimal inicial;

    /** N° de cuotas del plan (snapshot del lote o ajustado). */
    @Column(name = "num_cuotas")
    private Integer numCuotas;
}
