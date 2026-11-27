package com.vendelote.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRegistroDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    private String correo;

    /** Obligatorio en creación; opcional en edición (vacío = no cambiar). */
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String contrasena;

    @Size(max = 100)
    private String telefono;

    @Pattern(regexp = "^$|^\\d{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    /** DNI | CE | RUC | OTRO. */
    @Size(max = 20)
    private String tipoDocumento;

    @Size(max = 15)
    private String numDocumento;

    /** ADMINISTRADOR | VENDEDOR */
    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    /** activo | inactivo (opcional, por defecto activo) */
    private String estado;
}
