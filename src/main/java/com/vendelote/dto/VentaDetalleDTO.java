package com.vendelote.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Detalle completo de una venta: datos + comisión + pagos programados. */
@Data
@AllArgsConstructor
public class VentaDetalleDTO {

    private Integer id;
    private LocalDate fechaVenta;
    private String modalidad;
    private String estado;
    private BigDecimal precioVenta;
    private BigDecimal montoInicial;
    private LocalDate fechaInicioCuotas;
    private String notas;

    /** Etiqueta de los lotes (1 lote → "Proyecto Mz.A Lt.1"; varios → "Mz.A Lt.1, Mz.B Lt.2 (3 lotes)"). */
    private String loteEtiqueta;
    private Integer idEmpresa;
    private String empresaNombre;

    /** Nombres de los compradores unidos por ", ". */
    private String clienteNombre;
    /** Documentos de los compradores unidos por ", " (ej. "DNI 12345678, CE 00123456"). */
    private String clienteDocumento;

    private ComisionResumenDTO comision;
    private List<PagoComisionDTO> pagos;

    private LocalDateTime fechaRegistro;
}
