package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.logistica.dto.AuditoriaEventoResponse;
import com.frutas.trazabilidad.module.logistica.entity.AuditoriaEvento;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.AuditoriaEventoMapper;
import com.frutas.trazabilidad.module.logistica.repository.AuditoriaEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditoriaEventoService.
 * Tests audit trail creation, blockchain chain integrity, and query operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditoriaEventoServiceTest {

    @Mock
    private AuditoriaEventoRepository auditoriaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditoriaEventoMapper auditoriaMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditoriaEventoService auditoriaEventoService;

    private User testUser;
    private Empresa testEmpresa;
    private AuditoriaEvento testEvento;
    private AuditoriaEventoResponse testResponse;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .activo(true)
                .build();

        testUser = User.builder()
                .id(1L)
                .email("admin@frutascolombia.com")
                .nombre("Juan")
                .apellido("Perez")
                .empresa(testEmpresa)
                .rol(TipoRol.ADMIN)
                .activo(true)
                .build();

        testEvento = new AuditoriaEvento();
        testEvento.setId(1L);
        testEvento.setUsuario(testUser);
        testEvento.setTipoEntidad("ENVIO");
        testEvento.setEntidadId(100L);
        testEvento.setCodigoEntidad("ENV-001");
        testEvento.setTipoOperacion("CREATE");
        testEvento.setDescripcionOperacion("Creación de envío");
        testEvento.setEmpresaId(1L);
        testEvento.setEmpresaNombre("Frutas Colombia S.A.S");
        testEvento.setModulo("LOGISTICA");
        testEvento.setNivelCriticidad("INFO");
        testEvento.setEnCadena(false);
        testEvento.setHashEvento("abc123");
        testEvento.setFechaEvento(LocalDateTime.now());

        testResponse = AuditoriaEventoResponse.builder()
                .id(1L)
                .tipoEntidad("ENVIO")
                .entidadId(100L)
                .tipoOperacion("CREATE")
                .descripcionOperacion("Creación de envío")
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@frutascolombia.com");
        when(userRepository.findByEmail("admin@frutascolombia.com")).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("Register Creation Tests")
    class RegisterCreationTests {

        @Test
        @DisplayName("Should register creation event with all fields")
        void registrarCreacion_shouldSaveEventWithCorrectFields() {
            // When
            auditoriaEventoService.registrarCreacion(
                    "ENVIO",
                    100L,
                    "ENV-001",
                    "Creación de envío de exportación",
                    testUser
            );

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getTipoEntidad()).isEqualTo("ENVIO");
            assertThat(saved.getEntidadId()).isEqualTo(100L);
            assertThat(saved.getCodigoEntidad()).isEqualTo("ENV-001");
            assertThat(saved.getTipoOperacion()).isEqualTo("CREATE");
            assertThat(saved.getDescripcionOperacion()).isEqualTo("Creación de envío de exportación");
            assertThat(saved.getUsuario()).isEqualTo(testUser);
            assertThat(saved.getEmpresaId()).isEqualTo(1L);
            assertThat(saved.getNivelCriticidad()).isEqualTo("INFO");
            assertThat(saved.getEnCadena()).isFalse();
        }

        @Test
        @DisplayName("Should set correct module based on entity type")
        void registrarCreacion_shouldSetCorrectModule() {
            // When - PRODUCCION entity
            auditoriaEventoService.registrarCreacion("FINCA", 1L, "FIN-001", "Test", testUser);

            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getModulo()).isEqualTo("PRODUCCION");
        }

        @Test
        @DisplayName("Should generate SHA-256 hash for event")
        void registrarCreacion_shouldGenerateHash() {
            // When
            auditoriaEventoService.registrarCreacion("ENVIO", 100L, "ENV-001", "Test", testUser);

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getHashEvento()).isNotNull();
            assertThat(saved.getHashEvento()).hasSize(64); // SHA-256 = 64 hex chars
        }
    }

    @Nested
    @DisplayName("Register Update Tests")
    class RegisterUpdateTests {

        @Test
        @DisplayName("Should register update event with previous and new data")
        void registrarActualizacion_shouldSaveWithDataChanges() {
            // When
            auditoriaEventoService.registrarActualizacion(
                    "ENVIO",
                    100L,
                    "ENV-001",
                    "Actualización de destino",
                    "{pais:'Colombia'}",
                    "{pais:'Estados Unidos'}",
                    testUser
            );

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getTipoOperacion()).isEqualTo("UPDATE");
            assertThat(saved.getDatosAnteriores()).isEqualTo("{pais:'Colombia'}");
            assertThat(saved.getDatosNuevos()).isEqualTo("{pais:'Estados Unidos'}");
        }
    }

    @Nested
    @DisplayName("Register Deletion Tests")
    class RegisterDeletionTests {

        @Test
        @DisplayName("Should register deletion with WARNING criticality")
        void registrarEliminacion_shouldSetWarningCriticality() {
            // When
            auditoriaEventoService.registrarEliminacion(
                    "ENVIO",
                    100L,
                    "ENV-001",
                    "Eliminación de envío",
                    testUser
            );

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getTipoOperacion()).isEqualTo("DELETE");
            assertThat(saved.getNivelCriticidad()).isEqualTo("WARNING");
        }
    }

    @Nested
    @DisplayName("Register Shipment Close Tests (Blockchain)")
    class RegisterShipmentCloseTests {

        @Test
        @DisplayName("Should register shipment close as critical event in blockchain")
        void registrarCierreEnvio_shouldSetCriticalAndInChain() {
            // Given
            Envio envio = new Envio();
            envio.setId(100L);
            envio.setCodigoEnvio("ENV-001");
            envio.setNumeroPallets(5);
            envio.setPesoNetoTotal(2500.0);
            envio.setHashCierre("hash123");
            envio.setFechaCierre(LocalDateTime.now());
            envio.setEstado("CERRADO");

            when(auditoriaRepository.findUltimoEventoCadena(1L)).thenReturn(Optional.empty());

            // When
            auditoriaEventoService.registrarCierreEnvio(envio, testUser);

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getTipoOperacion()).isEqualTo("CLOSE");
            assertThat(saved.getNivelCriticidad()).isEqualTo("CRITICAL");
            assertThat(saved.getEnCadena()).isTrue();
            assertThat(saved.getDescripcionOperacion()).contains("ENV-001");
            assertThat(saved.getDescripcionOperacion()).contains("5 pallets");
        }

        @Test
        @DisplayName("Should chain to previous event hash")
        void registrarCierreEnvio_shouldChainToPreviousEvent() {
            // Given
            Envio envio = new Envio();
            envio.setId(100L);
            envio.setCodigoEnvio("ENV-001");
            envio.setNumeroPallets(5);
            envio.setPesoNetoTotal(2500.0);
            envio.setHashCierre("hash123");
            envio.setFechaCierre(LocalDateTime.now());
            envio.setEstado("CERRADO");

            AuditoriaEvento previousEvent = new AuditoriaEvento();
            previousEvent.setHashEvento("previousHash123456");

            when(auditoriaRepository.findUltimoEventoCadena(1L)).thenReturn(Optional.of(previousEvent));

            // When
            auditoriaEventoService.registrarCierreEnvio(envio, testUser);

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            AuditoriaEvento saved = captor.getValue();
            assertThat(saved.getHashAnterior()).isEqualTo("previousHash123456");
        }

        @Test
        @DisplayName("Should use '0' as previous hash for first blockchain event")
        void registrarCierreEnvio_firstEvent_shouldUseZeroAsPreviousHash() {
            // Given
            Envio envio = new Envio();
            envio.setId(100L);
            envio.setCodigoEnvio("ENV-001");
            envio.setNumeroPallets(5);
            envio.setPesoNetoTotal(2500.0);
            envio.setHashCierre("hash123");
            envio.setFechaCierre(LocalDateTime.now());
            envio.setEstado("CERRADO");

            when(auditoriaRepository.findUltimoEventoCadena(1L)).thenReturn(Optional.empty());

            // When
            auditoriaEventoService.registrarCierreEnvio(envio, testUser);

            // Then
            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());

            assertThat(captor.getValue().getHashAnterior()).isEqualTo("0");
        }
    }

    @Nested
    @DisplayName("List Audit Events Tests")
    class ListAuditEventsTests {

        @Test
        @DisplayName("Should list events by empresa")
        void listarPorEmpresa_shouldReturnEventsForUserCompany() {
            // Given
            mockSecurityContext();
            when(auditoriaRepository.findByEmpresaIdOrderByFechaDesc(1L))
                    .thenReturn(Arrays.asList(testEvento));
            when(auditoriaMapper.toResponse(any(AuditoriaEvento.class))).thenReturn(testResponse);

            // When
            List<AuditoriaEventoResponse> result = auditoriaEventoService.listarPorEmpresa();

            // Then
            assertThat(result).hasSize(1);
            verify(auditoriaRepository).findByEmpresaIdOrderByFechaDesc(1L);
        }

        @Test
        @DisplayName("Should list events by entity type and ID")
        void listarPorEntidad_shouldReturnFilteredEvents() {
            // Given
            when(auditoriaRepository.findByTipoEntidadAndEntidadId("ENVIO", 100L))
                    .thenReturn(Arrays.asList(testEvento));
            when(auditoriaMapper.toResponse(any(AuditoriaEvento.class))).thenReturn(testResponse);

            // When
            List<AuditoriaEventoResponse> result = auditoriaEventoService.listarPorEntidad("ENVIO", 100L);

            // Then
            assertThat(result).hasSize(1);
            verify(auditoriaRepository).findByTipoEntidadAndEntidadId("ENVIO", 100L);
        }
    }

    @Nested
    @DisplayName("Blockchain Chain Integrity Tests")
    class BlockchainIntegrityTests {

        @Test
        @DisplayName("Should list blockchain chain events")
        void listarCadenaBlockchain_shouldReturnChainedEvents() {
            // Given
            mockSecurityContext();
            testEvento.setEnCadena(true);
            when(auditoriaRepository.findCadenaBlockchainByEmpresaId(1L))
                    .thenReturn(Arrays.asList(testEvento));
            when(auditoriaMapper.toResponse(any(AuditoriaEvento.class))).thenReturn(testResponse);

            // When
            List<AuditoriaEventoResponse> result = auditoriaEventoService.listarCadenaBlockchain();

            // Then
            assertThat(result).hasSize(1);
            verify(auditoriaRepository).findCadenaBlockchainByEmpresaId(1L);
        }

        @Test
        @DisplayName("Should return true for valid chain integrity")
        void validarIntegridadCadena_withValidChain_shouldReturnTrue() {
            // Given
            mockSecurityContext();

            // Create properly chained events
            AuditoriaEvento event1 = createChainedEvent(1L, "0", testUser);
            AuditoriaEvento event2 = createChainedEvent(2L, event1.getHashEvento(), testUser);

            when(auditoriaRepository.findCadenaBlockchainByEmpresaId(1L))
                    .thenReturn(Arrays.asList(event1, event2));

            // When
            boolean result = auditoriaEventoService.validarIntegridadCadena();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when chain is broken")
        void validarIntegridadCadena_withBrokenChain_shouldReturnFalse() {
            // Given
            mockSecurityContext();

            AuditoriaEvento event1 = createChainedEvent(1L, "0", testUser);
            AuditoriaEvento event2 = createChainedEvent(2L, "wrong_hash", testUser);

            when(auditoriaRepository.findCadenaBlockchainByEmpresaId(1L))
                    .thenReturn(Arrays.asList(event1, event2));

            // When
            boolean result = auditoriaEventoService.validarIntegridadCadena();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true for empty chain")
        void validarIntegridadCadena_withEmptyChain_shouldReturnTrue() {
            // Given
            mockSecurityContext();
            when(auditoriaRepository.findCadenaBlockchainByEmpresaId(1L))
                    .thenReturn(List.of());

            // When
            boolean result = auditoriaEventoService.validarIntegridadCadena();

            // Then
            assertThat(result).isTrue();
        }

        private AuditoriaEvento createChainedEvent(Long id, String hashAnterior, User usuario) {
            AuditoriaEvento evento = new AuditoriaEvento();
            evento.setId(id);
            evento.setUsuario(usuario);
            evento.setTipoEntidad("ENVIO");
            evento.setEntidadId(id);
            evento.setCodigoEntidad("ENV-" + id);
            evento.setTipoOperacion("CLOSE");
            evento.setDescripcionOperacion("Cierre de envío " + id);
            evento.setEmpresaId(1L);
            evento.setEmpresaNombre("Frutas Colombia S.A.S");
            evento.setModulo("LOGISTICA");
            evento.setNivelCriticidad("CRITICAL");
            evento.setEnCadena(true);
            evento.setHashAnterior(hashAnterior);
            evento.setFechaEvento(LocalDateTime.now());

            // Calculate hash (simplified for testing)
            String hash = calculateSimpleHash(evento);
            evento.setHashEvento(hash);

            return evento;
        }

        private String calculateSimpleHash(AuditoriaEvento evento) {
            try {
                String datos = String.format(
                        "%s|%s|%d|%s|%s|%s|%d|%s",
                        evento.getHashAnterior() != null ? evento.getHashAnterior() : "0",
                        evento.getUsuario().getId(),
                        evento.getEntidadId(),
                        evento.getTipoEntidad(),
                        evento.getTipoOperacion(),
                        evento.getDescripcionOperacion(),
                        evento.getEmpresaId(),
                        evento.getFechaEvento()
                );

                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(datos.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("Module Detection Tests")
    class ModuleDetectionTests {

        @Test
        @DisplayName("Should detect PRODUCCION module for farm entities")
        void registrarCreacion_withFinca_shouldSetProduccionModule() {
            auditoriaEventoService.registrarCreacion("FINCA", 1L, "FIN-001", "Test", testUser);

            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getModulo()).isEqualTo("PRODUCCION");
        }

        @Test
        @DisplayName("Should detect EMPAQUE module for packaging entities")
        void registrarCreacion_withPallet_shouldSetEmpaqueModule() {
            auditoriaEventoService.registrarCreacion("PALLET", 1L, "PAL-001", "Test", testUser);

            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getModulo()).isEqualTo("EMPAQUE");
        }

        @Test
        @DisplayName("Should detect LOGISTICA module for shipment entities")
        void registrarCreacion_withEnvio_shouldSetLogisticaModule() {
            auditoriaEventoService.registrarCreacion("ENVIO", 1L, "ENV-001", "Test", testUser);

            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getModulo()).isEqualTo("LOGISTICA");
        }

        @Test
        @DisplayName("Should default to SISTEMA module for unknown entities")
        void registrarCreacion_withUnknownEntity_shouldSetSistemaModule() {
            auditoriaEventoService.registrarCreacion("DESCONOCIDO", 1L, "DES-001", "Test", testUser);

            ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
            verify(auditoriaRepository).save(captor.capture());
            assertThat(captor.getValue().getModulo()).isEqualTo("SISTEMA");
        }
    }
}
