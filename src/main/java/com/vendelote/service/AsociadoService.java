package com.vendelote.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.AsociadoDTO;
import com.vendelote.dto.AsociadoResumenDTO;
import com.vendelote.entity.Usuario;
import com.vendelote.enums.EstadoUsuario;
import com.vendelote.enums.RolUsuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.security.TenantContext;

import lombok.RequiredArgsConstructor;

/**
 * Gestión de vendedores asociados de un vendedor jefe.
 *
 * El asociado NO es un tenant independiente: comparte el idVendedor del jefe
 * (claim tid del JWT). Su idUsuario se usa para atribuir ventas, proformas y
 * clientes.
 */
@Service
@RequiredArgsConstructor
public class AsociadoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AsociadoResumenDTO> listar() {
        return usuarioRepository
                .findByIdVendedorJefeOrderByNombresAsc(jefeActual())
                .stream()
                .filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                .map(this::resumen)
                .toList();
    }

    @Transactional
    public AsociadoResumenDTO crear(AsociadoDTO dto) {
        Integer idJefe = jefeActual();
        String correo = dto.getCorreo().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new NegocioException("Ya existe una cuenta con ese correo");
        }

        Usuario asociado = new Usuario();
        asociado.setNombres(dto.getNombres().trim());
        asociado.setApellidos(dto.getApellidos().trim());
        asociado.setCorreo(correo);
        asociado.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        asociado.setTelefono(limpiar(dto.getTelefono()));
        String tipo = tipoDocumento(dto.getTipoDocumento());
        String numero = limpiar(dto.getNumDocumento() != null
                ? dto.getNumDocumento() : dto.getDni());
        asociado.setTipoDocumento(tipo);
        asociado.setNumDocumento(numero);
        asociado.setDni("DNI".equals(tipo) ? numero : null);
        asociado.setRol(RolUsuario.VENDEDOR_ASOCIADO);
        asociado.setIdVendedorJefe(idJefe);
        asociado.setEstado(EstadoUsuario.activo);

        return resumen(usuarioRepository.save(asociado));
    }

    @Transactional
    public AsociadoResumenDTO cambiarEstado(Integer id, String estado) {
        Usuario asociado = buscarDelJefe(id);
        EstadoUsuario nuevo;
        try {
            nuevo = EstadoUsuario.valueOf(estado.trim().toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            throw new NegocioException("Estado no válido: use activo o inactivo");
        }
        asociado.setEstado(nuevo);
        return resumen(usuarioRepository.save(asociado));
    }

    @Transactional
    public void resetPassword(Integer id, String contrasena) {
        if (contrasena == null || contrasena.trim().length() < 6) {
            throw new NegocioException("La contraseña debe tener al menos 6 caracteres");
        }
        Usuario asociado = buscarDelJefe(id);
        asociado.setContrasena(passwordEncoder.encode(contrasena.trim()));
        usuarioRepository.save(asociado);
    }

    /**
     * Devuelve el id del asociado que se debe atribuir a una operación.
     * - Si la sesión es de un asociado, siempre se atribuye a él.
     * - Si es del jefe, puede indicar un asociado; null significa operación del jefe.
     */
    @Transactional(readOnly = true)
    public Integer resolverAsociado(Integer idSolicitado) {
        if (TenantContext.esAsociado()) {
            return TenantContext.idUsuarioActual();
        }
        if (idSolicitado == null) {
            return null;
        }
        Usuario asociado = buscarDelJefe(idSolicitado);
        if (asociado.getEstado() != EstadoUsuario.activo) {
            throw new NegocioException("El vendedor asociado está inactivo");
        }
        return asociado.getId();
    }

    @Transactional(readOnly = true)
    public Integer jefeActual() {
        if (!TenantContext.esVendedorJefe()) {
            throw new NegocioException("Esta operación requiere una cuenta de vendedor jefe");
        }
        return TenantContext.idUsuarioActual();
    }

    private Usuario buscarDelJefe(Integer id) {
        Integer idJefe = jefeActual();
        return usuarioRepository.findByIdAndIdVendedorJefe(id, idJefe)
                .filter(u -> u.getRol() == RolUsuario.VENDEDOR_ASOCIADO)
                .orElseThrow(() -> new NegocioException("Vendedor asociado no encontrado"));
    }

    private AsociadoResumenDTO resumen(Usuario u) {
        return new AsociadoResumenDTO(
                u.getId(), u.getNombres(), u.getApellidos(), u.getCorreo(),
                u.getTelefono(), u.getDni(),
                u.getTipoDocumento(), u.getNumDocumento(),
                u.getEstado() != null ? u.getEstado().name() : null,
                u.getFechaRegistro()
        );
    }

    private String tipoDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return "DNI";
        }
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
