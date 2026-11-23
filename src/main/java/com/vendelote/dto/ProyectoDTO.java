package com.vendelote.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProyectoDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Integer idEmpresa;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 120)
    private String nombre;

    @Size(max = 200)
    private String ubicacion;

    private BigDecimal latitud;

    private BigDecimal longitud;

    /** ACTIVO | INACTIVO (opcional, por defecto ACTIVO). */
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Estado no válido")
    private String estado;
}
