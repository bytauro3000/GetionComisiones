package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Configuración de facturación del tenant (credenciales api-sunat).
 * Los GET NUNCA devuelven api_key/api_secret reales: solo "configurado".
 * En el PUT, api_key/api_secret vacíos = conservar los existentes.
 */
@Data
public class IntegracionSunatDTO {

    @Size(max = 150)
    private String apiUrl;

    /** Solo escritura (nunca se lee). Vacío en PUT = no cambiar. */
    @Size(max = 100)
    private String apiKey;

    @Size(max = 120)
    private String apiSecret;

    private boolean apiKeyConfigurado;
    private boolean apiSecretConfigurado;

    /** Serie de facturas en api-sunat (F001-F999). */
    @Size(max = 4)
    private String serieFactura = "F001";

    private boolean aplicarDetraccion = true;

    /** Cat. 54: 037 = Demás servicios gravados. */
    @Pattern(regexp = "^$|^\\d{3}$", message = "El código de bien debe tener 3 dígitos")
    private String codBienDetraccion = "037";

    @DecimalMin(value = "0")
    @DecimalMax(value = "100")
    private BigDecimal porcentajeDetraccion = new BigDecimal("10.00");

    /** Umbral en USD ≈ S/ 700 (regla SUNAT de detracciones). */
    @DecimalMin(value = "0")
    private BigDecimal umbralDetraccion = new BigDecimal("190.00");

    @Size(max = 25)
    private String ctaBanco;

    /** Cat. 59: 003 = Transferencia. */
    @Pattern(regexp = "^$|^\\d{3}$", message = "El código de medio de pago debe tener 3 dígitos")
    private String codMedioPago = "003";
}
