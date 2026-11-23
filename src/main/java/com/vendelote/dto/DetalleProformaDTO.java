package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class DetalleProformaDTO {

    private Integer id;

    @NotNull(message = "Seleccione un lote")
    private Integer idLote;

    /** Precio de oferta en USD (viene precargado del lote; se puede negociar). */
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precioUnitario;

    /** Inicial pactada (puede reducirse respecto a la del lote). */
    @PositiveOrZero(message = "La inicial no puede ser negativa")
    private BigDecimal inicial;

    @Min(value = 0, message = "El número de cuotas no puede ser negativo")
    @Max(value = 160, message = "El número de cuotas no puede superar 160")
    private Integer numCuotas;
}
