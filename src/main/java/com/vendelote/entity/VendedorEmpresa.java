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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Vínculo (tenant) vendedor ↔ empresa: historial de con quién trabaja cada vendedor. */
@Entity
@Table(name = "vendedor_empresa",
        uniqueConstraints = @UniqueConstraint(name = "uk_vend_emp",
                columnNames = { "id_vendedor", "id_empresa" }),
        indexes = @Index(name = "idx_vend_emp_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendedorEmpresa implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vendedor_empresa")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
