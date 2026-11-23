package com.vendelote.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditoriaResponseDTO {

    private Long idAuditoria;
    private LocalDateTime fechaHora;
    private String usuarioEmail;
    private String usuarioRol;
    private String accion;
    private String entidad;
    private Integer idEntidad;
    private String descripcion;
    private String ipOrigen;
}
