package com.vendelote.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.vendelote.crypto.EncryptedStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

/**
 * Credenciales y configuración de facturación POR TENANT: cada vendedor
 * emite con SU RUC, así que cada uno tiene su propia cuenta en api-sunat
 * (certificado, series) y aquí guardamos su api_key/api_secret.
 *
 * NOTA DE SEGURIDAD: las credenciales api_key/api_secret se cifran en reposo
 * con AES/GCM (ver {@link com.vendelote.crypto.EncryptedStringConverter}).
 */
@Entity
@Table(name = "integracion_sunat",
        uniqueConstraints = @UniqueConstraint(name = "uk_integracion_tenant",
                columnNames = "id_vendedor"),
        indexes = @Index(name = "idx_integracion_tenant", columnList = "id_vendedor"))
@EntityListeners(TenantAwareListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracionSunat implements TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_integracion")
    private Integer id;

    @Column(name = "id_vendedor", nullable = false)
    private Integer idVendedor;

    /** Base URL de api-sunat; null = usar la global de la aplicación. */
    @Column(name = "api_url", length = 150)
    private String apiUrl;

    /** Cifrado en reposo (AES/GCM). El largo cubre el texto cifrado en base64. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "api_key", length = 512)
    private String apiKey;

    /** Cifrado en reposo (AES/GCM). El largo cubre el texto cifrado en base64. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "api_secret", length = 512)
    private String apiSecret;

    /** Serie de facturas configurada en api-sunat (debe empezar con F; ej: F001). */
    @Column(name = "serie_factura", nullable = false, length = 4)
    private String serieFactura = "F001";

    /** ¿Aplicar detracción cuando el total supera el umbral? (regla SUNAT > S/700). */
    @Column(name = "aplica_detraccion", nullable = false)
    private boolean aplicarDetraccion = true;

    /** Catálogo 54: 037 = Demás servicios gravados. */
    @Column(name = "cod_bien_detraccion", nullable = false, length = 4)
    private String codBienDetraccion = "037";

    /** 10.00% típico para 037. */
    @Column(name = "porcentaje_detraccion", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDetraccion = new BigDecimal("10.00");

    /**
     * Umbral para aplicar detracción, en USD (la factura se emite en USD).
     * Default 190.00 USD ≈ S/ 700 (regla SUNAT: operaciones > S/ 700).
     * Ajustable por vendedor según el tipo de cambio que use.
     */
    @Column(name = "umbral_detraccion", nullable = false, precision = 14, scale = 2)
    private BigDecimal umbralDetraccion = new BigDecimal("190.00");

    /** Cuenta de abono donde el receptor deposita la detracción. */
    @Column(name = "cta_banco", length = 25)
    private String ctaBanco;

    /** Código SUNAT Cat. 59 de medio de pago (003 = Transferencia). */
    @Column(name = "cod_medio_pago", nullable = false, length = 4)
    private String codMedioPago = "003";

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
