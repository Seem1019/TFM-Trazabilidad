package com.frutas.trazabilidad.config;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.produccion.entity.*;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.produccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga datos de prueba en la base de datos al iniciar la aplicación.
 * Solo se ejecuta en perfil 'dev' o 'test'.
 */
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final FincaRepository fincaRepository;
    private final CertificacionRepository certificacionRepository;
    private final LoteRepository loteRepository;
    private final ActividadAgronomicarepository actividadRepository;
    private final CosechaRepository cosechaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🌱 Iniciando carga de datos de prueba...");

        // Verificar si ya existen datos
        if (empresaRepository.count() > 0) {
            log.info("✅ Los datos de prueba ya existen. Omitiendo seeder.");
            return;
        }

        try {
            // 1. Crear Empresas
            Empresa empresa1 = crearEmpresa("Frutas Tropicales S.A.S", "900123456-7", "admin@frutas.com");
            Empresa empresa2 = crearEmpresa("AgroExport Colombia Ltda", "900654321-3", "admin@agroexport.com");

            // 2. Crear Usuarios
            crearUsuarios(empresa1, empresa2);

            // 3. Crear Fincas
            Finca finca1 = crearFinca(empresa1, "FCA-001", "La Esperanza", "Dosquebradas", 15.5);
            Finca finca2 = crearFinca(empresa1, "FCA-002", "El Paraíso", "Pereira", 22.3);
            Finca finca3 = crearFinca(empresa2, "FNC-001", "Villa María", "Manizales", 18.0);

            // 4. Crear Certificaciones
            crearCertificacion(finca1, "GlobalG.A.P.", "Control Union", "GG-2024-001");
            crearCertificacion(finca1, "Orgánico", "Ecocert", "ORG-2024-045");
            crearCertificacion(finca2, "GlobalG.A.P.", "Control Union", "GG-2024-002");
            crearCertificacion(finca3, "Rainforest Alliance", "RA Certified", "RA-2024-120");

            // 5. Crear Lotes
            Lote lote1 = crearLote(finca1, "LOTE-AGU-001", "Lote Aguacate 1", "Aguacate", "Hass", 3.2);
            Lote lote2 = crearLote(finca1, "LOTE-MAN-001", "Lote Mango 1", "Mango", "Tommy", 4.5);
            Lote lote3 = crearLote(finca2, "LOTE-BAN-001", "Lote Banano 1", "Banano", "Cavendish", 6.0);
            Lote lote4 = crearLote(finca3, "LOTE-CAF-001", "Lote Café 1", "Café", "Caturra", 8.5);

            // 6. Crear Actividades Agronómicas
            crearActividad(lote1, "FERTILIZACIÓN", "Abono Orgánico", "50.0", LocalDate.now().minusDays(15));
            crearActividad(lote1, "FUMIGACIÓN", "Fungicida BioX", "2.5", LocalDate.now().minusDays(10));
            crearActividad(lote2, "RIEGO", null, "1000.0", LocalDate.now().minusDays(5));
            crearActividad(lote3, "PODA", null, null, LocalDate.now().minusDays(20));

            // 7. Crear Cosechas
            crearCosecha(lote2, 850.5, "kg", "PRIMERA", LocalDate.now().minusDays(3));
            crearCosecha(lote3, 1200.0, "kg", "PREMIUM", LocalDate.now().minusDays(1));

            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("📊 Resumen:");
            log.info("   - Empresas: {}", empresaRepository.count());
            log.info("   - Usuarios: {}", userRepository.count());
            log.info("   - Fincas: {}", fincaRepository.count());
            log.info("   - Certificaciones: {}", certificacionRepository.count());
            log.info("   - Lotes: {}", loteRepository.count());
            log.info("   - Actividades: {}", actividadRepository.count());
            log.info("   - Cosechas: {}", cosechaRepository.count());

        } catch (Exception e) {
            log.error("❌ Error al cargar datos de prueba: {}", e.getMessage(), e);
        }
    }

    private Empresa crearEmpresa(String razonSocial, String nit, String email) {
        Empresa empresa = Empresa.builder()
                .razonSocial(razonSocial)
                .nit(nit)
                .email(email)
                .telefono("+57 300 1234567")
                .direccion("Calle 123 #45-67, Pereira, Colombia")
                .activo(true)
                .build();
        empresa = empresaRepository.save(empresa);
        log.debug("✓ Empresa creada: {} (ID: {})", empresa.getRazonSocial(), empresa.getId());
        return empresa;
    }

    private void crearUsuarios(Empresa empresa1, Empresa empresa2) {
        // Usuario admin de Empresa 1
        User admin1 = User.builder()
                .email("admin@frutas.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .nombre("Administrador")
                .apellido("Frutas")
                .rol(TipoRol.ADMIN)
                .empresa(empresa1)
                .activo(true)
                .build();
        userRepository.save(admin1);
        log.debug("✓ Usuario creado: {}", admin1.getEmail());

        // Usuario productor de Empresa 1
        User productor1 = User.builder()
                .email("productor@frutas.com")
                .passwordHash(passwordEncoder.encode("prod123"))
                .nombre("Juan")
                .apellido("Pérez")
                .rol(TipoRol.PRODUCTOR)
                .empresa(empresa1)
                .activo(true)
                .build();
        userRepository.save(productor1);
        log.debug("✓ Usuario creado: {}", productor1.getEmail());

        // Usuario operador de Empresa 1
        User operador1 = User.builder()
                .email("planta@frutas.com")
                .passwordHash(passwordEncoder.encode("planta123"))
                .nombre("María")
                .apellido("González")
                .rol(TipoRol.OPERADOR_PLANTA)
                .empresa(empresa1)
                .activo(true)
                .build();
        userRepository.save(operador1);
        log.debug("✓ Usuario creado: {}", operador1.getEmail());

        // Usuario admin de Empresa 2
        User admin2 = User.builder()
                .email("admin@agroexport.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .nombre("Carlos")
                .apellido("Ramírez")
                .rol(TipoRol.ADMIN)
                .empresa(empresa2)
                .activo(true)
                .build();
        userRepository.save(admin2);
        log.debug("✓ Usuario creado: {}", admin2.getEmail());
    }

    private Finca crearFinca(Empresa empresa, String codigo, String nombre, String municipio, double area) {
        Finca finca = Finca.builder()
                .empresa(empresa)
                .codigoFinca(codigo)
                .nombre(nombre)
                .ubicacion("Vereda Alto del Río")
                .municipio(municipio)
                .departamento("Risaralda")
                .pais("Colombia")
                .areaHectareas(area)
                .propietario(empresa.getRazonSocial())
                .encargado("Encargado " + nombre)
                .telefono("+57 310 9876543")
                .email(codigo.toLowerCase() + "@finca.com")
                .latitud(4.813 + Math.random() * 0.1)
                .longitud(-75.696 - Math.random() * 0.1)
                .activo(true)
                .build();
        finca = fincaRepository.save(finca);
        log.debug("✓ Finca creada: {} (ID: {})", finca.getNombre(), finca.getId());
        return finca;
    }

    private void crearCertificacion(Finca finca, String tipo, String emisora, String numero) {
        Certificacion cert = Certificacion.builder()
                .finca(finca)
                .tipoCertificacion(tipo)
                .entidadEmisora(emisora)
                .numeroCertificado(numero)
                .fechaEmision(LocalDate.now().minusMonths(6))
                .fechaVencimiento(LocalDate.now().plusMonths(18))
                .estado("VIGENTE")
                .activo(true)
                .build();
        certificacionRepository.save(cert);
        log.debug("✓ Certificación creada: {} para {}", tipo, finca.getNombre());
    }

    private Lote crearLote(Finca finca, String codigo, String nombre, String tipoFruta, String variedad, double area) {
        Lote lote = Lote.builder()
                .finca(finca)
                .codigoLote(codigo)
                .nombre(nombre)
                .tipoFruta(tipoFruta)
                .variedad(variedad)
                .areaHectareas(area)
                .fechaSiembra(LocalDate.now().minusMonths(12))
                .fechaPrimeraCosechaEstimada(LocalDate.now().plusMonths(2))
                .densidadSiembra(300)
                .ubicacionInterna("Sector Norte")
                .estadoLote("ACTIVO")
                .activo(true)
                .build();
        lote = loteRepository.save(lote);
        log.debug("✓ Lote creado: {} (ID: {})", lote.getNombre(), lote.getId());
        return lote;
    }

    private void crearActividad(Lote lote, String tipo, String producto, String dosis, LocalDate fecha) {
        ActividadAgronomica actividad = ActividadAgronomica.builder()
                .lote(lote)
                .tipoActividad(tipo)
                .fechaActividad(fecha)
                .productoAplicado(producto)
                .dosisoCantidad(dosis)
                .unidadMedida(producto != null ? "kg" : null)
                .metodoAplicacion("Manual")
                .responsable("Técnico Agrícola")
                .intervaloSeguridadDias(producto != null ? 15 : 0)
                .activo(true)
                .build();
        actividadRepository.save(actividad);
        log.debug("✓ Actividad creada: {} en {}", tipo, lote.getNombre());
    }

    private void crearCosecha(Lote lote, double cantidad, String unidad, String calidad, LocalDate fecha) {
        Cosecha cosecha = Cosecha.builder()
                .lote(lote)
                .fechaCosecha(fecha)
                .cantidadCosechada(cantidad)
                .unidadMedida(unidad)
                .calidadInicial(calidad)
                .estadoFruta("MADURA")
                .responsableCosecha("Cosechero Principal")
                .numeroTrabajadores(5)
                .horaInicio("06:00")
                .horaFin("12:00")
                .temperaturaAmbiente(22.5)
                .activo(true)
                .build();
        cosechaRepository.save(cosecha);
        log.debug("✓ Cosecha creada: {} {} en {}", cantidad, unidad, lote.getNombre());
    }
}