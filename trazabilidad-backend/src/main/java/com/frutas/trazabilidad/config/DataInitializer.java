package com.frutas.trazabilidad.config;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializa datos de prueba en la base de datos.
 * Solo se ejecuta si no existen datos previos.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Verificar si ya existen datos
            if (userRepository.count() > 0) {
                log.info("✓ Base de datos ya contiene datos, omitiendo inicialización");
                return;
            }

            log.info("⚙ Inicializando datos de prueba...");

            // Crear empresa de prueba
            Empresa empresa = Empresa.builder()
                    .nit("900123456-7")
                    .razonSocial("Frutas Tropicales S.A.S")
                    .nombreComercial("TropicoFresh")
                    .email("contacto@tropicofruta.com")
                    .telefono("+57 300 1234567")
                    .direccion("Calle 10 #15-20, Dosquebradas, Risaralda")
                    .activo(true)
                    .build();
            empresa = empresaRepository.save(empresa);
            log.info("✓ Empresa creada: {}", empresa.getRazonSocial());

            // Crear usuario administrador
            User admin = User.builder()
                    .email("admin@test.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .nombre("Admin")
                    .apellido("Sistema")
                    .telefono("+57 300 1111111")
                    .empresa(empresa)
                    .rol(TipoRol.ADMIN)
                    .activo(true)
                    .build();
            userRepository.save(admin);
            log.info("✓ Usuario admin creado");

            // Crear usuario productor
            User productor = User.builder()
                    .email("productor@test.com")
                    .passwordHash(passwordEncoder.encode("productor123"))
                    .nombre("Juan")
                    .apellido("Productor")
                    .telefono("+57 300 2222222")
                    .empresa(empresa)
                    .rol(TipoRol.PRODUCTOR)
                    .activo(true)
                    .build();
            userRepository.save(productor);
            log.info("✓ Usuario productor creado");

            // Crear usuario operador planta
            User operadorPlanta = User.builder()
                    .email("planta@test.com")
                    .passwordHash(passwordEncoder.encode("planta123"))
                    .nombre("María")
                    .apellido("Operadora")
                    .telefono("+57 300 3333333")
                    .empresa(empresa)
                    .rol(TipoRol.OPERADOR_PLANTA)
                    .activo(true)
                    .build();
            userRepository.save(operadorPlanta);
            log.info("✓ Usuario operador planta creado");

            log.info("=".repeat(60));
            log.info("✅ Datos de prueba inicializados correctamente");
            log.info("=".repeat(60));
            log.info("📧 Usuarios disponibles:");
            log.info("   Admin:      admin@test.com / admin123");
            log.info("   Productor:  productor@test.com / productor123");
            log.info("   Planta:     planta@test.com / planta123");
            log.info("=".repeat(60));
        };
    }
}