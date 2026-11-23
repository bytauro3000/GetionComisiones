package com.vendelote.service.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendelote.dto.LoginResponse;
import com.vendelote.entity.RefreshToken;
import com.vendelote.entity.Usuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.repository.RefreshTokenRepository;
import com.vendelote.repository.UsuarioRepository;
import com.vendelote.security.JwtUtil;
import com.vendelote.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));

        // Limpiar solo tokens expirados o revocados.
        // NO se borran los tokens válidos de otras sesiones/dispositivos,
        // así el refresh no falla por rotación concurrente.
        refreshTokenRepository.deleteExpiredOrRevoked(usuario, new Date());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + refreshExpiration));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse refreshAccessToken(String requestToken) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(requestToken);

        if (optionalToken.isEmpty()) {
            throw new NegocioException("Refresh token no encontrado");
        }

        RefreshToken storedToken = optionalToken.get();

        if (storedToken.isRevoked()) {
            throw new NegocioException("Refresh token ya fue revocado");
        }

        if (storedToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(storedToken);
            throw new NegocioException("Refresh token expirado");
        }

        Usuario usuario = storedToken.getUsuario();

        // Un usuario inactivo no puede seguir refrescando su sesión
        if (usuario.getEstado() != com.vendelote.enums.EstadoUsuario.activo) {
            refreshTokenRepository.revokeAllFromUsuario(usuario);
            throw new NegocioException("Usuario inactivo");
        }

        String newAccessToken = jwtUtil.generateTokenFromUsuario(usuario);

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshToken = createRefreshToken(usuario.getId());

        return new LoginResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Override
    public Integer findUsuarioIdByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getUsuario().getId())
                .orElse(null);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
