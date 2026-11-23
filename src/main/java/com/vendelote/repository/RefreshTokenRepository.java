package com.vendelote.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vendelote.entity.RefreshToken;
import com.vendelote.entity.Usuario;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken r where r.usuario = :usuario and (r.revoked = true or r.expiryDate < :now)")
    void deleteExpiredOrRevoked(@Param("usuario") Usuario usuario, @Param("now") Date now);

    /** Revoca todos los tokens vigentes de un usuario (reset de password, cierre de sesión). */
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.usuario = :usuario and r.revoked = false")
    void revokeAllFromUsuario(@Param("usuario") Usuario usuario);

    /** Borrado en cascada: elimina los tokens (revocados o no) del usuario. */
    void deleteByUsuarioId(Integer idUsuario);
}
