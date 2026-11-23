package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Vista JSON de un pago de comisión. */
@Data
@AllArgsConstructor
public class PagoComisionDTO {

    private Integer id;
    private Integer idComision;
    private String tipo;
    private Integer numeroCuota;
    private BigDecimal monto;
    private LocalDate fechaProgramada;
    private LocalDate fechaPago;
    private String estado;
    private String formaPago;
    private String referencia;
    private String notas;
    private LocalDateTime fechaRegistro;
}
