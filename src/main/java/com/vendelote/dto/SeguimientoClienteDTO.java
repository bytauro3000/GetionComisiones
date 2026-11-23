package com.vendelote.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Actualización del seguimiento comercial de un cliente/prospecto. */
@Data
public class SeguimientoClienteDTO {

    @Size(max = 20)
    private String estado;

    @Size(max = 300)
    private String nota;

    private LocalDate proximoContacto;
}
