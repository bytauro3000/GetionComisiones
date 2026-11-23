package com.vendelote.service;

import java.util.List;

import com.vendelote.dto.PerfilDTO;
import com.vendelote.dto.RegistroPublicoDTO;
import com.vendelote.dto.UsuarioListadoDTO;
import com.vendelote.dto.UsuarioRegistroDTO;
import com.vendelote.entity.Usuario;

public interface UsuarioService {

    Usuario buscarByCorreo(String correo);

    /** Listado para el panel admin; estado opcional ("activo"|"inactivo"|"pendiente"). */
    List<UsuarioListadoDTO> listarUsuarios(String estado);

    UsuarioListadoDTO registrarUsuario(UsuarioRegistroDTO dto);

    /** Auto-registro público: rol VENDEDOR y estado pendiente de aprobación. */
    UsuarioListadoDTO autoRegistro(RegistroPublicoDTO dto);

    UsuarioListadoDTO aprobarUsuario(Integer id);

    UsuarioListadoDTO rechazarUsuario(Integer id);

    UsuarioListadoDTO editarUsuario(Integer id, UsuarioRegistroDTO dto);

    UsuarioListadoDTO cambiarEstadoUsuario(Integer id);

    void resetPassword(Integer id, String nuevaContrasena);

    /** Elimina el usuario y TODO su árbol de datos (empresas, proyectos, lotes,
     *  ventas, comisiones, comprobantes, proformas, clientes, sesiones y tokens). */
    void eliminarUsuario(Integer id);

    /** Perfil del usuario autenticado (sus propios datos). */
    PerfilDTO perfilActual();
}
