package com.vendelote.dto;

import java.time.LocalDateTime;

public record AsociadoResumenDTO(
        Integer id,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        String dni,
        String estado,
        LocalDateTime fechaRegistro
) {
}
