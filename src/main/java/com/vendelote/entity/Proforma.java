package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vendelote.enums.EstadoProforma;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Proforma comercial: oferta de lotes del programa para entregar al cliente.
 * No afecta el estado de los lotes (son inventario, no ventas).
 * Número correlativo "PF-{anio}-{0001}" por empresa y año.
 */
@Entity
@Table(name = "proforma", uniqueConstraints = @UniqueConstraint(
        name = "uk_proforma_numero", columnNames = { "id_vendedor", "numero" }))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Proforma implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proforma")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    /** Número correlativo emitido por el backend, p.ej. "PF-2026-0001". */
    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "fecha", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fecha;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoProforma estado = EstadoProforma.EMITIDA;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<ProformaDetalle> detalles = new ArrayList<>();
}
