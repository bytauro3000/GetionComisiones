package com.vendelote.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Listado enriquecido de comisiones. */
@Data
@AllArgsConstructor
public class ComisionListadoDTO {

    private Integer id;
    private Integer idVenta;
    private String estado;

    private BigDecimal montoTotal;
    private BigDecimal montoPagado;
    private BigDecimal montoSaldo;
    private BigDecimal montoAnticipo;
    private Integer cantidadCuotas;

    private Integer idLote;
    private String loteEtiqueta;
    private Integer idEmpresa;
    private String empresaNombre;
    private String clienteNombre;

    private Integer pagosPendientes;
    private BigDecimal montoPendiente;

    private List<PagoComisionDTO> pagos;
}
