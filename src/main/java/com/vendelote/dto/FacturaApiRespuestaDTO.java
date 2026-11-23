package com.vendelote.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Respuesta de api-sunat: {estado, mensaje, datos:{...}} (factura creada/consultada). */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaApiRespuestaDTO {

    private String estado;

    private String mensaje;

    private Datos datos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Datos {
        private Integer id;

        @JsonProperty("tipo_documento")
        private String tipoDocumento;

        private String serie;

        private String correlativo;

        @JsonProperty("numero_completo")
        private String numeroCompleto;

        @JsonProperty("sunat_status")
        private String sunatStatus;

        @JsonProperty("sunat_code")
        private String sunatCode;

        @JsonProperty("sunat_description")
        private String sunatDescription;
    }

    public boolean esExito() {
        return "exito".equalsIgnoreCase(estado);
    }
}
