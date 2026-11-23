package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Entrada de reparto: un socio con su participación en el pago inicial y/o en
 * las letras. En cada componente, si el "monto" viene informado prevalece
 * sobre el porcentaje.
 */
@Data
public class ComisionRepartoSocioDTO {

    @NotNull(message = "El socio es obligatorio")
    private Integer idSocio;

    private boolean participaInicial;
    /** 0..1 */
    private BigDecimal porcentajeInicial;
    private BigDecimal montoInicial;

    private boolean participaCuotas;
    /** 0..1 */
    private BigDecimal porcentajeCuotas;
    /** Monto fijo POR CADA cuota. */
    private BigDecimal montoCuotaSocio;
}
