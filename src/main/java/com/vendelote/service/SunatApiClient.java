package com.vendelote.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.vendelote.dto.FacturaApiRespuestaDTO;
import com.vendelote.exception.NegocioException;

import lombok.extern.slf4j.Slf4j;

/**
 * Cliente HTTP de api-sunat (facturación electrónica propia).
 * Autenticación X-Api-Key/X-Api-Secret POR CUENTA (por tenant): se envían
 * en cada petición, nunca se fijan como default del RestClient.
 */
@Service
@Slf4j
public class SunatApiClient {

    private final String urlBaseGlobal;
    private final RestClient restClient;

    public SunatApiClient(@Value("${sunat.api.url}") String urlBaseGlobal) {
        this.urlBaseGlobal = urlBaseGlobal;
        this.restClient = RestClient.builder().build();
    }

    /** POST /facturas — crea y encola la factura. Devuelve id/serie/correlativo SUNAT. */
    public FacturaApiRespuestaDTO crearFactura(String baseUrl, String apiKey, String apiSecret,
                                               Map<String, Object> payload) {
        return post(baseUrl + "/facturas", apiKey, apiSecret, payload);
    }

    /** GET /facturas/{id} — estado actual (sunat_status). */
    public FacturaApiRespuestaDTO obtenerFactura(String baseUrl, String apiKey, String apiSecret, Integer id) {
        return get(baseUrl + "/facturas/" + id, apiKey, apiSecret, FacturaApiRespuestaDTO.class);
    }

    /** GET /facturas/{id}/pdf — bytes del PDF (proxy; el frontend nunca ve las credenciales). */
    public byte[] descargarPdf(String baseUrl, String apiKey, String apiSecret, Integer id) {
        try {
            return restClient.get()
                    .uri(baseUrl + "/facturas/" + id + "/pdf")
                    .header("X-Api-Key", apiKey)
                    .header("X-Api-Secret", apiSecret)
                    .accept(MediaType.APPLICATION_PDF)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientResponseException e) {
            throw new NegocioException("No se pudo descargar el PDF (SUNAT aún no lo tiene disponible)");
        } catch (RestClientException e) {
            log.warn("api-sunat no disponible (pdf): {}", e.getMessage());
            throw new NegocioException("El servicio de facturación no está disponible");
        }
    }

    /** GET /sucursales — prueba de conexión/credenciales. */
    public boolean probarConexion(String baseUrl, String apiKey, String apiSecret) {
        try {
            restClient.get()
                    .uri(baseUrl + "/sucursales")
                    .header("X-Api-Key", apiKey)
                    .header("X-Api-Secret", apiSecret)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("Prueba de conexión api-sunat falló: {}", e.getMessage());
            return false;
        }
    }

    public String urlBase(String urlConfigurada) {
        String base = (urlConfigurada != null && !urlConfigurada.isBlank())
                ? urlConfigurada.trim() : urlBaseGlobal;
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    // ── Internos ───────────────────────────────────────────────────────────────

    private FacturaApiRespuestaDTO post(String uri, String apiKey, String apiSecret, Map<String, Object> payload) {
        try {
            FacturaApiRespuestaDTO respuesta = restClient.post()
                    .uri(uri)
                    .header("X-Api-Key", apiKey)
                    .header("X-Api-Secret", apiSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(FacturaApiRespuestaDTO.class);
            if (respuesta == null || !respuesta.esExito()) {
                throw new NegocioException(mensajeDe(respuesta,
                        "api-sunat rechazó la emisión"));
            }
            return respuesta;
        } catch (RestClientResponseException e) {
            throw new NegocioException("api-sunat rechazó la emisión: " + mensajeError(e));
        } catch (RestClientException e) {
            log.warn("api-sunat no disponible: {}", e.getMessage());
            throw new NegocioException(
                    "El servicio de facturación no está disponible (verifique la URL y que api-sunat esté en línea)");
        }
    }

    private <T> T get(String uri, String apiKey, String apiSecret, Class<T> tipo) {
        try {
            T respuesta = restClient.get()
                    .uri(uri)
                    .header("X-Api-Key", apiKey)
                    .header("X-Api-Secret", apiSecret)
                    .retrieve()
                    .body(tipo);
            if (respuesta == null) {
                throw new NegocioException("api-sunat no devolvió datos");
            }
            return respuesta;
        } catch (RestClientResponseException e) {
            throw new NegocioException("No se pudo consultar api-sunat: " + mensajeError(e));
        } catch (RestClientException e) {
            log.warn("api-sunat no disponible: {}", e.getMessage());
            throw new NegocioException("El servicio de facturación no está disponible");
        }
    }

    private String mensajeError(RestClientResponseException e) {
        try {
            String cuerpo = e.getResponseBodyAsString();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> json = mapper.readValue(cuerpo, Map.class);
            Object mensaje = json.get("mensaje");
            if (mensaje == null) {
                mensaje = json.get("message");
            }
            if (mensaje != null) {
                return mensaje.toString();
            }
            return cuerpo.length() > 200 ? cuerpo.substring(0, 200) : cuerpo;
        } catch (Exception ex) {
            return "HTTP " + e.getStatusCode().value();
        }
    }

    private String mensajeDe(FacturaApiRespuestaDTO respuesta, String porDefecto) {
        return respuesta != null && respuesta.getMensaje() != null
                ? respuesta.getMensaje() : porDefecto;
    }
}
