package com.vendelote.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Vista del reparto de una comisión con un socio (incluye el nombre). */
@Data
@AllArgsConstructor
public class ComisionRepartoDTO {

    private Integer id;
    private Integer idSocio;
    private String socioNombre;

    private boolean participaInicial;
    private BigDecimal porcentajeInicial;
    private BigDecimal montoInicial;

    private boolean participaCuotas;
    private BigDecimal porcentajeCuotas;
    private BigDecimal montoCuotaSocio;
}
