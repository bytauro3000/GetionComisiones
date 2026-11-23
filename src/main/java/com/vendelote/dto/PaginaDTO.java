package com.vendelote.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pagina simple serializable a JSON (evita el warning de PageImpl de Spring Data). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginaDTO<T> {

    private List<T> elementos;
    private int pagina;
    private int tamano;
    private long totalElementos;
    private int totalPaginas;
}
