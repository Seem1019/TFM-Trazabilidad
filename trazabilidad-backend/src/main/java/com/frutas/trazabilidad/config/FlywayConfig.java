package com.frutas.trazabilidad.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuración explícita de Flyway para asegurar que las migraciones
 * se ejecuten antes de que Hibernate intente validar el esquema.
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

    @Bean
    @Primary
    public Flyway flyway(DataSource dataSource) {
        log.info("🔧 Configurando Flyway manualmente...");
        log.info("📁 Ubicación de migraciones: {}", locations);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(validateOnMigrate)
                .cleanDisabled(cleanDisabled)
                .load();

        log.info("🚀 Ejecutando migraciones de Flyway...");
        var result = flyway.migrate();
        log.info("✅ Migraciones de Flyway completadas: {} migraciones aplicadas", result.migrationsExecuted);

        return flyway;
    }
}
