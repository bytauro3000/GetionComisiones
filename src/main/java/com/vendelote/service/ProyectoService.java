package com.vendelote.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vendelote.dto.ProyectoDTO;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Proyecto;
import com.vendelote.enums.EstadoProyecto;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.ProyectoRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/** Proyectos del tenant: CRUD, plano subible y sincronización del nombre en lotes. */
@Service
@RequiredArgsConstructor
public class ProyectoService {

    private static final Set<String> EXTENSIONES = Set.of("jpg", "jpeg", "png", "webp", "pdf");
    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB

    private final ProyectoRepository proyectoRepository;
    private final LoteRepository loteRepository;
    private final EmpresaService empresaService;
    private final CloudinaryService cloudinaryService;

    @Value("${app.uploads.dir:uploads/planos}")
    private String uploadsDir;

    @Transactional(readOnly = true)
    public List<Proyecto> listar() {
        return proyectoRepository.findByIdVendedorOrderByFechaRegistroDesc(TenantContext.idVendedorRequerido());
    }

    @Transactional(readOnly = true)
    public Proyecto buscar(Integer id) {
        return proyectoRepository.findByIdAndIdVendedor(id, TenantContext.idVendedorRequerido())
                .orElseThrow(() -> new NegocioException("Proyecto no encontrado"));
    }

    /** Para lote: valida que el proyecto exista en el tenant y pertenezca a la empresa dada. */
    @Transactional(readOnly = true)
    public Proyecto buscarDeEmpresa(Integer idProyecto, Integer idEmpresa) {
        Proyecto proyecto = buscar(idProyecto);
        if (!proyecto.getIdEmpresa().equals(idEmpresa)) {
            throw new NegocioException("El proyecto no pertenece a esa empresa");
        }
        return proyecto;
    }

    @Transactional
    public Proyecto crear(ProyectoDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Empresa empresa = empresaService.buscar(dto.getIdEmpresa());

        String nombre = dto.getNombre().trim();
        if (proyectoRepository.existsByIdVendedorAndIdEmpresaAndNombre(idV, empresa.getId(), nombre)) {
            throw new NegocioException("Ya existe un proyecto con ese nombre en esa empresa");
        }

        Proyecto proyecto = new Proyecto();
        proyecto.setIdVendedor(idV);
        proyecto.setNombre(nombre);
        proyecto.setEstado(estadoDesde(dto.getEstado(), EstadoProyecto.ACTIVO));
        aplicarUbicacion(proyecto, dto);
        proyecto.setIdEmpresa(empresa.getId());
        return proyectoRepository.save(proyecto);
    }

    @Transactional
    public Proyecto editar(Integer id, ProyectoDTO dto) {
        Integer idV = TenantContext.idVendedorRequerido();
        Proyecto proyecto = buscar(id);
        Empresa empresa = empresaService.buscar(dto.getIdEmpresa());

        String nombre = dto.getNombre().trim();
        if (proyectoRepository.existsByIdVendedorAndIdEmpresaAndNombreAndIdNot(
                idV, empresa.getId(), nombre, proyecto.getId())) {
            throw new NegocioException("Ya existe un proyecto con ese nombre en esa empresa");
        }

        boolean cambioNombre = !proyecto.getNombre().equals(nombre);
        proyecto.setNombre(nombre);
        proyecto.setEstado(estadoDesde(dto.getEstado(), proyecto.getEstado()));
        aplicarUbicacion(proyecto, dto);
        proyecto.setIdEmpresa(empresa.getId());

        Proyecto guardado = proyectoRepository.save(proyecto);

        // El texto del lote es una copia sincronizada (facturación lo usa).
        if (cambioNombre) {
            loteRepository.actualizarNombreProyecto(guardado.getId(), guardado.getNombre());
        }
        return guardado;
    }

    @Transactional
    public void eliminar(Integer id) {
        Proyecto proyecto = buscar(id);
        if (loteRepository.existsByIdProyecto(proyecto.getId())) {
            throw new NegocioException("No se puede eliminar: el proyecto tiene lotes asociados");
        }
        eliminarRecursoAnterior(proyecto.getPlanoRuta());
        proyectoRepository.delete(proyecto);
    }

    // ── Plano ─────────────────────────────────────────────────────────────────

    @Transactional
    public void guardarPlano(Integer id, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("Debe seleccionar un archivo de plano");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new NegocioException("El plano no puede superar 10 MB");
        }
        String ext = extensionDesdeNombre(archivo.getOriginalFilename());
        if (!EXTENSIONES.contains(ext)) {
            throw new NegocioException("Formato no válido: use JPG, PNG, WebP o PDF");
        }

        Proyecto proyecto = buscar(id);
        eliminarRecursoAnterior(proyecto.getPlanoRuta()); // reemplaza el anterior

