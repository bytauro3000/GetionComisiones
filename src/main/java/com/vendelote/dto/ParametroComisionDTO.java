package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * Parámetros de comisión (alcance GENERAL si idEmpresa = null, o por EMPRESA).
 * Los porcentajes se expresan en decimal: 0.0300 = 3%.
 */
@Data
public class ParametroComisionDTO {

    /** Null = GENERAL para todo el tenant. */
    private Integer idEmpresa;

    @NotNull(message = "El porcentaje de comisión es obligatorio")
    @DecimalMin(value = "0", message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "1", message = "El porcentaje debe ser menor a 1 (use 0.03 = 3%)")
    private BigDecimal porcentajeComision = new BigDecimal("0.0300");

    @NotNull(message = "El porcentaje de contado es obligatorio")
    @DecimalMin(value = "0")
    @DecimalMax(value = "1")
    private BigDecimal porcentajeComisionContado = new BigDecimal("0.0300");

    @NotNull(message = "El porcentaje de anticipo es obligatorio")
    @DecimalMin(value = "0")
    @DecimalMax(value = "1")
    private BigDecimal anticipoPorcentaje = new BigDecimal("0.3000");

    /** Anticipo fijo en USD (sobreescribe el % si está informado). */
    @PositiveOrZero
    private BigDecimal anticipoMontoFijo;

    @NotNull(message = "El monto de cuota es obligatorio")
    @PositiveOrZero(message = "El monto de cuota no puede ser negativo")
    private BigDecimal montoCuota = new BigDecimal("50.00");

    @NotNull(message = "Los meses de espera son obligatorios")
    @PositiveOrZero
    private Integer mesesEspera = 8;

    /** Null = vigente desde hoy. */
    private LocalDate fechaInicioVigencia;

    private LocalDate fechaFinVigencia;
}
