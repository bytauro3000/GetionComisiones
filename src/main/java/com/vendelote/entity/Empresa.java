package com.vendelote.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vendelote.dto.CuentaBancariaDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * Empresa a la que le intermedia el vendedor (paga las comisiones y es el
 * RECEPTOR de nuestras facturas). Copia propia de cada tenant: el mismo RUC
 * puede existir en la base de otro vendedor.
 */
@Entity
@Table(name = "empresa",
        uniqueConstraints = @UniqueConstraint(name = "uk_empresa_tenant_ruc",
                columnNames = { "id_vendedor", "ruc" }),
        indexes = @Index(name = "idx_empresa_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 120)
    private String nombreComercial;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "distrito", length = 60)
    private String distrito;

    @Column(name = "provincia", length = 60)
    private String provincia;

    @Column(name = "departamento", length = 60)
    private String departamento;

    @Column(name = "telefono", length = 100)
    private String telefono;

    @Column(name = "correo", length = 100)
    private String correo;

    /** Cuentas bancarias serializadas en JSON (ver {@link #cuentas}). */
    @JsonIgnore
    @Column(name = "cuentas_bancarias", columnDefinition = "TEXT")
    private String cuentasBancarias;

    /** Cuentas bancarias pobladas al leer (no se persiste: JSON en la columna). */
    @Transient
    private List<CuentaBancariaDTO> cuentas;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
