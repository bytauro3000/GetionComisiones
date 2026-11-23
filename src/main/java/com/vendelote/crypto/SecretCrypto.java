package com.vendelote.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Cifrado simétrico AES/GCM para secretos en reposo (p. ej. credenciales de
 * api-sunat). La clave se toma de la variable de entorno SECRETS_ENCRYPTION_KEY.
 *
 * - En producción es OBLIGATORIA (la app no arranca sin ella).
 * - En desarrollo, si falta, se usa una clave fija "solo-dev" (base de datos local).
 *
 * Los valores cifrados llevan el prefijo "enc:v1:". Cualquier valor sin ese
 * prefijo se considera texto plano legado y se devuelve tal cual (migración
 * transparente: se cifra en el siguiente guardado).
 */
@Slf4j
@Component
public class SecretCrypto {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final String DEV_FALLBACK = "vendelote-dev-only-insecure-key-change-me";

    private static volatile SecretKeySpec key;

    public SecretCrypto(@Value("${secrets.encryption-key:}") String raw, Environment environment) {
        if (raw == null || raw.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
                throw new IllegalStateException(
                        "SECRETS_ENCRYPTION_KEY no está definida. Configure la variable de entorno "
                                + "(base64 de 32 bytes, p. ej.: openssl rand -base64 32).");
            }
            raw = DEV_FALLBACK;
            log.warn("SECRETS_ENCRYPTION_KEY no definida: se usa la clave de desarrollo. "
                    + "NO usar así en producción.");
        }
        key = new SecretKeySpec(derivarClave(raw), "AES");
    }

    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] salida = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, salida, 0, iv.length);
            System.arraycopy(cipherText, 0, salida, iv.length, cipherText.length);
            return PREFIX + Base64.getEncoder().encodeToString(salida);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar el secreto", e);
        }
    }

    public static String decrypt(String stored) {
        if (stored == null || stored.isEmpty()) {
            return stored;
        }
        // Texto plano legado (filas previas al cifrado): se devuelve sin tocar.
        if (!stored.startsWith(PREFIX)) {
            return stored;
        }
        try {
            byte[] all = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(all, 0, IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(all, IV_LENGTH, all.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo descifrar el secreto (¿cambió SECRETS_ENCRYPTION_KEY?)", e);
        }
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /** Acepta base64 o passphrase; siempre deriva 32 bytes (AES-256). */
    private static byte[] derivarClave(String raw) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(raw);
        } catch (IllegalArgumentException e) {
            bytes = raw.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length == 16 || bytes.length == 24 || bytes.length == 32) {
            return bytes;
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar la clave de cifrado", e);
        }
    }
}
