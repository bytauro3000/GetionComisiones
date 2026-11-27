package com.vendelote.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import com.vendelote.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // ── Rutas públicas (sin autenticación) ────────────────────────────
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/api/proformas/pdf-publico/**",
                    "/ws/**",
                    "/actuator/health",
                    "/error"
                ).permitAll()
                // ── Gestión de usuarios, auditoría y sesiones: solo ADMINISTRADOR ──
                .requestMatchers(
                    "/api/usuarios/**",
                    "/api/auditoria/**",
                    "/api/admin/**"
                ).hasAuthority("ROLE_ADMINISTRADOR")
                // ── Vendedores asociados: solo el jefe de ese equipo ─────────────
                .requestMatchers("/api/asociados/**").hasAuthority("ROLE_VENDEDOR")
                // ── Escrituras de operación: solo el jefe (el asociado es lectura)
                //    El asociado sí puede crear clientes y proformas.
                .requestMatchers(
                    org.springframework.http.HttpMethod.POST,
                    "/api/ventas/**",
                    "/api/lotes/**",
                    "/api/proyectos/**",
                    "/api/empresas/**",
                    "/api/socios/**",
                    "/api/liquidaciones/**",
                    "/api/comprobantes/**",
                    "/api/parametros-comision/**",
                    "/api/comisiones/**"
                ).hasAnyAuthority("ROLE_VENDEDOR", "ROLE_ADMINISTRADOR")
                .requestMatchers(
                    org.springframework.http.HttpMethod.PUT,
                    "/api/ventas/**",
                    "/api/lotes/**",
                    "/api/proyectos/**",
                    "/api/empresas/**",
                    "/api/socios/**",
                    "/api/liquidaciones/**",
                    "/api/comprobantes/**",
                    "/api/parametros-comision/**",
                    "/api/comisiones/**"
                ).hasAnyAuthority("ROLE_VENDEDOR", "ROLE_ADMINISTRADOR")
                .requestMatchers(
                    org.springframework.http.HttpMethod.PATCH,
                    "/api/ventas/**",
                    "/api/lotes/**",
                    "/api/proyectos/**",
                    "/api/empresas/**",
                    "/api/socios/**",
                    "/api/liquidaciones/**",
                    "/api/comprobantes/**",
                    "/api/parametros-comision/**",
                    "/api/comisiones/**"
                ).hasAnyAuthority("ROLE_VENDEDOR", "ROLE_ADMINISTRADOR")
                .requestMatchers(
                    org.springframework.http.HttpMethod.DELETE,
                    "/api/ventas/**",
                    "/api/lotes/**",
                    "/api/proyectos/**",
                    "/api/empresas/**",
                    "/api/socios/**",
                    "/api/liquidaciones/**",
                    "/api/comprobantes/**",
                    "/api/parametros-comision/**",
                    "/api/comisiones/**"
                ).hasAnyAuthority("ROLE_VENDEDOR", "ROLE_ADMINISTRADOR")
                // Módulos que no forman parte del trabajo del asociado.
                .requestMatchers(
                    "/api/comprobantes/**",
                    "/api/liquidaciones/**",
                    "/api/socios/**",
                    "/api/parametros-comision/**"
                ).hasAnyAuthority("ROLE_VENDEDOR", "ROLE_ADMINISTRADOR")
                // ── Cualquier otra ruta requiere sesión válida ────────────────────
                .anyRequest().authenticated()
            )
            // ── Sin token / token expirado → 401 (el interceptor del frontend
            //    usa el 401 para intentar refrescar el access token) ──────────
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    "{\"error\":\"unauthorized\",\"mensaje\":\"No autenticado o token expirado\"}");
            }))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * CORS: el frontend corre en otro puerto/dominio y envía cookies
     * (refresh token) con credenciales, por lo que debe listarse el origen.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Patrones: permite fijar el dominio de Vercel y, si se desea, https://*.vercel.app
        cfg.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
