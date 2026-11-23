package com.vendelote.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProformaDTO {

    private Integer id;

    /** Empresa referencial (opcional): la proforma puede tener lotes de varias empresas. */
    private Integer idEmpresa;

    @NotNull(message = "El cliente es obligatorio")
    private Integer idCliente;

    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;

    /** Solo informativo en respuesta; el estado se cambia vía PATCH. */
    @Pattern(regexp = "EMITIDA|ACEPTADA|ANULADA", message = "Estado no válido")
    private String estado;

    @NotEmpty(message = "Agregue al menos un lote a la proforma")
    @Valid
    private List<DetalleProformaDTO> detalles;
}
