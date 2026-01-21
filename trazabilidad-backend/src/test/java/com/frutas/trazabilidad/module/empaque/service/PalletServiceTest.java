package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import com.frutas.trazabilidad.module.empaque.entity.EtiquetaPallet;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.mapper.PalletMapper;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PalletService.
 * Tests pallet management including label assignment and state changes.
 */
@ExtendWith(MockitoExtension.class)
class PalletServiceTest {

    @Mock
    private PalletRepository palletRepository;

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private PalletMapper mapper;

    @InjectMocks
    private PalletService palletService;

    private Pallet testPallet;
    private PalletRequest palletRequest;
    private PalletResponse palletResponse;
    private Etiqueta testEtiqueta;

    @BeforeEach
    void setUp() {
        testPallet = Pallet.builder()
                .id(1L)
                .codigoPallet("PAL-2024-001")
                .tipoPallet("ESTANDAR")
                .numeroCajas(80)
                .pesoNetoTotal(400.0)
                .pesoBrutoTotal(420.0)
                .destino("Estados Unidos")
                .tipoFruta("Mango")
                .estadoPallet("ARMADO")
                .fechaPaletizado(LocalDate.now())
                .etiquetas(new ArrayList<>())
                .activo(true)
                .build();

        testEtiqueta = Etiqueta.builder()
                .id(1L)
                .codigoEtiqueta("ETQ-001")
                .estadoEtiqueta("DISPONIBLE")
                .activo(true)
                .build();

        palletRequest = new PalletRequest();
        palletRequest.setCodigoPallet("PAL-2024-002");
        palletRequest.setTipoPallet("ESTANDAR");
        palletRequest.setNumeroCajas(80);
        palletRequest.setPesoNetoTotal(400.0);
        palletRequest.setPesoBrutoTotal(420.0);
        palletRequest.setDestino("Canada");
        palletRequest.setTipoFruta("Mango");

        palletResponse = PalletResponse.builder()
                .id(1L)
                .codigoPallet("PAL-2024-001")
                .tipoPallet("ESTANDAR")
                .estadoPallet("ARMADO")
                .numeroCajas(80)
                .build();
    }

    @Nested
    @DisplayName("List Pallets Tests")
    class ListPalletsTests {

        @Test
        @DisplayName("Should list all active pallets")
        void listarTodos_shouldReturnActivePallets() {
            // Given
            when(palletRepository.findByActivoTrueOrderByFechaPaletizadoDesc())
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarTodos();

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByActivoTrueOrderByFechaPaletizadoDesc();
        }

        @Test
        @DisplayName("Should list pallets by state")
        void listarPorEstado_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByEstadoPalletAndActivoTrue("ARMADO"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorEstado("ARMADO");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByEstadoPalletAndActivoTrue("ARMADO");
        }

        @Test
        @DisplayName("Should list pallets by destination")
        void listarPorDestino_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByDestinoContainingIgnoreCaseAndActivoTrue("Estados"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorDestino("Estados");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByDestinoContainingIgnoreCaseAndActivoTrue("Estados");
        }

        @Test
        @DisplayName("Should list pallets by fruit type")
        void listarPorTipoFruta_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByTipoFrutaAndActivoTrue("Mango"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorTipoFruta("Mango");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByTipoFrutaAndActivoTrue("Mango");
        }

        @Test
        @DisplayName("Should list pallets ready for shipment")
        void listarListosParaEnvio_shouldReturnReadyPallets() {
            // Given
            when(palletRepository.findPalletsListosParaEnvio())
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarListosParaEnvio();

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findPalletsListosParaEnvio();
        }
    }

    @Nested
    @DisplayName("Find Pallet Tests")
    class FindPalletTests {

        @Test
        @DisplayName("Should find pallet by ID")
        void buscarPorId_withValidId_shouldReturnPallet() {
            // Given
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(mapper.toResponse(testPallet)).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.buscarPorId(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCodigoPallet()).isEqualTo("PAL-2024-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void buscarPorId_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.buscarPorId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pallet no encontrado con ID: 999");
        }
    }

    @Nested
    @DisplayName("Create Pallet Tests")
    class CreatePalletTests {

