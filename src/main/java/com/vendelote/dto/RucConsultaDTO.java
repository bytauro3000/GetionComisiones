package com.vendelote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Respuesta de Declecta/SUNAT: consulta de RUC (datos del emisor/cliente). */
@Data
public class RucConsultaDTO {

    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("numero_documento")
    private String numeroDocumento;

    private String estado;

    private String condicion;

    private String direccion;

    private String distrito;

    private String provincia;

    private String departamento;

    private String ubigeo;
}
