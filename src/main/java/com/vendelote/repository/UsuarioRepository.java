package com.vendelote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vendelote.entity.Usuario;
import com.vendelote.enums.RolUsuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRolAndEstado(RolUsuario rol, com.vendelote.enums.EstadoUsuario estado);

    List<Usuario> findByRol(RolUsuario rol);

    long countByRol(RolUsuario rol);
}
