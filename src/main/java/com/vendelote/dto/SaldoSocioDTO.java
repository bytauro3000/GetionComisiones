package com.vendelote.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Saldo por liquidar de un socio. */
@Data
@AllArgsConstructor
public class SaldoSocioDTO {

    private Integer idSocio;
    private String socioNombre;
    private BigDecimal montoGenerado;
    private BigDecimal montoLiquidado;
    private BigDecimal montoPendiente;
}
