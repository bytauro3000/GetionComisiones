-- Amplía las columnas de credenciales de api-sunat para alojar el texto
-- cifrado en base64 (AES/GCM) que ahora persiste EncryptedStringConverter.
-- En una BD NUEVA el baseline ya las crea en 512; este ALTER es inocuo.
-- En una BD EXISTENTE (adoptada por baseline-on-migrate) corrige 100/120 -> 512.
ALTER TABLE integracion_sunat
    MODIFY COLUMN api_key VARCHAR(512) NULL,
    MODIFY COLUMN api_secret VARCHAR(512) NULL;
