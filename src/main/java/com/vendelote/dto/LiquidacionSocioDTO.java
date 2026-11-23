package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/** Liquidación registrada a un socio (entrada y salida). */
@Data
public class LiquidacionSocioDTO {

    private Integer id;
    private Integer idSocio;
    private String socioNombre;
    private LocalDate fecha;
    private BigDecimal montoBruto;
    private BigDecimal descuentoIgv;
    private BigDecimal descuentoDetraccion;
    private BigDecimal montoNeto;
    private String referencia;
    private String notas;
    private LocalDateTime fechaRegistro;
}
