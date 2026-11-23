package com.vendelote.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpresaDTO {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150)
    private String razonSocial;

    @Size(max = 120)
    private String nombreComercial;

    @Size(max = 200)
    private String direccion;

    @Size(max = 60)
    private String distrito;

    @Size(max = 60)
    private String provincia;

    @Size(max = 60)
    private String departamento;

    @Size(max = 100)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    private String correo;

    /** Cuentas bancarias (banco + moneda + número de cuenta + CCI). */
    @Valid
    @Size(max = 10)
    private List<CuentaBancariaDTO> cuentas;
}
