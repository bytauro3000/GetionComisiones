package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Pago cobrado aún sin factura (candidato para emitir comprobante). */
@Data
@AllArgsConstructor
public class PagoDisponibleDTO {

    private Integer idPago;
    private Integer idComision;
    private String tipo;
    private Integer numeroCuota;
    private BigDecimal monto;
    private LocalDate fechaPago;
    private String formaPago;
    private String referencia;

    private Integer idLote;
    private String loteEtiqueta; // "La Florida de Torre Blanca Mz.B1 Lt.15"

    private Integer idEmpresa;
    private String empresaNombre;
}
