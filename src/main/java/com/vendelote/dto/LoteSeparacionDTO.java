package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Separación (reserva) de un lote: el vendedor aparta el lote por un plazo
 * pactado (usual 15 días = $50, 1 mes = $100) para poder cerrarlo con su
 * cliente. Si la separación es de otro vendedor, el lote queda bloqueado
 * para la venta.
 */
@Data
public class LoteSeparacionDTO {

    /** Fecha de vencimiento de la separación. */
    @NotNull(message = "Debe indicar la fecha de vencimiento")
    private LocalDate fechaVence;

    /** Monto pagado por la separación (opcional). */
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    private BigDecimal monto;

    /** true = la separación la hizo otro vendedor (bloquea la venta). */
    private boolean ajena;

    /** Motivo / nota de la separación (opcional). */
    @Size(max = 300, message = "La nota no puede superar 300 caracteres")
    private String nota;
}
