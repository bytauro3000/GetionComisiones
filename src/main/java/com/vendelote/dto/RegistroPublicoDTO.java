package com.vendelote.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Auto-registro público de un vendedor (queda PENDIENTE de aprobación). */
@Data
public class RegistroPublicoDTO {

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

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String contrasena;

    @Size(max = 100)
    private String telefono;

    /** Opcional: si se envía, se autocompleta desde RENIEC (Declecta). */
    @Pattern(regexp = "^$|^\\d{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @Size(max = 20)
    private String tipoDocumento;

    @Size(max = 15)
    private String numDocumento;
}
