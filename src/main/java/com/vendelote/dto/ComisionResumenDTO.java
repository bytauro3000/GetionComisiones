package com.vendelote.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Resumen de la comisión (para listados y detalle de venta). */
@Data
@AllArgsConstructor
public class ComisionResumenDTO {

    private Integer id;
    private Integer idVenta;
    private BigDecimal porcentajeAplicado;
    private BigDecimal montoTotal;
    private BigDecimal montoAnticipo;
    private BigDecimal montoSaldo;
    private BigDecimal montoPagado;
    private BigDecimal montoCuota;
    private Integer cantidadCuotas;
    private String fechaInicioCuotas;
    private String estado;
}
