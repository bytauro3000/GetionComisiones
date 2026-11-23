package com.vendelote.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SesionResumenDTO {

    private long totalActivas;
    private long visitasHoy;
    private List<UsuarioActivoDTO> sesiones;
}
