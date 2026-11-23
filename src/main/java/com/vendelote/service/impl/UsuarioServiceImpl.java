package com.vendelote.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.PerfilDTO;
import com.vendelote.dto.RegistroPublicoDTO;
import com.vendelote.dto.UsuarioListadoDTO;
import com.vendelote.dto.UsuarioRegistroDTO;
import com.vendelote.entity.Empresa;
import com.vendelote.entity.Usuario;
import com.vendelote.enums.EstadoUsuario;
import com.vendelote.enums.RolUsuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.ClienteRepository;
import com.vendelote.repository.ComisionRepository;
import com.vendelote.repository.ComprobantePagoRepository;
import com.vendelote.repository.ComprobanteRepository;
import com.vendelote.repository.DetalleComprobanteRepository;
import com.vendelote.repository.EmpresaRepository;
import com.vendelote.repository.IntegracionSunatRepository;
import com.vendelote.repository.LoteRepository;
import com.vendelote.repository.PagoComisionRepository;
import com.vendelote.repository.ParametroComisionRepository;
import com.vendelote.repository.ProformaRepository;
import com.vendelote.repository.SecuenciaProformaRepository;
import com.vendelote.repository.ProyectoRepository;
import com.vendelote.repository.RefreshTokenRepository;
import com.vendelote.repository.SecuenciaRepository;
import com.vendelote.repository.SesionActivaRepository;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.repository.VendedorEmpresaRepository;
import com.vendelote.repository.VentaRepository;
import com.vendelote.security.TenantContext;
import com.vendelote.service.DeclectaService;
import com.vendelote.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UserDetailsService, UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeclectaService declectaService;

    // ── Repositorios del árbol de datos que se borra en cascada ─────────────
    private final PagoComisionRepository pagoComisionRepository;
    private final ComisionRepository comisionRepository;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final DetalleComprobanteRepository detalleComprobanteRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final ProformaRepository proformaRepository;
    private final SecuenciaProformaRepository secuenciaProformaRepository;
    private final VentaRepository ventaRepository;
    private final LoteRepository loteRepository;
    private final ProyectoRepository proyectoRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final IntegracionSunatRepository integracionSunatRepository;
    private final ParametroComisionRepository parametroComisionRepository;
    private final VendedorEmpresaRepository vendedorEmpresaRepository;
    private final SecuenciaRepository secuenciaRepository;
    private final SesionActivaRepository sesionActivaRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        boolean estaActivo = usuario.getEstado() == EstadoUsuario.activo;

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(),
                estaActivo,
                true, true, true,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
    }

    @Override
    public Usuario buscarByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioListadoDTO> listarUsuarios(String estado) {
        Stream<Usuario> base = usuarioRepository.findAll().stream();

        if (estado != null && !estado.isBlank()) {
            EstadoUsuario filtro;
            try {
                filtro = EstadoUsuario.valueOf(estado.trim().toLowerCase());
            } catch (Exception e) {
                throw new NegocioException("Estado no válido: use activo, inactivo o pendiente");
            }
            final EstadoUsuario filtroFinal = filtro;
            base = base.filter(u -> u.getEstado() == filtroFinal);
        }

        return base.map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioListadoDTO autoRegistro(RegistroPublicoDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new NegocioException("El correo ya está registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombres(dto.getNombres().trim());
        nuevo.setApellidos(dto.getApellidos().trim());
        nuevo.setCorreo(correo);
        nuevo.setTelefono(dto.getTelefono());
        nuevo.setDni(dto.getDni());
        nuevo.setRol(RolUsuario.VENDEDOR);
        nuevo.setEstado(EstadoUsuario.pendiente);
        nuevo.setContrasena(passwordEncoder.encode(dto.getContrasena()));

        // Autocompletado desde RENIEC (Declecta): la identidad de la BD manda.
        // Si Declecta no está configurado o falla, se conservan los datos escritos.
        if (nuevo.getDni() != null && !nuevo.getDni().isBlank()) {
            try {
                declectaService.buscarDni(nuevo.getDni()).ifPresent(dni -> {
                    if (dni.getNombres() != null && !dni.getNombres().isBlank()) {
                        nuevo.setNombres(dni.getNombres());
                    }
                    String apellidosReniec = ((dni.getApellidoPaterno() == null ? "" : dni.getApellidoPaterno())
                            + " " + (dni.getApellidoMaterno() == null ? "" : dni.getApellidoMaterno())).trim();
                    if (!apellidosReniec.isBlank()) {
                        nuevo.setApellidos(apellidosReniec);
                    }
                });
            } catch (Exception e) {
                // Nunca bloquear el registro por Declecta
            }
        }

        return toDTO(usuarioRepository.save(nuevo));
    }

    @Override
    @Transactional
    public UsuarioListadoDTO aprobarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));
        if (usuario.getEstado() != EstadoUsuario.pendiente) {
            throw new NegocioException("La cuenta no está pendiente de aprobación");
        }
        usuario.setEstado(EstadoUsuario.activo);
        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioListadoDTO rechazarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));
        if (usuario.getEstado() != EstadoUsuario.pendiente) {
            throw new NegocioException("La cuenta no está pendiente de aprobación");
        }
        usuario.setEstado(EstadoUsuario.inactivo);
        refreshTokenRepository.revokeAllFromUsuario(usuario);
        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioListadoDTO registrarUsuario(UsuarioRegistroDTO dto) {
        String correo = dto.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new NegocioException("El correo ya está registrado");
        }
        if (dto.getContrasena() == null || dto.getContrasena().isBlank()) {
            throw new NegocioException("La contraseña es obligatoria");
        }

        Usuario nuevo = new Usuario();
        aplicarDatos(nuevo, dto, true);
        nuevo.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        nuevo.setEstado(estadoDesde(dto));

        Usuario guardado = usuarioRepository.save(nuevo);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public UsuarioListadoDTO editarUsuario(Integer id, UsuarioRegistroDTO dto) {
        Usuario usuarioDB = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

        if (!usuarioDB.getCorreo().equals(dto.getCorreo().trim().toLowerCase())
                && usuarioRepository.existsByCorreo(dto.getCorreo().trim().toLowerCase())) {
            throw new NegocioException("El correo ya está registrado");
        }

        RolUsuario rolNuevo = rolDesde(dto);

        // Protección: no se puede degradar el último administrador activo
        if (usuarioDB.getRol() == RolUsuario.ADMINISTRADOR
                && rolNuevo != RolUsuario.ADMINISTRADOR) {
            asegurarNoSerUltimoAdmin(usuarioDB);
        }

        aplicarDatos(usuarioDB, dto, false);

        if (dto.getContrasena() != null && !dto.getContrasena().trim().isEmpty()) {
            usuarioDB.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }

        if (dto.getEstado() != null) {
            EstadoUsuario estadoNuevo = estadoDesde(dto);
            if (usuarioDB.getRol() == RolUsuario.ADMINISTRADOR
                    && estadoNuevo == EstadoUsuario.inactivo) {
                asegurarNoSerUltimoAdmin(usuarioDB);
            }
            usuarioDB.setEstado(estadoNuevo);
        }

        Usuario guardado = usuarioRepository.save(usuarioDB);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public UsuarioListadoDTO cambiarEstadoUsuario(Integer id) {
        Usuario usuarioDB = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

        if (usuarioDB.getEstado() == EstadoUsuario.activo) {
            // Protección: no se puede desactivar el último administrador activo
            if (usuarioDB.getRol() == RolUsuario.ADMINISTRADOR) {
                asegurarNoSerUltimoAdmin(usuarioDB);
            }
            usuarioDB.setEstado(EstadoUsuario.inactivo);
            // Al desactivar, se cierran sus refresh tokens para que no pueda reingresar
            refreshTokenRepository.revokeAllFromUsuario(usuarioDB);
        } else {
            usuarioDB.setEstado(EstadoUsuario.activo);
        }

        Usuario guardado = usuarioRepository.save(usuarioDB);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public void resetPassword(Integer id, String nuevaContrasena) {
        Usuario usuarioDB = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

        usuarioDB.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuarioDB);

        // Cerrar todas las sesiones con la contraseña anterior
        refreshTokenRepository.revokeAllFromUsuario(usuarioDB);
    }

    /**
     * Elimina un usuario y TODO su árbol de datos (empresas, proyectos, lotes,
     * ventas, comisiones, pagos, comprobantes, proformas, clientes, parámetros,
     * integración SUNAT, sesiones y tokens).
     *
     * El orden respeta las claves foráneas reales:
     * proforma_detalle → proforma, lote → proyecto, refresh_token/sesion_activa → usuario.
     *
     * @return número de filas eliminadas (para la auditoría)
     */
    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

        // Protección: un administrador no puede borrarse a sí mismo
        Integer idActual = TenantContext.idUsuarioActual();
        if (idActual != null && idActual.equals(id)) {
            throw new NegocioException("No puede eliminar su propia cuenta");
        }

        // Protección: no se puede borrar al único administrador activo
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            asegurarNoSerUltimoAdmin(usuario);
        }

        Integer v = usuario.getId();

        // 1) Hoja del árbol de comisiones
        pagoComisionRepository.deleteByIdVendedor(v);
        comisionRepository.deleteByIdVendedor(v);

        // 2) Comprobantes (pagos y detalles primero)
        comprobantePagoRepository.deleteByIdVendedor(v);
        detalleComprobanteRepository.deleteByIdVendedor(v);
        comprobanteRepository.deleteByIdVendedor(v);

        // 3) Proformas (JPA borra en cascada sus detalles)
        proformaRepository.deleteByIdVendedor(v);
        secuenciaProformaRepository.deleteByIdVendedor(v);

        // 4) Ventas
        ventaRepository.deleteByIdVendedor(v);

        // 5) Lotes y proyectos (FK real: lote.id_proyecto → proyecto)
        loteRepository.deleteByIdVendedor(v);
        proyectoRepository.deleteByIdVendedor(v);

        // 6) Empresas y su numeración de proformas
        for (Empresa empresa : empresaRepository.findByIdVendedorOrderByRazonSocialAsc(v)) {
            secuenciaRepository.deleteByIdEmpresa(empresa.getId());
        }
        empresaRepository.deleteByIdVendedor(v);

        // 7) Clientes y configuración del vendedor
        clienteRepository.deleteByIdVendedor(v);
        integracionSunatRepository.deleteByIdVendedor(v);
        parametroComisionRepository.deleteByIdVendedor(v);
        vendedorEmpresaRepository.deleteByIdVendedor(v);

        // 8) Sesiones y tokens (FK real hacia usuario)
        sesionActivaRepository.deleteByUsuarioId(v);
        refreshTokenRepository.deleteByUsuarioId(v);

        // 9) El usuario, al final
        usuarioRepository.delete(usuario);
    }

    /** Perfil del usuario autenticado (sus propios datos). */
    @Override
    @Transactional(readOnly = true)
    public PerfilDTO perfilActual() {
        Integer id = TenantContext.idUsuarioActual();
        if (id == null) {
            throw new NegocioException("No se pudo identificar al usuario autenticado");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));
        return new PerfilDTO(
                usuario.getId(), usuario.getNombres(), usuario.getApellidos(),
                usuario.getCorreo(), usuario.getTelefono(), usuario.getDni(),
                usuario.getRol().name());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void aplicarDatos(Usuario usuario, UsuarioRegistroDTO dto, boolean enCreacion) {
        usuario.setNombres(dto.getNombres().trim());
        usuario.setApellidos(dto.getApellidos().trim());
        usuario.setCorreo(dto.getCorreo().trim().toLowerCase());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDni(dto.getDni());
        usuario.setRol(rolDesde(dto));

        if (enCreacion && usuario.getRol() == null) {
            usuario.setRol(RolUsuario.VENDEDOR);
        }
    }

    private RolUsuario rolDesde(UsuarioRegistroDTO dto) {
        try {
            return RolUsuario.valueOf(dto.getRol().trim().toUpperCase());
        } catch (Exception e) {
            throw new NegocioException("Rol no válido: use ADMINISTRADOR o VENDEDOR");
        }
    }

    private EstadoUsuario estadoDesde(UsuarioRegistroDTO dto) {
        return "inactivo".equalsIgnoreCase(dto.getEstado())
                ? EstadoUsuario.inactivo : EstadoUsuario.activo;
    }

    /** Lanza excepción si el usuario dado es el último administrador activo
     *  y la operación lo dejaría fuera (degradación a vendedor o desactivación). */
    private void asegurarNoSerUltimoAdmin(Usuario usuario) {
        if (usuario.getEstado() != EstadoUsuario.activo
                || usuario.getRol() != RolUsuario.ADMINISTRADOR) {
            return;
        }
        long adminsActivos = usuarioRepository.findByRol(RolUsuario.ADMINISTRADOR).stream()
                .filter(u -> u.getEstado() == EstadoUsuario.activo)
                .count();
        if (adminsActivos <= 1) {
            throw new NegocioException(
                    "Operación no permitida: es el único administrador activo del sistema");
        }
    }

    private UsuarioListadoDTO toDTO(Usuario u) {
        return new UsuarioListadoDTO(
                u.getId(), u.getNombres(), u.getApellidos(), u.getCorreo(),
                u.getTelefono(), u.getDni(), u.getRol().name(),
                u.getEstado().name(), u.getFechaRegistro());
    }
}