        @Test
        @DisplayName("Should create pallet with valid data")
        void crear_withValidData_shouldCreatePallet() {
            // Given
            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(false);
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.crear(palletRequest);

            // Then
            assertThat(result).isNotNull();
            verify(palletRepository).save(any(Pallet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when codigo already exists")
        void crear_withDuplicateCodigo_shouldThrowIllegalArgumentException() {
            // Given
            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un pallet con el código: PAL-2024-002");

            verify(palletRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should assign labels when provided")
        void crear_withEtiquetas_shouldAssignLabels() {
            // Given
            palletRequest.setEtiquetasIds(Arrays.asList(1L));
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(false);
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.crear(palletRequest);

            // Then
            assertThat(testPallet.getEtiquetas()).hasSize(1);
            assertThat(testEtiqueta.getEstadoEtiqueta()).isEqualTo("ASIGNADA_PALLET");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when label not found")
        void crear_withInvalidEtiquetaId_shouldThrowResourceNotFoundException() {
            // Given
            palletRequest.setEtiquetasIds(Arrays.asList(999L));
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(false);
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Etiqueta no encontrada con ID: 999");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when label not available")
        void crear_withUnavailableEtiqueta_shouldThrowIllegalArgumentException() {
            // Given
            palletRequest.setEtiquetasIds(Arrays.asList(1L));
            testEtiqueta.setEstadoEtiqueta("ASIGNADA_PALLET");
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(false);
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no está disponible");
        }

        @Test
        @DisplayName("Should assign correct position to labels")
        void crear_shouldAssignPositionToLabels() {
            // Given
            Etiqueta etiqueta2 = Etiqueta.builder()
                    .id(2L)
                    .codigoEtiqueta("ETQ-002")
                    .estadoEtiqueta("DISPONIBLE")
                    .activo(true)
                    .build();

            palletRequest.setEtiquetasIds(Arrays.asList(1L, 2L));
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPallet(palletRequest.getCodigoPallet())).thenReturn(false);
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(testEtiqueta));
            when(etiquetaRepository.findById(2L)).thenReturn(Optional.of(etiqueta2));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.crear(palletRequest);

            // Then
            assertThat(testPallet.getEtiquetas()).hasSize(2);
            assertThat(testPallet.getEtiquetas().get(0).getPosicionEnPallet()).isEqualTo(1);
            assertThat(testPallet.getEtiquetas().get(1).getPosicionEnPallet()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Update Pallet Tests")
    class UpdatePalletTests {

        @Test
        @DisplayName("Should update pallet with valid data")
        void actualizar_withValidData_shouldUpdatePallet() {
            // Given
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.actualizar(1L, palletRequest);

            // Then
            assertThat(result).isNotNull();
            verify(mapper).updateEntityFromRequest(eq(testPallet), eq(palletRequest));
            verify(palletRepository).save(testPallet);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void actualizar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.actualizar(999L, palletRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pallet no encontrado con ID: 999");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when changing to existing codigo")
        void actualizar_withDuplicateCodigo_shouldThrowIllegalArgumentException() {
            // Given
            testPallet.setCodigoPallet("PAL-2024-001"); // Different from request
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(palletRepository.existsByCodigoPallet("PAL-2024-002")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> palletService.actualizar(1L, palletRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un pallet con el código: PAL-2024-002");
        }

        @Test
        @DisplayName("Should allow update when keeping same codigo")
        void actualizar_withSameCodigo_shouldAllowUpdate() {
            // Given
            testPallet.setCodigoPallet("PAL-2024-002"); // Same as request
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.actualizar(1L, palletRequest);

            // Then
            assertThat(result).isNotNull();
            verify(palletRepository, never()).existsByCodigoPallet(any());
        }
    }

    @Nested
    @DisplayName("Delete Pallet Tests")
    class DeletePalletTests {

        @Test
        @DisplayName("Should soft delete pallet and release labels")
        void eliminar_shouldSoftDeleteAndReleaseLabels() {
            // Given
            EtiquetaPallet ep = EtiquetaPallet.builder()
                    .etiqueta(testEtiqueta)
                    .pallet(testPallet)
                    .activo(true)
                    .build();
            testPallet.setEtiquetas(new ArrayList<>(List.of(ep)));
            testEtiqueta.setEstadoEtiqueta("ASIGNADA_PALLET");

            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));

            // When
            palletService.eliminar(1L);

            // Then
            ArgumentCaptor<Pallet> palletCaptor = ArgumentCaptor.forClass(Pallet.class);
            verify(palletRepository).save(palletCaptor.capture());

            assertThat(palletCaptor.getValue().getActivo()).isFalse();
            assertThat(testEtiqueta.getEstadoEtiqueta()).isEqualTo("DISPONIBLE");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void eliminar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.eliminar(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pallet no encontrado con ID: 999");
        }
    }

    @Nested
    @DisplayName("Change State Tests")
    class ChangeStateTests {

        @Test
        @DisplayName("Should change pallet state")
        void cambiarEstado_shouldChangeState() {
            // Given
            when(palletRepository.findById(1L)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.cambiarEstado(1L, "EN_CAMARA");

            // Then
            ArgumentCaptor<Pallet> palletCaptor = ArgumentCaptor.forClass(Pallet.class);
            verify(palletRepository).save(palletCaptor.capture());
            assertThat(palletCaptor.getValue().getEstadoPallet()).isEqualTo("EN_CAMARA");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void cambiarEstado_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.cambiarEstado(999L, "EN_CAMARA"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pallet no encontrado con ID: 999");
        }
    }
}
