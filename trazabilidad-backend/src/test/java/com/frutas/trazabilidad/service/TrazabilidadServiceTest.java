package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.TrazabilidadCompletaDTO;
import com.frutas.trazabilidad.dto.TrazabilidadPublicaDTO;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.empaque.entity.*;
import com.frutas.trazabilidad.module.empaque.repository.ControlCalidadRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaPalletRepository;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import com.frutas.trazabilidad.module.produccion.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TrazabilidadService.
 * Tests the complete traceability chain construction from farm to final destination.
 */
@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private ControlCalidadRepository controlCalidadRepository;

    @Mock
    private EtiquetaPalletRepository etiquetaPalletRepository;

    @InjectMocks
    private TrazabilidadService trazabilidadService;

    private Empresa testEmpresa;
    private Finca testFinca;
    private Lote testLote;
    private Cosecha testCosecha;
    private RecepcionPlanta testRecepcion;
    private Clasificacion testClasificacion;
    private Etiqueta testEtiqueta;
    private Pallet testPallet;
    private Envio testEnvio;
    private EtiquetaPallet testEtiquetaPallet;

    @BeforeEach
    void setUp() {
        // Setup empresa
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .activo(true)
                .build();

        // Setup finca with certificaciones
        testFinca = Finca.builder()
                .id(1L)
                .codigoFinca("FIN-001")
                .nombre("Finca La Esperanza")
                .municipio("Villeta")
                .departamento("Cundinamarca")
                .pais("Colombia")
                .areaHectareas(50.0)
                .empresa(testEmpresa)
                .activo(true)
                .build();

        Certificacion cert = Certificacion.builder()
                .id(1L)
                .tipoCertificacion("GLOBAL_GAP")
                .entidadEmisora("SGS")
                .estado("VIGENTE")
                .activo(true)
                .finca(testFinca)
                .build();
        testFinca.setCertificaciones(new ArrayList<>(List.of(cert)));

        // Setup lote with actividades
        testLote = Lote.builder()
                .id(1L)
                .codigoLote("LOT-001")
                .nombre("Lote Norte")
                .tipoFruta("Mango")
                .variedad("Tommy Atkins")
                .areaHectareas(10.0)
                .fechaSiembra(LocalDate.of(2022, 1, 15))
                .estadoLote("PRODUCCION")
                .finca(testFinca)
                .activo(true)
                .build();

        ActividadAgronomica actividad = ActividadAgronomica.builder()
                .id(1L)
                .tipoActividad("FERTILIZACION")
                .fechaActividad(LocalDate.of(2024, 3, 15))
                .responsable("Juan Perez")
                .observaciones("Fertilizacion programada")
                .activo(true)
                .lote(testLote)
                .build();
        testLote.setActividades(new ArrayList<>(List.of(actividad)));

        // Setup cosecha
        testCosecha = Cosecha.builder()
                .id(1L)
                .fechaCosecha(LocalDate.of(2024, 6, 1))
                .cantidadCosechada(500.0)
                .unidadMedida("KG")
                .estadoFruta("MADURO")
                .responsableCosecha("Pedro Garcia")
                .lote(testLote)
                .activo(true)
                .build();
        testLote.setCosechas(new ArrayList<>(List.of(testCosecha)));

        // Setup recepcion
        testRecepcion = RecepcionPlanta.builder()
                .id(1L)
                .codigoRecepcion("REC-001")
                .fechaRecepcion(LocalDate.of(2024, 6, 2))
                .cantidadRecibida(480.0)
                .estadoRecepcion("PROCESADO")
                .responsableRecepcion("Maria Lopez")
                .lote(testLote)
                .activo(true)
                .build();

        // Setup clasificacion
        testClasificacion = Clasificacion.builder()
                .id(1L)
                .codigoClasificacion("CLA-001")
                .fechaClasificacion(LocalDate.of(2024, 6, 2))
                .calidad("PREMIUM")
                .calibre("GRANDE")
                .cantidadClasificada(450.0)
                .responsableClasificacion("Carlos Rodriguez")
                .recepcion(testRecepcion)
                .activo(true)
                .build();

        // Setup etiqueta
        testEtiqueta = Etiqueta.builder()
                .id(1L)
                .codigoEtiqueta("ETQ-001")
                .codigoQr("QR-MANGO-2024-001")
                .tipoEtiqueta("CAJA")
                .estadoEtiqueta("DISPONIBLE")
                .urlQr("https://trazabilidad.com/qr/QR-MANGO-2024-001")
                .clasificacion(testClasificacion)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .activo(true)
                .build();

        // Setup pallet
        testPallet = Pallet.builder()
                .id(1L)
                .codigoPallet("PAL-001")
                .tipoPallet("ESTANDAR")
                .numeroCajas(80)
                .pesoNetoTotal(400.0)
                .pesoBrutoTotal(420.0)
                .estadoPallet("ARMADO")
                .activo(true)
                .build();

        // Setup etiqueta-pallet relationship
        testEtiquetaPallet = EtiquetaPallet.builder()
                .id(1L)
                .etiqueta(testEtiqueta)
                .pallet(testPallet)
                .posicionEnPallet(1)
                .activo(true)
                .build();

        // Setup envio with eventos
        testEnvio = new Envio();
        testEnvio.setId(1L);
        testEnvio.setCodigoEnvio("ENV-001");
        testEnvio.setEstado("EN_TRANSITO");
        testEnvio.setPaisDestino("Estados Unidos");
        testEnvio.setPuertoDestino("Miami");
        testEnvio.setFechaSalidaEstimada(LocalDate.of(2024, 6, 10));
        testEnvio.setTipoTransporte("MARITIMO");
        testEnvio.setActivo(true);

        EventoLogistico evento = new EventoLogistico();
        evento.setId(1L);
        evento.setTipoEvento("SALIDA_PUERTO");
        evento.setFechaEvento(LocalDate.of(2024, 6, 10));
        evento.setUbicacion("Puerto de Cartagena");
        evento.setCiudad("Cartagena");
        evento.setPais("Colombia");
        evento.setActivo(true);
        evento.setEnvio(testEnvio);
        testEnvio.setEventos(new ArrayList<>(List.of(evento)));

        testPallet.setEnvio(testEnvio);
    }

    @Nested
    @DisplayName("Public Traceability Tests")
    class PublicTraceabilityTests {

        @Test
        @DisplayName("Should return complete public traceability for valid QR code")
        void obtenerTrazabilidadPublica_withValidQr_shouldReturnCompleteTraceability() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.of(testEtiquetaPallet));

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCodigoEtiqueta()).isEqualTo("ETQ-001");
            assertThat(result.getTipoProducto()).isEqualTo("Mango");
            assertThat(result.getCalidad()).isEqualTo("PREMIUM");
            assertThat(result.getVariedad()).isEqualTo("Tommy Atkins");
        }

        @Test
        @DisplayName("Should include origin information")
        void obtenerTrazabilidadPublica_shouldIncludeOrigenInfo() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getOrigen()).isNotNull();
            assertThat(result.getOrigen().getFinca()).isEqualTo("Finca La Esperanza");
            assertThat(result.getOrigen().getMunicipio()).isEqualTo("Villeta");
            assertThat(result.getOrigen().getDepartamento()).isEqualTo("Cundinamarca");
            assertThat(result.getOrigen().getPais()).isEqualTo("Colombia");
            assertThat(result.getOrigen().getCodigoLote()).isEqualTo("LOT-001");
        }

        @Test
        @DisplayName("Should include production information")
        void obtenerTrazabilidadPublica_shouldIncludeProduccionInfo() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getProduccion()).isNotNull();
            assertThat(result.getProduccion().getFechaCosecha()).isEqualTo(LocalDate.of(2024, 6, 1));
            assertThat(result.getProduccion().getEstadoFruta()).isEqualTo("MADURO");
            assertThat(result.getProduccion().getActividadesRegistradas()).isEqualTo(1);
            assertThat(result.getProduccion().getTiposActividades()).contains("FERTILIZACION");
        }

        @Test
        @DisplayName("Should include packaging information")
        void obtenerTrazabilidadPublica_shouldIncludeEmpaqueInfo() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getEmpaque()).isNotNull();
            assertThat(result.getEmpaque().getFechaRecepcion()).isNotNull();
            assertThat(result.getEmpaque().getFechaClasificacion()).isNotNull();
            assertThat(result.getEmpaque().getCalidadClasificada()).isEqualTo("PREMIUM");
            assertThat(result.getEmpaque().getCalibre()).isEqualTo("GRANDE");
        }

        @Test
        @DisplayName("Should include logistics information when pallet is assigned to shipment")
        void obtenerTrazabilidadPublica_withShipment_shouldIncludeLogisticaInfo() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.of(testEtiquetaPallet));

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getLogistica()).isNotNull();
            assertThat(result.getLogistica().getEstadoEnvio()).isEqualTo("EN_TRANSITO");
            assertThat(result.getLogistica().getPaisDestino()).isEqualTo("Estados Unidos");
            assertThat(result.getLogistica().getPuertoDestino()).isEqualTo("Miami");
            assertThat(result.getLogistica().getTipoTransporte()).isEqualTo("MARITIMO");
            assertThat(result.getLogistica().getEventos()).hasSize(1);
        }

        @Test
        @DisplayName("Should return null logistics when pallet not assigned")
        void obtenerTrazabilidadPublica_withoutPallet_shouldReturnNullLogistica() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getLogistica()).isNull();
        }

        @Test
        @DisplayName("Should include certifications")
        void obtenerTrazabilidadPublica_shouldIncludeCertificaciones() {
            // Given
            when(etiquetaRepository.findByCodigoQr("QR-MANGO-2024-001")).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadPublicaDTO result = trazabilidadService.obtenerTrazabilidadPublica("QR-MANGO-2024-001");

            // Then
            assertThat(result.getCertificaciones()).isNotNull();
            assertThat(result.getCertificaciones()).hasSize(1);
            assertThat(result.getCertificaciones().get(0).getTipoCertificacion()).isEqualTo("GLOBAL_GAP");
            assertThat(result.getCertificaciones().get(0).getEntidadEmisora()).isEqualTo("SGS");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid QR code")
        void obtenerTrazabilidadPublica_withInvalidQr_shouldThrowResourceNotFoundException() {
            // Given
            when(etiquetaRepository.findByCodigoQr("INVALID-QR")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> trazabilidadService.obtenerTrazabilidadPublica("INVALID-QR"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontró información de trazabilidad para el código QR proporcionado");
        }
    }

    @Nested
    @DisplayName("Complete Internal Traceability Tests")
    class CompleteTraceabilityTests {

        @Test
        @DisplayName("Should return complete internal traceability for authorized user")
        void obtenerTrazabilidadCompleta_withAuthorizedUser_shouldReturnCompleteData() {
            // Given
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.of(testEtiquetaPallet));

            // When
            TrazabilidadCompletaDTO result = trazabilidadService.obtenerTrazabilidadCompleta(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEtiquetaId()).isEqualTo(1L);
            assertThat(result.getCodigoEtiqueta()).isEqualTo("ETQ-001");
            assertThat(result.getCodigoQr()).isEqualTo("QR-MANGO-2024-001");
        }

        @Test
        @DisplayName("Should include complete origin info with contact details")
        void obtenerTrazabilidadCompleta_shouldIncludeFullOrigenInfo() {
            // Given
            testFinca.setEncargado("Juan Administrador");
            testFinca.setTelefono("3001234567");
            testFinca.setEmail("finca@ejemplo.com");

            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadCompletaDTO result = trazabilidadService.obtenerTrazabilidadCompleta(1L, 1L);

            // Then
            assertThat(result.getOrigen()).isNotNull();
            assertThat(result.getOrigen().getFincaId()).isEqualTo(1L);
            assertThat(result.getOrigen().getFincaCodigo()).isEqualTo("FIN-001");
            assertThat(result.getOrigen().getContactoResponsable()).isEqualTo("Juan Administrador");
            assertThat(result.getOrigen().getTelefonoContacto()).isEqualTo("3001234567");
            assertThat(result.getOrigen().getEmailContacto()).isEqualTo("finca@ejemplo.com");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when etiqueta belongs to different enterprise")
        void obtenerTrazabilidadCompleta_withUnauthorizedUser_shouldThrowIllegalArgumentException() {
            // Given
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));

            // When/Then - empresaId=2 doesn't match testEmpresa.id=1
            assertThatThrownBy(() -> trazabilidadService.obtenerTrazabilidadCompleta(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No tiene permisos para consultar esta etiqueta");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent etiqueta")
        void obtenerTrazabilidadCompleta_withInvalidEtiquetaId_shouldThrowResourceNotFoundException() {
            // Given
            when(etiquetaRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> trazabilidadService.obtenerTrazabilidadCompleta(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Etiqueta no encontrada con ID: 999");
        }

        @Test
        @DisplayName("Should include complete agronomic activities")
        void obtenerTrazabilidadCompleta_shouldIncludeActividadesAgronomicas() {
            // Given
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadCompletaDTO result = trazabilidadService.obtenerTrazabilidadCompleta(1L, 1L);

            // Then
            assertThat(result.getProduccion()).isNotNull();
            assertThat(result.getProduccion().getActividades()).hasSize(1);
            assertThat(result.getProduccion().getActividades().get(0).getTipoActividad()).isEqualTo("FERTILIZACION");
            assertThat(result.getProduccion().getActividades().get(0).getResponsable()).isEqualTo("Juan Perez");
        }

        @Test
        @DisplayName("Should include quality controls in packaging info")
        void obtenerTrazabilidadCompleta_shouldIncludeControlesCalidad() {
            // Given
            ControlCalidad control = ControlCalidad.builder()
                    .id(1L)
                    .fechaControl(LocalDate.of(2024, 6, 2))
                    .tipoControl("VISUAL")
                    .resultado("APROBADO")
                    .observaciones("Sin defectos visibles")
                    .laboratorio("Control Calidad Interno")
                    .activo(true)
                    .build();

            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of(control));
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadCompletaDTO result = trazabilidadService.obtenerTrazabilidadCompleta(1L, 1L);

            // Then
            assertThat(result.getEmpaque()).isNotNull();
            assertThat(result.getEmpaque().getControlesCalidad()).hasSize(1);
            assertThat(result.getEmpaque().getControlesCalidad().get(0).getResultado()).isEqualTo("APROBADO");
            assertThat(result.getEmpaque().getControlesCalidad().get(0).getTipoControl()).isEqualTo("VISUAL");
        }

        @Test
        @DisplayName("Should include audit information")
        void obtenerTrazabilidadCompleta_shouldIncludeAuditoriaInfo() {
            // Given
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(controlCalidadRepository.findByClasificacionIdAndActivoTrueOrderByFechaControlDesc(anyLong()))
                    .thenReturn(List.of());
            when(etiquetaPalletRepository.findByEtiquetaIdAndActivoTrue(anyLong()))
                    .thenReturn(Optional.empty());

            // When
            TrazabilidadCompletaDTO result = trazabilidadService.obtenerTrazabilidadCompleta(1L, 1L);

            // Then
            assertThat(result.getAuditoria()).isNotNull();
            assertThat(result.getAuditoria().getEmpresaId()).isEqualTo(1L);
            assertThat(result.getAuditoria().getEmpresaNombre()).isEqualTo("Frutas Colombia S.A.S");
        }
    }
}
