package com.vendelote.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vendelote.dto.LoginRequest;
import com.vendelote.dto.LoginResponse;
import com.vendelote.dto.MensajeErrorDTO;
import com.vendelote.dto.RegistroPublicoDTO;
import com.vendelote.dto.UsuarioListadoDTO;
import com.vendelote.entity.RefreshToken;
import com.vendelote.entity.Usuario;
import com.vendelote.enums.AccionAuditoria;
import com.vendelote.enums.EstadoUsuario;
import com.vendelote.exception.NegocioException;
import com.vendelote.security.JwtUtil;
import com.vendelote.service.AuditoriaService;
import com.vendelote.service.RefreshTokenService;
import com.vendelote.service.SesionActivaService;
import com.vendelote.service.UsuarioService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Autenticación del monolito: login, refresh rotativo (cookie HttpOnly),
 * logout y validación. Sin gateway: el token se firma y valida aquí.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;
    private final SesionActivaService sesionActivaService;
    private final AuditoriaService auditoriaService;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        // Cuentas de auto-registro sin aprobar: se responde ANTES de autenticar
        // (403 y no 401, para que el interceptor no intente refrescar el token)
        Usuario usuario = usuarioService.buscarByCorreo(loginRequest.getCorreo());
        if (usuario != null && usuario.getEstado() == EstadoUsuario.pendiente) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MensajeErrorDTO(
                            "Cuenta pendiente de aprobación: espere a que el administrador la revise"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getCorreo(), loginRequest.getContrasena())
        );

        usuario = usuarioService.buscarByCorreo(loginRequest.getCorreo());

        String token = jwtUtil.generateToken(authentication, usuario);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario.getId());
        agregarCookieRefreshToken(response, refreshToken.getToken());

        // Registrar sesión activa
        String ip = obtenerIpCliente(request);
        String userAgent = request.getHeader("User-Agent");
        sesionActivaService.registrarLogin(usuario, ip, userAgent);

        auditoriaService.registrar(AccionAuditoria.LOGIN, "Usuario", usuario.getId(),
                "LOGIN exitoso: " + usuario.getCorreo() + " | IP: " + ip);

        // El refresh token NO se devuelve en el body, solo en la cookie HttpOnly
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * Auto-registro público de vendedor: crea la cuenta en estado PENDIENTE
     * y el administrador debe aprobarla antes de que pueda iniciar sesión.
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroPublicoDTO dto,
                                       HttpServletRequest request) {
        UsuarioListadoDTO creado = usuarioService.autoRegistro(dto);
        auditoriaService.registrar(AccionAuditoria.REGISTRO, "Usuario", creado.getId(),
                "Auto-registro pendiente: " + creado.getCorreo()
                        + " | Documento: "
                        + (creado.getTipoDocumento() == null ? "" : creado.getTipoDocumento() + " ")
                        + (creado.getNumDocumento() == null ? "-" : creado.getNumDocumento())
                        + " | IP: " + obtenerIpCliente(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Cuenta creada correctamente. Debe ser aprobada por un administrador "
                        + "antes de poder iniciar sesión."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        // Actualizar última actividad de la sesión antes de rotar el token
        Integer usuarioId = refreshTokenService.findUsuarioIdByToken(refreshToken);
        if (usuarioId != null) {
            sesionActivaService.actualizarRefresh(usuarioId);
        }

        LoginResponse loginResponse;
        try {
            loginResponse = refreshTokenService.refreshAccessToken(refreshToken);
        } catch (NegocioException e) {
            // Refresh inválido/expirado: limpiar cookie y pedir login de nuevo
            eliminarCookieRefreshToken(response);
            return ResponseEntity.status(401).build();
        }

        agregarCookieRefreshToken(response, loginResponse.getRefreshToken());

        // El nuevo refresh token se envía por cookie, no por body
        return ResponseEntity.ok(new LoginResponse(loginResponse.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                       HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            Integer usuarioId = refreshTokenService.findUsuarioIdByToken(refreshToken);
            if (usuarioId != null) {
                sesionActivaService.desactivarSesion(usuarioId);
            }
            refreshTokenService.revokeRefreshToken(refreshToken);
            auditoriaService.registrar(AccionAuditoria.LOGOUT, "Usuario", usuarioId, "LOGOUT");
        }
        eliminarCookieRefreshToken(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam(value = "token", required = false) String token) {
        // Valida firma y expiración reales (no solo la presencia del token).
        return ResponseEntity.ok(jwtUtil.isTokenValid(token));
    }

    private void agregarCookieRefreshToken(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 días (coincide con refresh token en BD)
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
    }

    private void eliminarCookieRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
