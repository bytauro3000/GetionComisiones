package com.vendelote.security;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.vendelote.entity.Usuario;
import com.vendelote.enums.RolUsuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 1000 * 60 * 30; // 30 minutos

    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret-key:}") String secretKey, Environment environment) {
        if (secretKey == null || secretKey.isBlank()) {
            // En producción el secreto es OBLIGATORIO: sin él la app no debe arrancar.
            if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
                throw new IllegalStateException(
                        "JWT_SECRET no está definido. Configure la variable de entorno JWT_SECRET "
                                + "(base64 de 48+ bytes, generada con: openssl rand -base64 48).");
            }
            // En desarrollo se genera una clave efímera (se invalida al reiniciar).
            // El refresh token (UUID en BD) permite recuperar la sesión automáticamente.
            byte[] random = new byte[48];
            new SecureRandom().nextBytes(random);
            this.secretKey = Base64.getEncoder().encodeToString(random);
            log.warn("JWT_SECRET no definido: se usó una clave efímera de desarrollo. "
                    + "Los access tokens se invalidan al reiniciar el servidor.");
        } else {
            this.secretKey = secretKey;
        }
    }

    /** Único responsable de crear el token en el Login. */
    public String generateToken(Authentication authentication, Usuario usuario) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", userDetails.getAuthorities().iterator().next().getAuthority());
        claims.put("nombre", usuario.getNombres());
        claims.put("apellidos", usuario.getApellidos());
        claims.put("id", usuario.getId());
        // Multi-tenant: el vendedor ES el tenant; el admin de plataforma no lleva tid
        if (usuario.getRol() == RolUsuario.VENDEDOR) {
            claims.put("tid", usuario.getId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Token generado desde el usuario (usado por el refresh rotativo). */
    public String generateTokenFromUsuario(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", "ROLE_" + usuario.getRol().name().toUpperCase());
        claims.put("nombre", usuario.getNombres());
        claims.put("apellidos", usuario.getApellidos());
        claims.put("id", usuario.getId());
        if (usuario.getRol() == RolUsuario.VENDEDOR) {
            claims.put("tid", usuario.getId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getCorreo())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Lanza excepción si la firma es inválida o el token expiró. */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** true solo si la firma es válida y el token NO ha expirado. */
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
