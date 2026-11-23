package com.vendelote.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioActivoDTO {

    private Long idSesion;
    private Integer idUsuario;
    private String nombreCompleto;
    private String correo;
    private String ip;
    private String userAgent;
    private LocalDateTime fechaLogueo;
    private LocalDateTime ultimoRefresh;
}