        if (cloudinaryService.disponible()) {
            // Nube: sube a Cloudinary y guarda solo la secure_url
            String nombreBase = UUID.randomUUID() + "." + ext;
            try {
                String url = cloudinaryService.subir(
                        archivo.getBytes(), contentTypeDesde(nombreBase),
                        "vendelote/planos", nombreBase.replace('.', '_'));
                proyecto.setPlanoRuta(url);
            } catch (IOException e) {
                throw new NegocioException("No se pudo leer el archivo del plano");
            }
        } else {
            // Fallback local: disco del servidor
            String nombreGuardado = UUID.randomUUID() + "." + ext;
            Path destino = Paths.get(uploadsDir, nombreGuardado);
            try {
                Files.createDirectories(destino.getParent());
                archivo.transferTo(destino);
            } catch (IOException e) {
                throw new NegocioException("No se pudo guardar el plano");
            }
            proyecto.setPlanoRuta(nombreGuardado);
        }
        proyectoRepository.save(proyecto);
    }

    @Transactional
    public void borrarPlano(Integer id) {
        Proyecto proyecto = buscar(id);
        if (proyecto.getPlanoRuta() == null) {
            return;
        }
        eliminarRecursoAnterior(proyecto.getPlanoRuta());
        proyecto.setPlanoRuta(null);
        proyectoRepository.save(proyecto);
    }

    @Transactional(readOnly = true)
    public PlanoRecurso recursoPlano(Integer id) {
        Proyecto proyecto = buscar(id);
        String ruta = proyecto.getPlanoRuta();
        if (ruta == null) {
            throw new NegocioException("El proyecto no tiene plano");
        }

        if (ruta.startsWith("http")) {
            // Cloudinary: el backend hace de proxy (el frontend nunca ve la URL)
            byte[] bytes = cloudinaryService.descargar(ruta);
            return new PlanoRecurso(bytes, contentTypeDesde(ruta));
        }

        Path archivo = Paths.get(uploadsDir, ruta);
        if (!Files.exists(archivo)) {
            throw new NegocioException("El archivo del plano no está disponible");
        }
        try {
            return new PlanoRecurso(Files.readAllBytes(archivo), contentTypeDesde(ruta));
        } catch (IOException e) {
            throw new NegocioException("No se pudo leer el plano");
        }
    }

    /**
     * Migra los planos locales del tenant a Cloudinary (sube, guarda la URL
     * y borra el archivo del disco). Responde con la cantidad migrada.
     */
    @Transactional
    public int migrarPlanosACloudinary() {
        if (!cloudinaryService.disponible()) {
            throw new NegocioException(
                    "Cloudinary no está configurado (faltan CLOUDINARY_CLOUD_NAME / API_KEY / API_SECRET)");
        }
        int migrados = 0;
        for (Proyecto proyecto : proyectoRepository.findByIdVendedorOrderByFechaRegistroDesc(
                TenantContext.idVendedorRequerido())) {
            String ruta = proyecto.getPlanoRuta();
            if (ruta == null || ruta.isBlank() || ruta.startsWith("http")) {
                continue;
            }
            Path archivo = Paths.get(uploadsDir, ruta);
            if (!Files.exists(archivo)) {
                continue;
            }
            try {
                byte[] bytes = Files.readAllBytes(archivo);
                String ext = extensionDesdeNombre(ruta);
                String nombreBase = UUID.randomUUID() + "." + ext;
                String url = cloudinaryService.subir(bytes, contentTypeDesde(ruta),
                        "vendelote/planos", nombreBase.replace('.', '_'));
                proyecto.setPlanoRuta(url);
                proyectoRepository.save(proyecto);
                Files.deleteIfExists(archivo);
                migrados++;
            } catch (IOException e) {
                // un archivo problemático no detiene el resto de la migración
            }
        }
        return migrados;
    }

    /** bytes + content-type del plano para servirlo al frontend (con auth tenant). */
    public record PlanoRecurso(byte[] bytes, String contentType) {
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void aplicarUbicacion(Proyecto proyecto, ProyectoDTO dto) {
        proyecto.setUbicacion(dto.getUbicacion() != null && !dto.getUbicacion().isBlank()
                ? dto.getUbicacion().trim() : null);
        proyecto.setLatitud(validarCoordenada(dto.getLatitud(), -90, 90, "latitud"));
        proyecto.setLongitud(validarCoordenada(dto.getLongitud(), -180, 180, "longitud"));
    }

    private BigDecimal validarCoordenada(BigDecimal valor, double min, double max, String campo) {
        if (valor == null) {
            return null;
        }
        if (valor.doubleValue() < min || valor.doubleValue() > max) {
            throw new NegocioException("Coordenada de " + campo + " fuera de rango");
        }
        return valor;
    }

    private EstadoProyecto estadoDesde(String valor, EstadoProyecto porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return EstadoProyecto.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Estado no válido: use ACTIVO o INACTIVO");
        }
    }

    private String extensionDesdeNombre(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "";
        }
        return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String contentTypeDesde(String nombreGuardado) {
        String ext = extensionDesdeNombre(nombreGuardado);
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private void borrarArchivoSiExiste(String nombreGuardado) {
        if (nombreGuardado == null || nombreGuardado.isBlank() || nombreGuardado.startsWith("http")) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(uploadsDir, nombreGuardado));
        } catch (IOException ignored) {
            // el archivo viejo no bloquea la operación
        }
    }

    /** Borra el plano anterior esté en disco o en Cloudinary. */
    private void eliminarRecursoAnterior(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return;
        }
        if (ruta.startsWith("http")) {
            cloudinaryService.eliminarPorUrl(ruta);
        } else {
            borrarArchivoSiExiste(ruta);
        }
    }
}
