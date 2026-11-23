package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Integer idEmpresa;

    @NotNull(message = "Debe seleccionar un proyecto")
    private Integer idProyecto;

    @NotBlank(message = "La manzana es obligatoria")
    @Size(max = 10)
    private String manzana;

    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 10)
    private String numero;

    @Size(max = 150, message = "La descripción no puede superar 150 caracteres")
    private String descripcion;

    @Positive(message = "El área debe ser positiva")
    private BigDecimal areaM2;

    @PositiveOrZero(message = "El precio/m² no puede ser negativo")
    private BigDecimal precioM2;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precio;

    /** Cuota inicial del plan de financiamiento. */
    @PositiveOrZero(message = "La inicial no puede ser negativa")
    private BigDecimal inicial;

    /**
     * Cantidad de cuotas del plan (usualmente 150, máximo 160).
     * El saldo NO se recibe: lo calcula el backend (max(0, precio − inicial)).
     */
    @PositiveOrZero(message = "El número de cuotas no puede ser negativo")
    @Max(value = 160, message = "El máximo de cuotas es 160")
    private Integer numCuotas;

    /** DISPONIBLE | RESERVADO | VENDIDO (opcional, por defecto DISPONIBLE). */
    @Pattern(regexp = "DISPONIBLE|RESERVADO|VENDIDO", message = "Estado no válido")
    private String estado;
}
