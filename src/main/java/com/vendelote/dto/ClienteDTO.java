package com.vendelote.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteDTO {

    /** DNI | CE | RUC | OTRO (por defecto DNI). */
    @Size(max = 20)
    private String tipoDocumento = "DNI";

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 15)
    private String numDocumento;

    @NotBlank(message = "Los nombres / razón social son obligatorios")
    @Size(max = 120)
    private String nombres;

    /** Asociado dueño del cliente (opcional; null = jefe). */
    private Integer idVendedorAsociado;

    @Size(max = 120)
    private String apellidos;

    @Size(max = 100)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    private String correo;

    @Size(max = 200)
    private String direccion;

    /** true = prospecto (no obliga a completar todos los datos). */
    private Boolean prospecto;

    /** NUEVO | CONTACTADO | INTERESADO | NO_INTERESADO | GANADO */
    @Size(max = 20)
    private String estadoSeguimiento;

    @Size(max = 300)
    private String notaSeguimiento;

    private LocalDate proximoContacto;
}
