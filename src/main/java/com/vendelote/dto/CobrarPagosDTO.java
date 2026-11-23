package com.vendelote.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Registro de cobro: marca 1 o varios pagos (seleccionados) como PAGADOS. */
@Data
public class CobrarPagosDTO {

    /** ids de pago (dentro de la comisión) a marcar como cobrados. */
    @NotEmpty(message = "Seleccione al menos un pago")
    private List<Integer> pagos;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    /** TRANSFERENCIA | DEPOSITO | EFECTIVO | YAPE | PLIN | OTRO */
    private String formaPago;

    private String referencia;

    private String notas;
}
