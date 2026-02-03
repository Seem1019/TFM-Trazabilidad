package com.frutas.trazabilidad.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuración de JPA que garantiza que Flyway se ejecute primero.
 * La anotación @DependsOn("flyway") fuerza que el bean de Flyway
 * se cree y ejecute antes de crear el EntityManagerFactory.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.frutas.trazabilidad")
@Slf4j
public class JpaConfig {

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    /**
     * EntityManagerFactory que depende explícitamente de Flyway.
     * Esto garantiza que las migraciones se ejecuten antes de la validación del schema.
     */
    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            Flyway flyway) {  // Inyectar Flyway para forzar la dependencia

        log.info("=================================================");
        log.info("🔧 Configurando EntityManagerFactory...");
        log.info("📌 DDL Auto: {}", ddlAuto);
        log.info("📌 Flyway ejecutado: ✅");
        log.info("=================================================");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.frutas.trazabilidad");
        em.setPersistenceUnitName("default");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setGenerateDdl(false);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.jdbc.time_zone", "UTC");
        properties.setProperty("hibernate.format_sql", String.valueOf(showSql));
        // Estrategia de naming: convierte camelCase a snake_case (ej: camposModificados -> campos_modificados)
        properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
