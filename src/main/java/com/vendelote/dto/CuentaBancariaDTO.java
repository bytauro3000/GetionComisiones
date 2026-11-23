package com.vendelote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Cuenta bancaria de la empresa: banco + moneda + número de cuenta + CCI. */
@Data
public class CuentaBancariaDTO {

    @NotBlank(message = "El banco es obligatorio")
    @Size(max = 60)
    private String banco;

    /** Moneda de la cuenta: SOLES | DOLARES. */
    @NotBlank(message = "El tipo de cuenta es obligatorio (soles o dólares)")
    @Pattern(regexp = "SOLES|DOLARES", message = "El tipo de cuenta debe ser SOLES o DOLARES")
    private String tipoCuenta;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(max = 40)
    private String numeroCuenta;

    /** Clave de Cuenta Interbancaria: 20 dígitos (opcional). */
    @Size(max = 40)
    private String cci;
}
