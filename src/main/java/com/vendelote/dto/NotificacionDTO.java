package com.vendelote.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificacionDTO {

    private Integer id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Integer idReferencia;
    private boolean leida;
    private LocalDateTime fechaCreacion;
}
