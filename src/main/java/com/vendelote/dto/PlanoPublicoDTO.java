package com.vendelote.dto;

import java.util.List;

/** Datos mínimos y de solo lectura que puede ver un cliente con el enlace público. */
public record PlanoPublicoDTO(
        String proyecto,
        String programa,
        List<LotePublicoDTO> lotes
) {
}
