package com.vendelote.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.IntegracionSunatDTO;
import com.vendelote.entity.IntegracionSunat;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.IntegracionSunatRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Configuración de facturación por tenant (credenciales api-sunat).
 * Las claves solo se LEEN internamente para emitir; el DTO de salida nunca
 * las incluye (solo indicador "configurado").
 */
@Service
@RequiredArgsConstructor
public class IntegracionSunatService {

    private final IntegracionSunatRepository integracionRepository;
    private final SunatApiClient sunatApiClient;

    /** Config actual (si no existe, devuelve defaults sin persistir). */
    @Transactional(readOnly = true)
    public IntegracionSunatDTO obtener() {
        return toDTO(buscarOEntidadPorDefecto());
    }

    @Transactional
    public IntegracionSunatDTO guardar(IntegracionSunatDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        IntegracionSunat entidad = integracionRepository.findByIdVendedor(idV)
                .orElseGet(() -> {
                    IntegracionSunat nueva = new IntegracionSunat();
                    nueva.setIdVendedor(idV);
                    return nueva;
                });

        if (dto.getApiUrl() != null && !dto.getApiUrl().isBlank()) {
            entidad.setApiUrl(dto.getApiUrl().trim());
        }
        if (dto.getApiKey() != null && !dto.getApiKey().isBlank()) {
            entidad.setApiKey(dto.getApiKey().trim());
        }
        if (dto.getApiSecret() != null && !dto.getApiSecret().isBlank()) {
            entidad.setApiSecret(dto.getApiSecret().trim());
        }
        if (dto.getSerieFactura() != null && !dto.getSerieFactura().isBlank()) {
            String serie = dto.getSerieFactura().trim().toUpperCase();
            if (!serie.matches("F\\d{3}|\\d{4}")) {
                throw new NegocioException("La serie debe empezar con F (ej: F001) o ser 4 dígitos");
            }
            entidad.setSerieFactura(serie);
        }
        entidad.setAplicarDetraccion(dto.isAplicarDetraccion());
        if (dto.getCodBienDetraccion() != null && !dto.getCodBienDetraccion().isBlank()) {
            entidad.setCodBienDetraccion(dto.getCodBienDetraccion().trim());
        }
        if (dto.getPorcentajeDetraccion() != null) {
            entidad.setPorcentajeDetraccion(dto.getPorcentajeDetraccion());
        }
        if (dto.getUmbralDetraccion() != null) {
            entidad.setUmbralDetraccion(dto.getUmbralDetraccion());
        }
        entidad.setCtaBanco(dto.getCtaBanco() != null && !dto.getCtaBanco().isBlank()
                ? dto.getCtaBanco().trim() : null);
        if (dto.getCodMedioPago() != null && !dto.getCodMedioPago().isBlank()) {
            entidad.setCodMedioPago(dto.getCodMedioPago().trim());
        }

        return toDTO(integracionRepository.save(entidad));
    }

    /** Prueba de conexión real contra api-sunat (GET /sucursales). */
    @Transactional(readOnly = true)
    public boolean probar() {
        IntegracionSunat config = integracionRepository
                .findByIdVendedor(TenantContext.idVendedorRequerido()).orElse(null);
        if (config == null || vacio(config.getApiKey()) || vacio(config.getApiSecret())) {
            throw new NegocioException("Configure api_key y api_secret de api-sunat antes de probar");
        }
        return sunatApiClient.probarConexion(
                sunatApiClient.urlBase(config.getApiUrl()), config.getApiKey(), config.getApiSecret());
    }

    /**
     * Configuración valida para EMITIR; lanza mensajes accionables si falta
     * algo. Llamar siempre antes de armar el payload.
     */
    @Transactional(readOnly = true)
    public IntegracionSunat requerirParaEmitir() {
        IntegracionSunat config = integracionRepository
                .findByIdVendedor(TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException(
                        "Configure primero la facturación electrónica (api-sunat): "
                                + "sección Integración SUNAT"));
        if (vacio(config.getApiKey()) || vacio(config.getApiSecret())) {
            throw new NegocioException("Falta api_key/api_secret de api-sunat (sección Integración SUNAT)");
        }
        if (vacio(config.getSerieFactura())) {
            throw new NegocioException("Falta la serie de facturas (ej: F001) en Integración SUNAT");
        }
        if (config.isAplicarDetraccion() && vacio(config.getCtaBanco())) {
            throw new NegocioException(
                    "Falta la cuenta bancaria para la detracción (o desactive la detracción) en Integración SUNAT");
        }
        return config;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private IntegracionSunat buscarOEntidadPorDefecto() {
        return integracionRepository
                .findByIdVendedor(TenantContext.idVendedorRequerido())
                .orElseGet(IntegracionSunat::new);
    }

    private IntegracionSunatDTO toDTO(IntegracionSunat e) {
        IntegracionSunatDTO dto = new IntegracionSunatDTO();
        dto.setApiUrl(e.getApiUrl());
        // Las claves NUNCA salen hacia el frontend
        dto.setApiKeyConfigurado(!vacio(e.getApiKey()));
        dto.setApiSecretConfigurado(!vacio(e.getApiSecret()));
        dto.setSerieFactura(e.getSerieFactura() != null ? e.getSerieFactura() : "F001");
        dto.setAplicarDetraccion(e.isAplicarDetraccion());
        dto.setCodBienDetraccion(e.getCodBienDetraccion() != null ? e.getCodBienDetraccion() : "037");
        dto.setPorcentajeDetraccion(e.getPorcentajeDetraccion() != null
                ? e.getPorcentajeDetraccion() : new BigDecimal("10.00"));
        dto.setUmbralDetraccion(e.getUmbralDetraccion() != null
                ? e.getUmbralDetraccion() : new BigDecimal("190.00"));
        dto.setCtaBanco(e.getCtaBanco());
        dto.setCodMedioPago(e.getCodMedioPago() != null ? e.getCodMedioPago() : "003");
        return dto;
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
