package com.vendelote.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vendelote.entity.Usuario;
import com.vendelote.enums.EstadoUsuario;
import com.vendelote.enums.RolUsuario;
import com.vendelote.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Semilla inicial: si la tabla usuario está vacía, crea el primer
 * administrador (y un vendedor de prueba) para poder entrar al sistema.
 * Los datos son configurables por propiedad.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@vendelote.com}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin1234}")
    private String adminPassword;

    @Value("${app.seed.create-demo-vendedor:true}")
    private boolean createDemoVendedor;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Ya existen usuarios, se omite la semilla inicial.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("Sistema");
        admin.setCorreo(adminEmail.toLowerCase());
        admin.setContrasena(passwordEncoder.encode(adminPassword));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(EstadoUsuario.activo);
        usuarioRepository.save(admin);

        if (createDemoVendedor) {
            Usuario vendedor = new Usuario();
            vendedor.setNombres("Vendedor");
            vendedor.setApellidos("Demo");
            vendedor.setCorreo("vendedor@vendelote.com");
            vendedor.setContrasena(passwordEncoder.encode("Vendedor123"));
            vendedor.setRol(RolUsuario.VENDEDOR);
            vendedor.setEstado(EstadoUsuario.activo);
            usuarioRepository.save(vendedor);
        }

        log.warn("════════════════════════════════════════════════════════");
        log.warn(" USUARIO ADMIN INICIAL CREADO: {} / {}", adminEmail, adminPassword);
        if (createDemoVendedor) {
            log.warn(" VENDEDOR DEMO CREADO: vendedor@vendelote.com / Vendedor123");
            log.warn(" En produccion desactive la semilla demo con SEED_DEMO_VENDEDOR=false.");
        }
        log.warn(" CAMBIA LA CONTRASEÑA DEL ADMIN TRAS EL PRIMER INICIO.");
        if ("Admin1234".equals(adminPassword)) {
            log.warn(" La contraseña del admin es la de fábrica. Use SEED_ADMIN_PASSWORD en produccion.");
        }
        log.warn("════════════════════════════════════════════════════════");
    }
}
