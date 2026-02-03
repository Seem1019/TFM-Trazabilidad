package com.frutas.trazabilidad.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración explícita de Flyway para asegurar que las migraciones
 * se ejecuten antes de que Hibernate intente validar el esquema.
 *
 * IMPORTANTE: Este bean se ejecuta manualmente al crear el DataSource,
 * garantizando que las migraciones ocurran ANTES de la validación de Hibernate.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.baseline-version:0}")
    private String baselineVersion;

    @Value("${spring.flyway.validate-on-migrate:true}")
    private boolean validateOnMigrate;

    @Value("${spring.flyway.clean-disabled:true}")
    private boolean cleanDisabled;

    /**
     * Crea y ejecuta Flyway inmediatamente.
     * Este bean debe crearse antes que cualquier bean de JPA/Hibernate.
     */
    @Bean(initMethod = "")
    public Flyway flyway(DataSource dataSource) {
        log.info("=================================================");
        log.info("🔧 Configurando Flyway manualmente...");
        log.info("📁 Ubicación de migraciones: {}", locations);
        log.info("📌 Baseline on migrate: {}", baselineOnMigrate);
        log.info("📌 Baseline version: {}", baselineVersion);
        log.info("=================================================");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(validateOnMigrate)
                .cleanDisabled(cleanDisabled)
                .load();

        // Ejecutar migraciones inmediatamente
        log.info("🚀 Ejecutando migraciones de Flyway...");
        try {
            var result = flyway.migrate();
            log.info("✅ Migraciones completadas exitosamente!");
            log.info("   - Migraciones ejecutadas: {}", result.migrationsExecuted);
            log.info("   - Versión actual del schema: {}", result.targetSchemaVersion);
            if (result.warnings != null && !result.warnings.isEmpty()) {
                result.warnings.forEach(w -> log.warn("   ⚠️ {}", w));
            }
        } catch (Exception e) {
            log.error("❌ Error ejecutando migraciones de Flyway: {}", e.getMessage());
            throw e;
        }

        return flyway;
    }
}
