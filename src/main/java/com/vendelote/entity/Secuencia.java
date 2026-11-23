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
 * Correlativo por empresa y año para proformas (PF-2026-0001).
 * Se incrementa con bloqueo pesimista para evitar números repetidos.
 */
@Entity
@Table(name = "secuencia", uniqueConstraints = @UniqueConstraint(
        name = "uk_secuencia_empresa_anio", columnNames = { "id_empresa", "anio" }))
@Getter
@Setter
@NoArgsConstructor
public class Secuencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_secuencia")
    private Integer id;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "ultimo_numero", nullable = false)
    private Integer ultimoNumero;
}
