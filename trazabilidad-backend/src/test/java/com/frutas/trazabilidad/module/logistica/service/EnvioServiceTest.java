package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.module.logistica.dto.EnvioRequest;
import com.frutas.trazabilidad.module.logistica.dto.EnvioResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.mapper.EnvioMapper;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import com.frutas.trazabilidad.repository.UserRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnvioService.
 * Tests all shipment management business rules including validation, state changes, and closing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private PalletRepository palletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnvioMapper envioMapper;

    @Mock
    private AuditoriaEventoService auditoriaService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EnvioService envioService;

    private Empresa testEmpresa;
    private User testUser;
    private Envio testEnvio;
    private EnvioRequest envioRequest;
    private EnvioResponse envioResponse;
    private Pallet testPallet;

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
                .email("operador@frutascolombia.com")
                .nombre("Juan")
                .apellido("Perez")
                .empresa(testEmpresa)
                .rol(TipoRol.OPERADOR_LOGISTICA)
                .activo(true)
                .build();

        testEnvio = new Envio();
        testEnvio.setId(1L);
        testEnvio.setCodigoEnvio("ENV-2024-001");
        testEnvio.setEstado("PREPARANDO");
        testEnvio.setPaisDestino("Estados Unidos");
        testEnvio.setPuertoDestino("Miami");
        testEnvio.setCiudadDestino("Miami");
        testEnvio.setExportador("Frutas Colombia S.A.S");
        testEnvio.setTipoTransporte("MARITIMO");
        testEnvio.setFechaSalidaEstimada(LocalDate.now().plusDays(7));
        testEnvio.setUsuario(testUser);
        testEnvio.setPallets(new ArrayList<>());
        testEnvio.setEventos(new ArrayList<>());
        testEnvio.setDocumentos(new ArrayList<>());
        testEnvio.setActivo(true);

        testPallet = Pallet.builder()
                .id(1L)
                .codigoPallet("PAL-001")
                .estadoPallet("ARMADO")
                .pesoNetoTotal(400.0)
                .pesoBrutoTotal(420.0)
                .numeroCajas(80)
                .activo(true)
                .build();

        envioRequest = new EnvioRequest();
        envioRequest.setCodigoEnvio("ENV-2024-002");
        envioRequest.setPaisDestino("Canada");
        envioRequest.setPuertoDestino("Vancouver");
        envioRequest.setTipoTransporte("MARITIMO");

        envioResponse = EnvioResponse.builder()
                .id(1L)
                .codigoEnvio("ENV-2024-001")
                .estado("PREPARANDO")
                .paisDestino("Estados Unidos")
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operador@frutascolombia.com");
        when(userRepository.findByEmail("operador@frutascolombia.com")).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("Create Shipment Tests")
    class CreateShipmentTests {

        @Test
        @DisplayName("Should create shipment with valid data")
        void crear_withValidData_shouldCreateEnvio() {
            // Given
            mockSecurityContext();
            when(envioRepository.existsByCodigoEnvio(envioRequest.getCodigoEnvio())).thenReturn(false);
            when(envioMapper.toEntity(eq(envioRequest), eq(testUser))).thenReturn(testEnvio);
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            EnvioResponse result = envioService.crear(envioRequest);

            // Then
            assertThat(result).isNotNull();
            verify(envioRepository).save(any(Envio.class));
            verify(auditoriaService).registrarCreacion(
                    eq("ENVIO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when codigo already exists")
        void crear_withDuplicateCodigo_shouldThrowIllegalArgumentException() {
            // Given
            mockSecurityContext();
            when(envioRepository.existsByCodigoEnvio(envioRequest.getCodigoEnvio())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> envioService.crear(envioRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ya existe un envío con el código");

            verify(envioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should assign pallets when provided in request")
        void crear_withPalletIds_shouldAssignPallets() {
            // Given
            mockSecurityContext();
            envioRequest.setPalletsIds(Arrays.asList(1L, 2L));

            Pallet pallet2 = Pallet.builder()
                    .id(2L)
                    .codigoPallet("PAL-002")
                    .estadoPallet("EN_CAMARA")
                    .pesoNetoTotal(350.0)
                    .pesoBrutoTotal(370.0)
                    .numeroCajas(70)
                    .activo(true)
                    .build();

            when(envioRepository.existsByCodigoEnvio(envioRequest.getCodigoEnvio())).thenReturn(false);
            when(envioMapper.toEntity(eq(envioRequest), eq(testUser))).thenReturn(testEnvio);
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(palletRepository.findById(2L)).thenReturn(Optional.of(pallet2));
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            envioService.crear(envioRequest);

            // Then
            verify(palletRepository, times(2)).save(any(Pallet.class));
        }

        @Test
        @DisplayName("Should reject pallet not in valid state")
        void crear_withInvalidPalletState_shouldThrowIllegalStateException() {
            // Given
            mockSecurityContext();
            testPallet.setEstadoPallet("DESPACHADO");
            envioRequest.setPalletsIds(Arrays.asList(1L));

            when(envioRepository.existsByCodigoEnvio(envioRequest.getCodigoEnvio())).thenReturn(false);
            when(envioMapper.toEntity(eq(envioRequest), eq(testUser))).thenReturn(testEnvio);
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));

            // When/Then
            assertThatThrownBy(() -> envioService.crear(envioRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no está disponible para asignación");
        }
    }

    @Nested
    @DisplayName("Update Shipment Tests")
    class UpdateShipmentTests {

        @Test
        @DisplayName("Should update shipment with valid data")
        void actualizar_withValidData_shouldUpdateEnvio() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            EnvioResponse result = envioService.actualizar(1L, envioRequest);

            // Then
            assertThat(result).isNotNull();
            verify(envioMapper).updateEntity(eq(testEnvio), eq(envioRequest));
            verify(auditoriaService).registrarActualizacion(
                    eq("ENVIO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw RuntimeException when shipment not found")
        void actualizar_withInvalidId_shouldThrowRuntimeException() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> envioService.actualizar(999L, envioRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Envío no encontrado con ID: 999");
        }

        @Test
        @DisplayName("Should throw RuntimeException when user has no permission")
        void actualizar_withNoPermission_shouldThrowRuntimeException() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> envioService.actualizar(1L, envioRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para modificar este envío");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when shipment is closed")
        void actualizar_whenClosed_shouldThrowIllegalStateException() {
            // Given
            mockSecurityContext();
            testEnvio.setEstado("CERRADO");
            testEnvio.setFechaCierre(LocalDateTime.now());
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> envioService.actualizar(1L, envioRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se puede modificar un envío cerrado");
        }
    }

    @Nested
    @DisplayName("Change State Tests")
    class ChangeStateTests {

        @Test
        @DisplayName("Should change state successfully")
        void cambiarEstado_shouldChangeState() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            envioService.cambiarEstado(1L, "EN_TRANSITO");

            // Then
            ArgumentCaptor<Envio> envioCaptor = ArgumentCaptor.forClass(Envio.class);
            verify(envioRepository).save(envioCaptor.capture());
            assertThat(envioCaptor.getValue().getEstado()).isEqualTo("EN_TRANSITO");
        }

        @Test
        @DisplayName("Should set fecha salida real when changing to EN_TRANSITO")
        void cambiarEstado_toEnTransito_shouldSetFechaSalidaReal() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            envioService.cambiarEstado(1L, "EN_TRANSITO");

            // Then
            ArgumentCaptor<Envio> envioCaptor = ArgumentCaptor.forClass(Envio.class);
            verify(envioRepository).save(envioCaptor.capture());
            assertThat(envioCaptor.getValue().getFechaSalidaReal()).isEqualTo(LocalDate.now());
        }
    }

    @Nested
    @DisplayName("Close Shipment Tests")
    class CloseShipmentTests {

        @Test
        @DisplayName("Should close shipment and generate hash")
        void cerrar_shouldCloseAndGenerateHash() {
            // Given
            mockSecurityContext();
            testEnvio.setPallets(Arrays.asList(testPallet));
            testEnvio.setNumeroPallets(1);
            testEnvio.setPesoNetoTotal(400.0);

            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            envioService.cerrar(1L);

            // Then
            ArgumentCaptor<Envio> envioCaptor = ArgumentCaptor.forClass(Envio.class);
            verify(envioRepository).save(envioCaptor.capture());
            Envio closedEnvio = envioCaptor.getValue();

            assertThat(closedEnvio.getEstado()).isEqualTo("CERRADO");
            assertThat(closedEnvio.getFechaCierre()).isNotNull();
            assertThat(closedEnvio.getUsuarioCierre()).isEqualTo(testUser);
            assertThat(closedEnvio.getHashCierre()).isNotNull();
            assertThat(closedEnvio.getHashCierre()).hasSize(64); // SHA-256 produces 64 hex chars

            verify(auditoriaService).registrarCierreEnvio(eq(testEnvio), eq(testUser));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when already closed")
        void cerrar_whenAlreadyClosed_shouldThrowIllegalStateException() {
            // Given
            mockSecurityContext();
            testEnvio.setEstado("CERRADO");
            testEnvio.setFechaCierre(LocalDateTime.now());

            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> envioService.cerrar(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El envío ya está cerrado");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no pallets assigned")
        void cerrar_withNoPallets_shouldThrowIllegalStateException() {
            // Given
            mockSecurityContext();
            testEnvio.setPallets(new ArrayList<>());

            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> envioService.cerrar(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El envío debe tener al menos un pallet asignado");
        }
    }

    @Nested
    @DisplayName("Delete Shipment Tests")
    class DeleteShipmentTests {

        @Test
        @DisplayName("Should soft delete shipment")
        void eliminar_shouldSoftDelete() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When
            envioService.eliminar(1L);

            // Then
            ArgumentCaptor<Envio> envioCaptor = ArgumentCaptor.forClass(Envio.class);
            verify(envioRepository).save(envioCaptor.capture());
            assertThat(envioCaptor.getValue().getActivo()).isFalse();

            verify(auditoriaService).registrarEliminacion(
                    eq("ENVIO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trying to delete closed shipment")
        void eliminar_whenClosed_shouldThrowIllegalStateException() {
            // Given
            mockSecurityContext();
            testEnvio.setEstado("CERRADO");
            testEnvio.setFechaCierre(LocalDateTime.now());

            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> envioService.eliminar(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se puede eliminar un envío cerrado");
        }
    }

    @Nested
    @DisplayName("Calculate Totals Tests")
    class CalculateTotalsTests {

        @Test
        @DisplayName("Should calculate totals from pallets")
        void crear_shouldCalculateTotalsFromPallets() {
            // Given
            mockSecurityContext();
            Pallet pallet1 = Pallet.builder()
                    .id(1L)
                    .estadoPallet("ARMADO")
                    .pesoNetoTotal(400.0)
                    .pesoBrutoTotal(420.0)
                    .numeroCajas(80)
                    .build();
            Pallet pallet2 = Pallet.builder()
                    .id(2L)
                    .estadoPallet("EN_CAMARA")
                    .pesoNetoTotal(350.0)
                    .pesoBrutoTotal(370.0)
                    .numeroCajas(70)
                    .build();

            testEnvio.setPallets(Arrays.asList(pallet1, pallet2));

            envioRequest.setPalletsIds(Arrays.asList(1L, 2L));

            when(envioRepository.existsByCodigoEnvio(envioRequest.getCodigoEnvio())).thenReturn(false);
            when(envioMapper.toEntity(eq(envioRequest), eq(testUser))).thenReturn(testEnvio);
            when(palletRepository.findById(1L)).thenReturn(Optional.of(pallet1));
            when(palletRepository.findById(2L)).thenReturn(Optional.of(pallet2));
            when(envioRepository.save(any(Envio.class))).thenReturn(testEnvio);
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            envioService.crear(envioRequest);

            // Then
            ArgumentCaptor<Envio> envioCaptor = ArgumentCaptor.forClass(Envio.class);
            verify(envioRepository).save(envioCaptor.capture());
            Envio savedEnvio = envioCaptor.getValue();

            assertThat(savedEnvio.getPesoNetoTotal()).isEqualTo(750.0);
            assertThat(savedEnvio.getPesoBrutoTotal()).isEqualTo(790.0);
            assertThat(savedEnvio.getNumeroPallets()).isEqualTo(2);
            assertThat(savedEnvio.getNumeroCajas()).isEqualTo(150);
        }
    }

    @Nested
    @DisplayName("List Shipments Tests")
    class ListShipmentsTests {

        @Test
        @DisplayName("Should list shipments by enterprise")
        void listarPorEmpresa_shouldReturnShipments() {
            // Given
            mockSecurityContext();
            when(envioRepository.findByEmpresaId(1L)).thenReturn(Arrays.asList(testEnvio));
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            List<EnvioResponse> result = envioService.listarPorEmpresa();

            // Then
            assertThat(result).hasSize(1);
            verify(envioRepository).findByEmpresaId(1L);
        }

        @Test
        @DisplayName("Should list shipments by state")
        void listarPorEstado_shouldReturnFilteredShipments() {
            // Given
            mockSecurityContext();
            when(envioRepository.findByEmpresaIdAndEstado(1L, "PREPARANDO"))
                    .thenReturn(Arrays.asList(testEnvio));
            when(envioMapper.toResponse(any(Envio.class))).thenReturn(envioResponse);

            // When
            List<EnvioResponse> result = envioService.listarPorEstado("PREPARANDO");

            // Then
            assertThat(result).hasSize(1);
            verify(envioRepository).findByEmpresaIdAndEstado(1L, "PREPARANDO");
        }
    }
}
