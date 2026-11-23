package com.vendelote.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioListadoDTO {

    private Integer id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String dni;
    private String rol;
    private String estado;
    private LocalDateTime fechaRegistro;
}
