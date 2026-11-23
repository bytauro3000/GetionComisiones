package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Emisión de comprobante: seleccionas los pagos cobrados que cubre y el
 * sistema arma items/descripción por lote, calcula IGV y detracción.
 */
@Data
public class ComprobanteCrearDTO {

    /** FACTURA | BOLETA (MVP: solo FACTURA habilitado). */
    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipo;

    /** Receptor: empresa a la que se le factura (debe ser la dueña de los pagos). */
    @NotNull(message = "La empresa es obligatoria")
    private Integer idEmpresa;

    /** ids de pagos PAGADOS aún sin comprobante. */
    @NotEmpty(message = "Seleccione al menos un pago cobrado")
    private List<Integer> pagos;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    /** Descripción libre extra (se agrega a la generada por lote). */
    private String concepto;
}
