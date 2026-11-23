package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.vendelote.enums.EstadoSeguimiento;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Fila de seguimiento: un cliente/prospecto con las proformas que solicitó
 * en el rango de fechas consultado.
 */
@Data
@AllArgsConstructor
public class ProformaSeguimientoDTO {

    private Integer idCliente;
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteTelefono;
    private boolean prospecto;
    private EstadoSeguimiento estadoSeguimiento;
    private String notaSeguimiento;
    private LocalDate proximoContacto;

    private int cantidadProformas;
    private LocalDate ultimaFecha;
    private String ultimaProformaNumero;
    private Integer ultimaProformaId;
    private BigDecimal totalOfertado;
}
