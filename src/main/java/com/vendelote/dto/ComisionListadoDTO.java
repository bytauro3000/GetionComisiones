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
    private BigDecimal porcentajeAnticipo;
    private BigDecimal montoCuota;
    private Integer cantidadCuotas;

    private Integer idLote;
    private String loteEtiqueta;
    private Integer idEmpresa;
    private String empresaNombre;
    private String clienteNombre;

    /** Asociado atribuido y su reparto informativo 50/50 con el jefe. */
    private Integer idVendedorAsociado;
    private String vendedorAsociado;
    private BigDecimal montoJefe;
    private BigDecimal montoAsociado;

    private Integer pagosPendientes;
    private BigDecimal montoPendiente;

    /** Socios con quienes se reparte el inicial y/o las letras (máx. 2). */
    private List<ComisionRepartoDTO> repartos;

    private List<PagoComisionDTO> pagos;
}
