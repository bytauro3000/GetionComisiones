package com.vendelote.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendelote.exception.NegocioException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Subida/eliminación/descarga de archivos en Cloudinary (API REST firmada,
 * sin SDK Java). Si faltan credenciales, {@link #disponible()} es false y el
 * sistema sigue guardando los planos en disco local.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final String API_BASE = "https://api.cloudinary.com/v1_1/";

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;
    @Value("${cloudinary.api-key:}")
    private String apiKey;
    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    /** true solo si las 3 credenciales de entorno están presentes. */
    public boolean disponible() {
        return noVacio(cloudName) && noVacio(apiKey) && noVacio(apiSecret);
    }

    /**
     * Sube un archivo (imagen o PDF) y devuelve su secure_url.
     * Usa el upload API firmado: signature = sha1(params ordenados + secret).
     *
     * @param carpeta   carpeta lógica en Cloudinary, p.ej. "vendelote/planos"
     * @param publicId  nombre base del archivo (uuid.ext) sin carpeta
     */
    public String subir(byte[] datos, String contentType, String carpeta, String publicId) {
        if (!disponible()) {
            throw new NegocioException("Cloudinary no está configurado (faltan variables de entorno)");
        }
        long timestamp = System.currentTimeMillis() / 1000;
        // Parámetros signables en orden alfabético
        String paraFirmar = "folder=" + carpeta + "&public_id=" + publicId + "&timestamp=" + timestamp;
        String firma = sha1(paraFirmar + apiSecret);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(datos));
        form.add("folder", carpeta);
        form.add("public_id", publicId);
        form.add("timestamp", String.valueOf(timestamp));
        form.add("api_key", apiKey);
        form.add("signature", firma);

        try {
            String json = restClient.post()
                    .uri(API_BASE + "{cloud}/auto/upload", cloudName)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            JsonNode node = objectMapper.readTree(json);
            String url = node.path("secure_url").asText(null);
            if (url == null || url.isBlank()) {
                throw new NegocioException("Cloudinary no devolvió la URL del archivo");
            }
            return url;
        } catch (NegocioException e) {
            throw e;
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error subiendo a Cloudinary ({}): {}", publicId, e.getMessage());
            throw new NegocioException("No se pudo subir el archivo a Cloudinary");
        }
    }

    /** Elimina un archivo por su secure_url (extrae el public_id del path). */
    public void eliminarPorUrl(String url) {
        if (!disponible() || url == null || !url.startsWith("http")) {
            return;
        }
        String publicId = publicIdDesdeUrl(url);
        if (publicId == null) {
            return;
        }
        long timestamp = System.currentTimeMillis() / 1000;
        String paraFirmar = "public_id=" + publicId + "&timestamp=" + timestamp;
        String firma = sha1(paraFirmar + apiSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("public_id", publicId);
        form.add("timestamp", String.valueOf(timestamp));
        form.add("api_key", apiKey);
        form.add("signature", firma);

        try {
            restClient.post()
                    .uri(API_BASE + "{cloud}/destroy", cloudName)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("No se pudo eliminar {} de Cloudinary: {}", publicId, e.getMessage());
        }
    }

    /**
     * Descarga un archivo desde su URL pública (el backend hace de proxy,
     * igual que con los comprobantes de SUNAT). No requiere credenciales.
     */
    public byte[] descargar(String url) {
        try {
            byte[] cuerpo = restClient.get().uri(url).retrieve().body(byte[].class);
            if (cuerpo == null || cuerpo.length == 0) {
                throw new NegocioException("El archivo en Cloudinary está vacío");
            }
            return cuerpo;
        } catch (RestClientException e) {
            log.error("Error descargando de Cloudinary: {}", e.getMessage());
            throw new NegocioException("No se pudo descargar el archivo desde Cloudinary");
        }
    }

    /** "…/upload/v123/vendelote/planos/abc.pdf" → "vendelote/planos/abc" */
    public static String publicIdDesdeUrl(String url) {
        int i = url.indexOf("/upload/");
        if (i < 0) {
            return null;
        }
        String despues = url.substring(i + "/upload/".length());
        // quitar versión vNNNN/
        if (despues.matches("^v\\d+/.*")) {
            despues = despues.substring(despues.indexOf('/') + 1);
        }
        // quitar query y extensión final
        int q = despues.indexOf('?');
        if (q >= 0) {
            despues = despues.substring(0, q);
        }
        int punto = despues.lastIndexOf('.');
        if (punto > despues.lastIndexOf('/')) {
            despues = despues.substring(0, punto);
        }
        return despues.isBlank() ? null : despues;
    }

    private String sha1(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 no disponible", e);
        }
    }

    private boolean noVacio(String v) {
        return v != null && !v.isBlank();
    }
}
