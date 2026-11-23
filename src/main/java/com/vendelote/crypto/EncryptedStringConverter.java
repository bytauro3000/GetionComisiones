package com.vendelote.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte un String a su forma cifrada (AES/GCM) al persistir y lo descifra
 * al leer. Se aplica explícitamente con @Convert en cada campo sensible.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return SecretCrypto.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return SecretCrypto.decrypt(dbData);
    }
}
