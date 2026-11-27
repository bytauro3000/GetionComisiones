package com.vendelote.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Socio con quien se comparte la comisión (referencia del tenant, sin login). */
@Data
public class SocioDTO {

    private Integer id;

    @NotBlank(message = "Los nombres del socio son obligatorios")
    @Size(max = 120)
    private String nombres;

    @Size(max = 120)
    private String apellidos;

    /** DNI | CE | RUC | OTRO. */
    @Size(max = 20)
    private String tipoDocumento;

    @Size(max = 15)
    private String documento;

    @Size(max = 100)
    private String telefono;

    private Boolean activo = true;

    private LocalDateTime fechaRegistro;
}
