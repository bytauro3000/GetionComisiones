package com.vendelote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos del usuario autenticado (su propio perfil). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfilDTO {

    private Integer id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String dni;
    private String rol;
}
