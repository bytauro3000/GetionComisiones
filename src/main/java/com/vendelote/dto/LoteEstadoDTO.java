package com.vendelote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cambio rápido de estado del lote desde la tabla (switch "vendido").
 * No confunde con la venta: solo mueve el estado del inventario.
 */
@Data
public class LoteEstadoDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "DISPONIBLE|RESERVADO|VENDIDO", message = "Estado no válido")
    private String estado;

    /** Motivo del cambio (visible al marcar VENDIDO manualmente). */
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String motivo;
}
