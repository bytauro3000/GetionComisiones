package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Alta de venta AGRUPADA: uno o varios lotes y uno o varios clientes bajo la
 * MISMA venta. precioVenta = total de la venta (suma de precios de lista,
 * editable por si hay descuento). En CONTADO la inicial se fija sola = total.
 */
@Data
public class VentaCrearDTO {

    @NotEmpty(message = "Debe seleccionar al menos un lote")
    private List<Integer> idsLotes;

    @NotEmpty(message = "Debe seleccionar al menos un cliente")
    private List<Integer> idsClientes;

    @NotNull(message = "La fecha de venta es obligatoria")
    private LocalDate fechaVenta;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser positivo")
    private BigDecimal precioVenta;

    /** Solo FINANCIADO: inicial pagada el día de la firma (> 0 y < precio). */
    @Positive
    private BigDecimal montoInicial;

    @NotBlank(message = "La modalidad es obligatoria")
    @Size(max = 20)
    private String modalidad;

    /** Monto total de comisión en USD pactado (null = % del parámetro vigente). */
    @PositiveOrZero(message = "La comisión manual no puede ser negativa")
    private BigDecimal comisionManual;

    @Size(max = 300)
    private String notas;

    /**
     * Override del % del INICIAL DEL CLIENTE que se paga como anticipo
     * (ej. 0.30 = 30%). Si es null se usa el parámetro vigente.
     */
    @PositiveOrZero
    private BigDecimal porcentajeAnticipo;

    /** Override del monto de cada cuota (letra). Null = parámetro vigente. */
    @PositiveOrZero
    private BigDecimal montoCuota;

    /** Reparto con socios (máximo 2: tú + 2 socios = 3 partes). */
    @Size(max = 2, message = "Máximo 2 socios por comisión")
    private List<ComisionRepartoSocioDTO> socios;
}
