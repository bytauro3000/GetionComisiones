package com.vendelote.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.ComisionRepartoDTO;
import com.vendelote.dto.ComisionRepartoSocioDTO;
import com.vendelote.entity.Comision;
import com.vendelote.entity.ComisionReparto;
import com.vendelote.entity.Usuario;
import com.vendelote.enums.RolUsuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ComisionRepartoRepository;
import com.vendelote.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Guarda y valida el reparto de una comisión con sus socios (máx. 2).
 * Los porcentajes se guardan en escala 0..1 (0.5 = 50%).
 *
 * En cada componente (inicial / letras), si el monto fijo viene informado
 * prevalece sobre el porcentaje; si no, se usa el porcentaje.
 */
@Service
@RequiredArgsConstructor
public class ComisionRepartoService {

    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");

    private final UsuarioRepository usuarioRepository;
    private final ComisionRepartoRepository repartoRepository;

    /** Reemplaza el reparto de la comisión validando montos y sumas. */
    @Transactional
    public void reemplazar(Comision comision, List<ComisionRepartoSocioDTO> socios,
                           BigDecimal inicialTotal, BigDecimal cuotaBase) {
        Integer idV = comision.getIdVendedor();
        repartoRepository.deleteByIdComisionAndIdVendedor(comision.getId(), idV);

        if (socios == null || socios.isEmpty()) {
            return;
        }
        if (socios.size() > 2) {
            throw new NegocioException("Máximo 2 socios por comisión (vendedor + 2 = 3 partes)");
        }

        Set<Integer> vistos = new HashSet<>();
        BigDecimal sumaInicial = BigDecimal.ZERO;
        BigDecimal sumaCuota = BigDecimal.ZERO;
        List<ComisionReparto> filas = new ArrayList<>();

        for (ComisionRepartoSocioDTO dto : socios) {
            if (dto.getIdSocio() == null) {
                throw new NegocioException("Debe indicar el socio");
            }
            if (!vistos.add(dto.getIdSocio())) {
                throw new NegocioException("El socio está repetido en el reparto");
            }
            Usuario socio = usuarioRepository.findByIdAndIdVendedorJefe(dto.getIdSocio(), idV)
                    .filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                    .orElseThrow(() -> new NegocioException("Vendedor socio no encontrado"));

            ComisionReparto fila = new ComisionReparto();
            fila.setIdVendedor(idV);
            fila.setIdComision(comision.getId());
            fila.setIdSocio(socio.getId());

            // ── Componente INICIAL ──────────────────────────────────────────
            fila.setParticipaInicial(dto.isParticipaInicial());
            if (dto.isParticipaInicial()) {
                if (dto.getMontoInicial() != null) {
                    BigDecimal monto = validarMonto(dto.getMontoInicial(), "inicial");
                    fila.setMontoInicial(monto);
                    fila.setPorcentajeInicial(porcentajeDe(monto, inicialTotal));
                    sumaInicial = sumaInicial.add(monto);
                } else {
                    BigDecimal pct = validarPorcentaje(dto.getPorcentajeInicial(), "inicial");
                    if (pct != null) {
                        fila.setPorcentajeInicial(pct);
                        sumaInicial = sumaInicial.add(share(pct, inicialTotal));
                    }
                }
            }

            // ── Componente LETRAS (por cada cuota) ──────────────────────────
            fila.setParticipaCuotas(dto.isParticipaCuotas());
            if (dto.isParticipaCuotas()) {
                if (dto.getMontoCuotaSocio() != null) {
                    BigDecimal monto = validarMonto(dto.getMontoCuotaSocio(), "letra");
                    fila.setMontoCuotaSocio(monto);
                    fila.setPorcentajeCuotas(porcentajeDe(monto, cuotaBase));
                    sumaCuota = sumaCuota.add(monto);
                } else {
                    BigDecimal pct = validarPorcentaje(dto.getPorcentajeCuotas(), "letra");
                    if (pct != null) {
                        fila.setPorcentajeCuotas(pct);
                        sumaCuota = sumaCuota.add(share(pct, cuotaBase));
                    }
                }
            }

            filas.add(fila);
        }

        if (inicialTotal != null && sumaInicial.compareTo(inicialTotal.add(TOLERANCIA)) > 0) {
            throw new NegocioException("El reparto del pago inicial supera el total ("
                    + inicialTotal.setScale(2, RoundingMode.HALF_UP) + " USD)");
        }
        if (cuotaBase != null && cuotaBase.signum() > 0
                && sumaCuota.compareTo(cuotaBase.add(TOLERANCIA)) > 0) {
            throw new NegocioException("El reparto por letra supera el monto de la cuota ("
                    + cuotaBase.setScale(2, RoundingMode.HALF_UP) + " USD)");
        }

        repartoRepository.saveAll(filas);
    }

    @Transactional(readOnly = true)
    public List<ComisionRepartoDTO> listar(Integer idComision, Integer idVendedor) {
        List<ComisionReparto> filas = repartoRepository
                .findByIdComisionAndIdVendedor(idComision, idVendedor);
        if (filas.isEmpty()) {
            return List.of();
        }
        Map<Integer, Usuario> socios = usuarioRepository
                .findAllById(filas.stream().map(ComisionReparto::getIdSocio).toList())
                .stream().collect(Collectors.toMap(Usuario::getId, Function.identity(), (a, b) -> a));

        return filas.stream().map(r -> {
            Usuario s = socios.get(r.getIdSocio());
            String nombre = s == null ? "Socio" :
                    (s.getNombres() + (s.getApellidos() != null ? " " + s.getApellidos() : "")).trim();
            return new ComisionRepartoDTO(
                    r.getId(), r.getIdSocio(), nombre,
                    r.isParticipaInicial(), r.getPorcentajeInicial(), r.getMontoInicial(),
                    r.isParticipaCuotas(), r.getPorcentajeCuotas(), r.getMontoCuotaSocio());
        }).toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private BigDecimal validarPorcentaje(BigDecimal pct, String etiqueta) {
        if (pct == null) {
            return null;
        }
        if (pct.signum() < 0 || pct.compareTo(BigDecimal.ONE) > 0) {
            throw new NegocioException("El porcentaje del reparto " + etiqueta
                    + " debe estar entre 0% y 100%");
        }
        return pct.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal validarMonto(BigDecimal monto, String etiqueta) {
        if (monto.signum() < 0) {
            throw new NegocioException("El monto del reparto " + etiqueta + " no puede ser negativo");
        }
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal share(BigDecimal pct, BigDecimal base) {
        if (pct == null || base == null) {
            return BigDecimal.ZERO;
        }
        return pct.multiply(base).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal porcentajeDe(BigDecimal monto, BigDecimal base) {
        if (base == null || base.signum() <= 0) {
            return null;
        }
        return monto.divide(base, 4, RoundingMode.HALF_UP);
    }
}
