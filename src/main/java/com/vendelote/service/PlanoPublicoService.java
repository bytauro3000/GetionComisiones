package com.vendelote.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.LotePublicoDTO;
import com.vendelote.dto.PlanoPublicoDTO;
import com.vendelote.entity.Lote;
import com.vendelote.entity.Proyecto;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lectura pública del plano. Solo expone manzana, número y estado de los lotes
 * de los programas VF/PV del vendedor dueño del token. No expone precios,
 * clientes, ventas ni identificadores internos.
 */
@Service
@RequiredArgsConstructor
public class PlanoPublicoService {

    private final ProyectoRepository proyectoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public Optional<PlanoPublicoDTO> obtenerPorToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return proyectoRepository.findByPlanoPublicToken(token.trim())
                .map(this::construir);
    }

    private PlanoPublicoDTO construir(Proyecto proyecto) {
        String programa = programaDesde(proyecto.getNombre());
        if (programa == null) {
            return null;
        }

        List<LotePublicoDTO> lotes = loteRepository
                .findByIdVendedorOrderByFechaRegistroDesc(proyecto.getIdVendedor())
                .stream()
                .map(this::aPublico)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PlanoPublicoDTO(proyecto.getNombre(), programa, lotes);
    }

    private LotePublicoDTO aPublico(Lote lote) {
        String programa = programaDesde(lote.getProyecto());
        if (programa == null || lote.getEstado() == null) {
            return null;
        }
        return new LotePublicoDTO(
                programa,
                lote.getManzana(),
                lote.getNumero(),
                lote.getEstado().name()
        );
    }

    private String programaDesde(String nombre) {
        if (nombre == null) {
            return null;
        }
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\bRESIDENCIAL\\b", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (n.contains("VILLA FAMILIA")) {
            return "VF";
        }
        if (n.contains("PRUDENCIA")) {
            return "PV";
        }
        return null;
    }
}
