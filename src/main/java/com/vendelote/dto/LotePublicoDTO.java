package com.vendelote.dto;

/** Estado público de un lote: sin precios, clientes ni datos internos. */
public record LotePublicoDTO(
        String programa,
        String manzana,
        String numero,
        String estado
) {
}
