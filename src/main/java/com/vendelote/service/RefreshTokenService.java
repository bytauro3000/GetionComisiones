package com.vendelote.service;

import com.vendelote.dto.LoginResponse;
import com.vendelote.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(Integer usuarioId);

    LoginResponse refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String token);

    Integer findUsuarioIdByToken(String token);
}
