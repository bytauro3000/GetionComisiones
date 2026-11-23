package com.vendelote.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Cálculo sugerido de liquidación para un socio (con descuentos IGV/detracción). */
@Data
@AllArgsConstructor
public class LiquidacionResumenDTO {

    private Integer idSocio;
    private String socioNombre;
    private BigDecimal montoBruto;
    private BigDecimal descuentoIgv;
    private BigDecimal descuentoDetraccion;
    private BigDecimal montoNeto;
    private BigDecimal montoLiquidado;
    private BigDecimal montoPendiente;
}
