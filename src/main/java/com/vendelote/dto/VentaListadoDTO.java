package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Fila del listado de ventas con datos de lote, cliente y empresa resueltos. */
@Data
@AllArgsConstructor
public class VentaListadoDTO {

    private Integer id;
    private LocalDate fechaVenta;
    private String modalidad;
    private String estado;
    private BigDecimal precioVenta;
    private BigDecimal montoInicial;

    /** Etiqueta de los lotes de la venta: 1 lote → "Proyecto Mz.A Lt.1"; varios → "Mz.A Lt.1, Mz.B Lt.2 (3 lotes)". */
    private String loteEtiqueta;

    /** Nombres de los compradores unidos por ", ". */
    private String clienteNombre;

    private Integer idEmpresa;
    private String empresaNombre;

    private BigDecimal comisionTotal;   // null si aún no existe comisión
    private BigDecimal comisionPagada;  // null si aún no existe comisión
    private String comisionEstado;      // null si aún no existe comisión

    private LocalDateTime fechaRegistro;
}
