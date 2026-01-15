package com.frutas.trazabilidad.config;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.produccion.entity.*;
import com.frutas.trazabilidad.module.empaque.entity.*;
import com.frutas.trazabilidad.module.logistica.entity.*;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.produccion.repository.*;
import com.frutas.trazabilidad.module.empaque.repository.*;
import com.frutas.trazabilidad.module.logistica.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Carga datos de prueba en la base de datos al iniciar la aplicación.
 * Solo se ejecuta en perfil 'dev' o 'test'.
 */
@Component
@Profile({"dev", "test","production"})
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Repositorios de Producción
    private final FincaRepository fincaRepository;
    private final CertificacionRepository certificacionRepository;
    private final LoteRepository loteRepository;
    private final ActividadAgronomicarepository actividadRepository;
    private final CosechaRepository cosechaRepository;

    // Repositorios de Empaque
    private final RecepcionPlantaRepository recepcionPlantaRepository;
    private final ClasificacionRepository clasificacionRepository;
    private final ControlCalidadRepository controlCalidadRepository;
    private final PalletRepository palletRepository;

    // Repositorios de Logística
    private final EnvioRepository envioRepository;
    private final DocumentoExportacionRepository documentoExportacionRepository;
    private final EventoLogisticoRepository eventoLogisticoRepository;

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
            Cosecha cosecha1 = crearCosecha(lote2, 850.5, "kg", "PRIMERA", LocalDate.now().minusDays(3));
            Cosecha cosecha2 = crearCosecha(lote3, 1200.0, "kg", "PREMIUM", LocalDate.now().minusDays(1));

            // 8. Crear Recepciones en Planta (Módulo Empaque)
            RecepcionPlanta recepcion1 = crearRecepcion(lote2, "REC-2025-001", 850.5, LocalDate.now().minusDays(2));
            RecepcionPlanta recepcion2 = crearRecepcion(lote3, "REC-2025-002", 1200.0, LocalDate.now());

            // 9. Crear Clasificaciones
            Clasificacion clasificacion1 = crearClasificacion(recepcion1, "CLAS-2025-001", "PRIMERA", 750.0, "14");
            Clasificacion clasificacion2 = crearClasificacion(recepcion1, "CLAS-2025-002", "SEGUNDA", 85.5, "12");
            Clasificacion clasificacion3 = crearClasificacion(recepcion2, "CLAS-2025-003", "PREMIUM", 1100.0, "16");

            // 10. Crear Controles de Calidad
            crearControlCalidad(clasificacion1, null, "CC-2025-001", "VISUAL", "FIRMEZA", "8.5 lb", "7-9 lb", true);
            crearControlCalidad(clasificacion3, null, "CC-2025-002", "LABORATORIO", "BRIX", "14.2", "12-16", true);

            // 11. Crear Pallets
            Pallet pallet1 = crearPallet("PLT-2025-001", LocalDate.now().minusDays(1), "EPAL", 80, 800.0, "Mango", "PRIMERA", "Estados Unidos");
            Pallet pallet2 = crearPallet("PLT-2025-002", LocalDate.now(), "AMERICANO", 100, 1050.0, "Banano", "PREMIUM", "España");
            Pallet pallet3 = crearPallet("PLT-2025-003", LocalDate.now(), "EPAL", 85, 850.0, "Mango", "PRIMERA", "Estados Unidos");

            // 12. Crear Control de Calidad sobre Pallets
            crearControlCalidad(null, pallet1, "CC-2025-003", "EMPAQUE", "PESO_PALLET", "802 kg", "800 kg", true);

            // 13. Crear Envíos (Módulo Logística)
            User admin = userRepository.findByEmail("admin@frutas.com").orElseThrow();
            Envio envio1 = crearEnvio(admin, "ENV-2025-001", "Estados Unidos", "Miami", "MARITIMO", LocalDate.now().plusDays(5));
            Envio envio2 = crearEnvio(admin, "ENV-2025-002", "España", "Barcelona", "MARITIMO", LocalDate.now().plusDays(7));

            // 14. Asignar Pallets a Envíos
            asignarPalletAEnvio(pallet1, envio1);
            asignarPalletAEnvio(pallet3, envio1);
            asignarPalletAEnvio(pallet2, envio2);

            // 15. Crear Documentos de Exportación
            crearDocumento(envio1, "PACKING_LIST", "PL-2025-001", LocalDate.now().minusDays(1), "Exportadora Frutas Colombia");
            crearDocumento(envio1, "CERTIFICADO_FITOSANITARIO", "CFE-2025-001", LocalDate.now().minusDays(1), "ICA");
            crearDocumento(envio1, "FACTURA_COMERCIAL", "FC-2025-001", LocalDate.now().minusDays(1), "Frutas Tropicales S.A.S");
            crearDocumento(envio2, "PACKING_LIST", "PL-2025-002", LocalDate.now(), "Exportadora Frutas Colombia");
            crearDocumento(envio2, "CERTIFICADO_FITOSANITARIO", "CFE-2025-002", LocalDate.now(), "ICA");

            // 16. Crear Eventos Logísticos (Tracking)
            crearEvento(envio1, "CARGA", LocalDate.now().minusDays(1), LocalTime.of(14, 30), "Planta Empaque Frutícola", "Pereira", "Colombia");
            crearEvento(envio1, "SALIDA_PLANTA", LocalDate.now().minusDays(1), LocalTime.of(16, 0), "Planta Empaque Frutícola", "Pereira", "Colombia");
            crearEvento(envio1, "ARRIBO_PUERTO", LocalDate.now(), LocalTime.of(8, 15), "Puerto de Buenaventura", "Buenaventura", "Colombia");
            crearEvento(envio2, "CARGA", LocalDate.now(), LocalTime.of(10, 0), "Planta Empaque Frutícola", "Pereira", "Colombia");

            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("📊 Resumen:");
            log.info("   - Empresas: {}", empresaRepository.count());
            log.info("   - Usuarios: {}", userRepository.count());
            log.info("   - Fincas: {}", fincaRepository.count());
            log.info("   - Certificaciones: {}", certificacionRepository.count());
            log.info("   - Lotes: {}", loteRepository.count());
            log.info("   - Actividades: {}", actividadRepository.count());
            log.info("   - Cosechas: {}", cosechaRepository.count());
            log.info("   - Recepciones: {}", recepcionPlantaRepository.count());
            log.info("   - Clasificaciones: {}", clasificacionRepository.count());
            log.info("   - Controles Calidad: {}", controlCalidadRepository.count());
            log.info("   - Pallets: {}", palletRepository.count());
            log.info("   - Envíos: {}", envioRepository.count());
            log.info("   - Documentos Exportación: {}", documentoExportacionRepository.count());
            log.info("   - Eventos Logísticos: {}", eventoLogisticoRepository.count());

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

    private Cosecha crearCosecha(Lote lote, double cantidad, String unidad, String calidad, LocalDate fecha) {
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
        cosecha = cosechaRepository.save(cosecha);
        log.debug("✓ Cosecha creada: {} {} en {}", cantidad, unidad, lote.getNombre());
        return cosecha;
    }

    // ======================== MÓDULO EMPAQUE ========================

    private RecepcionPlanta crearRecepcion(Lote lote, String codigo, double cantidad, LocalDate fecha) {
        RecepcionPlanta recepcion = RecepcionPlanta.builder()
                .lote(lote)
                .codigoRecepcion(codigo)
                .fechaRecepcion(fecha)
                .horaRecepcion("08:30")
                .cantidadRecibida(cantidad)
                .unidadMedida("kg")
                .temperaturaFruta(18.5)
                .estadoInicial("MADURA")
                .responsableRecepcion("Jefe de Planta")
                .vehiculoTransporte("ABC-123")
                .conductor("Pedro Martínez")
                .estadoRecepcion("RECIBIDA")
                .activo(true)
                .build();
        recepcion = recepcionPlantaRepository.save(recepcion);
        log.debug("✓ Recepción creada: {} - {} kg", codigo, cantidad);
        return recepcion;
    }

    private Clasificacion crearClasificacion(RecepcionPlanta recepcion, String codigo, String calidad,
                                            double cantidad, String calibre) {
        Clasificacion clasificacion = Clasificacion.builder()
                .recepcion(recepcion)
                .codigoClasificacion(codigo)
                .fechaClasificacion(LocalDate.now())
                .calidad(calidad)
                .cantidadClasificada(cantidad)
                .unidadMedida("kg")
                .calibre(calibre)
                .porcentajeMerma(calidad.equals("SEGUNDA") ? 10.0 : 2.0)
                .cantidadMerma(calidad.equals("SEGUNDA") ? cantidad * 0.1 : cantidad * 0.02)
                .motivoMerma(calidad.equals("SEGUNDA") ? "GOLPES LEVES" : "DESCARTE NORMAL")
                .responsableClasificacion("Clasificador Senior")
                .activo(true)
                .build();
        clasificacion = clasificacionRepository.save(clasificacion);
        log.debug("✓ Clasificación creada: {} - {} {}", codigo, calidad, calibre);
        return clasificacion;
    }

    private ControlCalidad crearControlCalidad(Clasificacion clasificacion, Pallet pallet, String codigo,
                                              String tipoControl, String parametro, String valorMedido,
                                              String valorEsperado, boolean cumple) {
        ControlCalidad control = ControlCalidad.builder()
                .clasificacion(clasificacion)
                .pallet(pallet)
                .codigoControl(codigo)
                .fechaControl(LocalDate.now())
                .tipoControl(tipoControl)
                .parametroEvaluado(parametro)
                .valorMedido(valorMedido)
                .valorEsperado(valorEsperado)
                .cumpleEspecificacion(cumple)
                .resultado(cumple ? "APROBADO" : "RECHAZADO")
                .responsableControl("Técnico de Calidad")
                .laboratorio(tipoControl.equals("LABORATORIO") ? "LabControl S.A.S" : null)
                .activo(true)
                .build();
        control = controlCalidadRepository.save(control);
        log.debug("✓ Control de calidad creado: {} - {}", codigo, tipoControl);
        return control;
    }

    private Pallet crearPallet(String codigo, LocalDate fecha, String tipo, int numeroCajas,
                              double pesoNeto, String tipoFruta, String calidad, String destino) {
        Pallet pallet = Pallet.builder()
                .codigoPallet(codigo)
                .fechaPaletizado(fecha)
                .tipoPallet(tipo)
                .numeroCajas(numeroCajas)
                .pesoNetoTotal(pesoNeto)
                .pesoBrutoTotal(pesoNeto + (numeroCajas * 0.5)) // 0.5 kg por caja
                .alturaPallet(120.0)
                .tipoFruta(tipoFruta)
                .calidad(calidad)
                .destino(destino)
                .temperaturaAlmacenamiento(4.5)
                .responsablePaletizado("Operador de Empaque")
                .estadoPallet("ARMADO")
                .activo(true)
                .build();
        pallet = palletRepository.save(pallet);
        log.debug("✓ Pallet creado: {} - {} cajas", codigo, numeroCajas);
        return pallet;
    }

    // ======================== MÓDULO LOGÍSTICA ========================

    private Envio crearEnvio(User usuario, String codigo, String paisDestino, String puertoDestino,
                            String tipoTransporte, LocalDate fechaSalida) {
        Envio envio = new Envio();
        envio.setCodigoEnvio(codigo);
        envio.setUsuario(usuario);
        envio.setFechaCreacion(LocalDate.now());
        envio.setFechaSalidaEstimada(fechaSalida);
        envio.setExportador("Frutas Tropicales S.A.S");
        envio.setPaisDestino(paisDestino);
        envio.setPuertoDestino(puertoDestino);
        envio.setCiudadDestino(puertoDestino);
        envio.setTipoTransporte(tipoTransporte);
        envio.setCodigoContenedor("MSCU" + (int)(Math.random() * 1000000));
        envio.setTipoContenedor("Reefer 40ft");
        envio.setTemperaturaContenedor(4.5);
        envio.setTransportista("Maersk Line");
        envio.setNumeroBooking("BK-" + codigo);
        envio.setEstado("CREADO");
        envio.setClienteImportador("Fresh Import LLC");
        envio.setIncoterm("FOB");
        envio.setActivo(true);
        envio = envioRepository.save(envio);
        log.debug("✓ Envío creado: {} - Destino: {}", codigo, paisDestino);
        return envio;
    }

    private void asignarPalletAEnvio(Pallet pallet, Envio envio) {
        pallet.setEnvio(envio);
        pallet.setEstadoPallet("ASIGNADO_ENVIO");
        palletRepository.save(pallet);

        // Actualizar totales del envío
        envio.setNumeroPallets((envio.getNumeroPallets() != null ? envio.getNumeroPallets() : 0) + 1);
        envio.setNumeroCajas((envio.getNumeroCajas() != null ? envio.getNumeroCajas() : 0) + pallet.getNumeroCajas());
        envio.setPesoNetoTotal((envio.getPesoNetoTotal() != null ? envio.getPesoNetoTotal() : 0.0) + pallet.getPesoNetoTotal());
        envio.setPesoBrutoTotal((envio.getPesoBrutoTotal() != null ? envio.getPesoBrutoTotal() : 0.0) + pallet.getPesoBrutoTotal());
        envioRepository.save(envio);

        log.debug("✓ Pallet {} asignado a envío {}", pallet.getCodigoPallet(), envio.getCodigoEnvio());
    }

    private DocumentoExportacion crearDocumento(Envio envio, String tipo, String numero,
                                               LocalDate fechaEmision, String emisor) {
        DocumentoExportacion documento = new DocumentoExportacion();
        documento.setEnvio(envio);
        documento.setTipoDocumento(tipo);
        documento.setNumeroDocumento(numero);
        documento.setFechaEmision(fechaEmision);
        documento.setFechaVencimiento(tipo.equals("CERTIFICADO_FITOSANITARIO") ?
                                     fechaEmision.plusDays(15) : null);
        documento.setEntidadEmisora(emisor);
        documento.setFuncionarioEmisor("Funcionario Autorizado");
        documento.setEstado("GENERADO");
        documento.setObligatorio(tipo.equals("CERTIFICADO_FITOSANITARIO") || tipo.equals("PACKING_LIST"));
        documento.setActivo(true);
        documento = documentoExportacionRepository.save(documento);
        log.debug("✓ Documento creado: {} - {}", tipo, numero);
        return documento;
    }

    private EventoLogistico crearEvento(Envio envio, String tipoEvento, LocalDate fecha, LocalTime hora,
                                       String ubicacion, String ciudad, String pais) {
        EventoLogistico evento = new EventoLogistico();
        evento.setEnvio(envio);
        evento.setCodigoEvento("EVT-" + envio.getCodigoEnvio() + "-" + tipoEvento);
        evento.setTipoEvento(tipoEvento);
        evento.setFechaEvento(fecha);
        evento.setHoraEvento(hora);
        evento.setUbicacion(ubicacion);
        evento.setCiudad(ciudad);
        evento.setPais(pais);
        evento.setResponsable("Coordinador Logística");
        evento.setOrganizacion("Frutas Tropicales S.A.S");
        evento.setTemperaturaRegistrada(4.5);
        evento.setIncidencia(false);
        evento.setActivo(true);
        evento = eventoLogisticoRepository.save(evento);
        log.debug("✓ Evento logístico creado: {} - {}", tipoEvento, ubicacion);
        return evento;
    }
}