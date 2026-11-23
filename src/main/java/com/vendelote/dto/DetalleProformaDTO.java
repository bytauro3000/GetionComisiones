package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
}
