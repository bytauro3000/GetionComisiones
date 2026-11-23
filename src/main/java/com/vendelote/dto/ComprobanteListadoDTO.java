package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Comprobante listado con pagos que cubre. */
@Data
@AllArgsConstructor
public class ComprobanteListadoDTO {

    private Integer id;
    private String tipo;
    private String serie;
    private String correlativo;
    private String idApiSunat;
    private LocalDate fechaEmision;
    private Integer idEmpresa;
    private String empresaNombre;
    private String empresaRuc;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String concepto;
    private boolean aplicaDetraccion;
    private BigDecimal montoDetraccion;
    private String estado;
    private List<PagoComisionDTO> pagos;
    private LocalDateTime fechaRegistro;
}
