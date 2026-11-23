package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.vendelote.enums.EstadoComprobante;
import com.vendelote.enums.TipoComprobante;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comprobante electrónico (factura/boleta/NC/ND) emitido por el VENDEDOR a
 * la EMPRESA (emisor = nosotros con nuestro RUC; receptor = la empresa que
 * nos paga). Los pagos que cubre se vinculan por comprobante_pago (N:M),
 * lo que permite una factura con varios lotes y su descripción derivada.
 *
 * Flujo: se registra localmente → POST /api/v1/facturas (api-sunat firma y
 * encola) → guardamos id_api_sunat → estado EN_PROCESO → se consulta hasta
 * ACEPTADO/RECHAZADO (CDR).
 */
@Entity
@Table(name = "comprobante",
        indexes = {
            @Index(name = "idx_comprobante_tenant", columnList = "id_vendedor"),
            @Index(name = "idx_comprobante_tenant_empresa", columnList = "id_vendedor,id_empresa")
        })
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoComprobante tipo;

    /** Serie y correlativo asignados por api-sunat al emitir. */
    @Column(name = "serie", length = 20)
    private String serie;

    @Column(name = "correlativo", length = 20)
    private String correlativo;

    /** id del comprobante en la API de facturación (para pdf/xml/cdr/estado). */
    @Column(name = "id_api_sunat", length = 40)
    private String idApiSunat;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /** Empresa receptora de la factura. */
    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    /** Suma de los pagos cubiertos (sin IGV). */
    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv", nullable = false, precision = 14, scale = 2)
    private BigDecimal igv;

    /** subtotal + igv (USD). */
    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    /** 18.00 por defecto (snapshot al emitir). */
    @Column(name = "porcentaje_igv", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIgv = new BigDecimal("18.00");

    /** Descripción en el XML: qué lotes se están pagando. */
    @Column(name = "concepto", nullable = false, length = 300)
    private String concepto;

    /** ¿Se aplicó detracción al emitir (total > umbral y habilitado)? */
    @Column(name = "aplica_detraccion", nullable = false)
    private boolean aplicaDetraccion;

    @Column(name = "monto_detraccion", precision = 14, scale = 2)
    private BigDecimal montoDetraccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoComprobante estado = EstadoComprobante.EMITIDO;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
