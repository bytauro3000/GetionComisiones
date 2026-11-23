package com.vendelote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Correlativo de proformas POR VENDEDOR y año (PF-2026-0001).
 * La proforma es un documento del vendedor y puede incluir lotes de varias
 * empresas, por eso el número ya no depende de la empresa.
 */
@Entity
@Table(name = "secuencia_proforma", uniqueConstraints = @UniqueConstraint(
        name = "uk_secuencia_proforma_tenant_anio", columnNames = { "id_vendedor", "anio" }))
@Getter
@Setter
@NoArgsConstructor
public class SecuenciaProforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_secuencia_proforma")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "ultimo_numero", nullable = false)
    private Integer ultimoNumero;
}
