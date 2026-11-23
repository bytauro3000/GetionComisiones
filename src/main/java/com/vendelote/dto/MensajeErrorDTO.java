package com.vendelote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cuerpo de respuesta de errores (400/401/500). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeErrorDTO {

    private String mensaje;
}
