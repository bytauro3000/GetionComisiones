package com.vendelote.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.vendelote.dto.DniConsultaDTO;
import com.vendelote.dto.RucConsultaDTO;

/**
 * Consulta de datos abiertos vía Declecta (RENIEC: DNI, SUNAT: RUC).
 * Token único de la plataforma (lo paga el SaaS; cuota ~100/mes) por lo
 * que TODAS las respuestas se cachean en memoria por 1 hora.
 *
 * Fail-safe: cualquier fallo (sin token, sin cuota, caída) devuelve vacío
 * y el flujo del usuario continúa con los datos que haya escrito a mano.
 */
@Service
public class DeclectaService {

    private static final Logger log = LoggerFactory.getLogger(DeclectaService.class);
    private static final Duration TTL = Duration.ofHours(1);

    private final boolean configurado;
    private final RestClient restClient;

    private final Map<String, CacheEntrada<DniConsultaDTO>> cacheDni = new ConcurrentHashMap<>();
    private final Map<String, CacheEntrada<RucConsultaDTO>> cacheRuc = new ConcurrentHashMap<>();

    public DeclectaService(
            @Value("${decolecta.api.url:https://api.decolecta.com}") String urlBase,
            @Value("${decolecta.api.token:}") String token) {
        this.configurado = token != null && !token.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(configurarTimeouts())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    /** DNI (8 dígitos) → datos de RENIEC, o vacío si no existe/no está disponible. */
    public Optional<DniConsultaDTO> buscarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            return Optional.empty();
        }
        return consultar("/v1/reniec/dni?numero=" + dni, dni, cacheDni, DniConsultaDTO.class)
                .filter(d -> d.getNumeroDocumento() != null && !d.getNumeroDocumento().isBlank());
    }

    /** RUC (11 dígitos) → datos de SUNAT, o vacío si no existe/no está disponible. */
    public Optional<RucConsultaDTO> buscarRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            return Optional.empty();
        }
        return consultar("/v1/sunat/ruc?numero=" + ruc, ruc, cacheRuc, RucConsultaDTO.class)
                .filter(r -> r.getRazonSocial() != null && !r.getRazonSocial().isBlank());
    }

    public boolean estaConfigurado() {
        return configurado;
    }

    // ── Internos ───────────────────────────────────────────────────────────────

    private <T> Optional<T> consultar(String uri, String clave,
                                      Map<String, CacheEntrada<T>> cache, Class<T> tipo) {
        if (!configurado) {
            return Optional.empty();
        }
        CacheEntrada<T> hit = cache.get(clave);
        if (hit != null && !hit.expiro()) {
            return Optional.of(hit.valor());
        }
        try {
            T valor = restClient.get().uri(uri).retrieve().body(tipo);
            if (valor == null) {
                return Optional.empty();
            }
            cache.put(clave, new CacheEntrada<>(valor, Instant.now().plus(TTL)));
            return Optional.of(valor);
        } catch (RestClientResponseException e) {
            log.debug("Declecta respondió {} para {}", e.getStatusCode(), uri);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Declecta no disponible ({}): {}", uri, e.getMessage());
            return Optional.empty();
        }
    }

    private static ClientHttpRequestFactory configurarTimeouts() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(8));
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }

    private record CacheEntrada<T>(T valor, Instant expira) {
        boolean expiro() {
            return Instant.now().isAfter(expira);
        }
    }
}
