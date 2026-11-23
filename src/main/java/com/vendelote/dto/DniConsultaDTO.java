package com.vendelote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Respuesta de Declecta/RENIEC: consulta de DNI. */
@Data
public class DniConsultaDTO {

    @JsonProperty("first_name")
    private String nombres;

    @JsonProperty("first_last_name")
    private String apellidoPaterno;

    @JsonProperty("second_last_name")
    private String apellidoMaterno;

    @JsonProperty("full_name")
    private String nombreCompleto;

    @JsonProperty("document_number")
    private String numeroDocumento;
}
