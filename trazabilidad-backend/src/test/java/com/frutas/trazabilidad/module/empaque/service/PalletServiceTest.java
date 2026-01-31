package com.frutas.trazabilidad.module.empaque.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.entity.Etiqueta;
import com.frutas.trazabilidad.module.empaque.entity.EtiquetaPallet;
import com.frutas.trazabilidad.module.empaque.entity.Pallet;
import com.frutas.trazabilidad.module.empaque.mapper.PalletMapper;
import com.frutas.trazabilidad.module.empaque.repository.EtiquetaRepository;
import com.frutas.trazabilidad.module.empaque.repository.PalletRepository;
import com.frutas.trazabilidad.repository.EmpresaRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PalletService.
 * Tests pallet management including label assignment and state changes.
 * All tests include empresaId parameter for multitenant validation.
 */
@ExtendWith(MockitoExtension.class)
class PalletServiceTest {

    @Mock
    private PalletRepository palletRepository;

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PalletMapper mapper;

    @InjectMocks
    private PalletService palletService;

    private static final Long EMPRESA_ID = 1L;

    private Empresa testEmpresa;
    private Pallet testPallet;
    private PalletRequest palletRequest;
    private PalletResponse palletResponse;
    private Etiqueta testEtiqueta;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(EMPRESA_ID)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .activo(true)
                .build();

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
                .empresa(testEmpresa)
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
        @DisplayName("Should list all active pallets for empresa")
        void listarPorEmpresa_shouldReturnActivePallets() {
            // Given
            when(palletRepository.findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc(EMPRESA_ID))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorEmpresa(EMPRESA_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc(EMPRESA_ID);
        }

        @Test
        @DisplayName("Should list pallets by state for empresa")
        void listarPorEstado_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByEmpresaIdAndEstadoPalletAndActivoTrue(EMPRESA_ID, "ARMADO"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorEstado(EMPRESA_ID, "ARMADO");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByEmpresaIdAndEstadoPalletAndActivoTrue(EMPRESA_ID, "ARMADO");
        }

        @Test
        @DisplayName("Should list pallets by destination for empresa")
        void listarPorDestino_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByEmpresaIdAndDestinoContainingIgnoreCaseAndActivoTrue(EMPRESA_ID, "Estados"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorDestino(EMPRESA_ID, "Estados");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByEmpresaIdAndDestinoContainingIgnoreCaseAndActivoTrue(EMPRESA_ID, "Estados");
        }

        @Test
        @DisplayName("Should list pallets by fruit type for empresa")
        void listarPorTipoFruta_shouldReturnFilteredPallets() {
            // Given
            when(palletRepository.findByEmpresaIdAndTipoFrutaAndActivoTrue(EMPRESA_ID, "Mango"))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarPorTipoFruta(EMPRESA_ID, "Mango");

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findByEmpresaIdAndTipoFrutaAndActivoTrue(EMPRESA_ID, "Mango");
        }

        @Test
        @DisplayName("Should list pallets ready for shipment for empresa")
        void listarListosParaEnvio_shouldReturnReadyPallets() {
            // Given
            when(palletRepository.findPalletsListosParaEnvio(EMPRESA_ID))
                    .thenReturn(Arrays.asList(testPallet));
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            List<PalletResponse> result = palletService.listarListosParaEnvio(EMPRESA_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(palletRepository).findPalletsListosParaEnvio(EMPRESA_ID);
        }
    }

    @Nested
    @DisplayName("Find Pallet Tests")
    class FindPalletTests {

        @Test
        @DisplayName("Should find pallet by ID and empresaId")
        void buscarPorId_withValidId_shouldReturnPallet() {
            // Given
            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));
            when(mapper.toResponse(testPallet)).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.buscarPorId(1L, EMPRESA_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCodigoPallet()).isEqualTo("PAL-2024-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void buscarPorId_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.buscarPorId(999L, EMPRESA_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when pallet belongs to different empresa")
        void buscarPorId_withDifferentEmpresa_shouldThrowResourceNotFoundException() {
            // Given
            Long otherEmpresaId = 999L;
            when(palletRepository.findByIdAndEmpresaId(1L, otherEmpresaId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.buscarPorId(1L, otherEmpresaId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create Pallet Tests")
    class CreatePalletTests {

        @Test
        @DisplayName("Should create pallet with valid data")
        void crear_withValidData_shouldCreatePallet() {
            // Given
            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(false);
            when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(testEmpresa));
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.crear(palletRequest, EMPRESA_ID);

            // Then
            assertThat(result).isNotNull();
            verify(palletRepository).save(any(Pallet.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when codigo already exists in empresa")
        void crear_withDuplicateCodigo_shouldThrowIllegalArgumentException() {
            // Given
            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest, EMPRESA_ID))
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

            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(false);
            when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(testEmpresa));
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testEtiqueta));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.crear(palletRequest, EMPRESA_ID);

            // Then
            assertThat(testPallet.getEtiquetas()).hasSize(1);
            assertThat(testEtiqueta.getEstadoEtiqueta()).isEqualTo("ASIGNADA_PALLET");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when label not found in empresa")
        void crear_withInvalidEtiquetaId_shouldThrowResourceNotFoundException() {
            // Given
            palletRequest.setEtiquetasIds(Arrays.asList(999L));
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(false);
            when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(testEmpresa));
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest, EMPRESA_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when label not available")
        void crear_withUnavailableEtiqueta_shouldThrowIllegalArgumentException() {
            // Given
            palletRequest.setEtiquetasIds(Arrays.asList(1L));
            testEtiqueta.setEstadoEtiqueta("ASIGNADA_PALLET");
            testPallet.setEtiquetas(new ArrayList<>());

            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(false);
            when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(testEmpresa));
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testEtiqueta));

            // When/Then
            assertThatThrownBy(() -> palletService.crear(palletRequest, EMPRESA_ID))
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

            when(palletRepository.existsByCodigoPalletAndEmpresaId(palletRequest.getCodigoPallet(), EMPRESA_ID)).thenReturn(false);
            when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(testEmpresa));
            when(mapper.toEntity(palletRequest)).thenReturn(testPallet);
            when(etiquetaRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testEtiqueta));
            when(etiquetaRepository.findByIdAndEmpresaId(2L, EMPRESA_ID)).thenReturn(Optional.of(etiqueta2));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.crear(palletRequest, EMPRESA_ID);

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
            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.actualizar(1L, palletRequest, EMPRESA_ID);

            // Then
            assertThat(result).isNotNull();
            verify(mapper).updateEntityFromRequest(eq(testPallet), eq(palletRequest));
            verify(palletRepository).save(testPallet);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void actualizar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.actualizar(999L, palletRequest, EMPRESA_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when changing to existing codigo")
        void actualizar_withDuplicateCodigo_shouldThrowIllegalArgumentException() {
            // Given
            testPallet.setCodigoPallet("PAL-2024-001"); // Different from request
            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));
            when(palletRepository.existsByCodigoPalletAndEmpresaId("PAL-2024-002", EMPRESA_ID)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> palletService.actualizar(1L, palletRequest, EMPRESA_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un pallet con el código: PAL-2024-002");
        }

        @Test
        @DisplayName("Should allow update when keeping same codigo")
        void actualizar_withSameCodigo_shouldAllowUpdate() {
            // Given
            testPallet.setCodigoPallet("PAL-2024-002"); // Same as request
            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            PalletResponse result = palletService.actualizar(1L, palletRequest, EMPRESA_ID);

            // Then
            assertThat(result).isNotNull();
            verify(palletRepository, never()).existsByCodigoPalletAndEmpresaId(any(), any());
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

            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));

            // When
            palletService.eliminar(1L, EMPRESA_ID);

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
            when(palletRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.eliminar(999L, EMPRESA_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Change State Tests")
    class ChangeStateTests {

        @Test
        @DisplayName("Should change pallet state")
        void cambiarEstado_shouldChangeState() {
            // Given
            when(palletRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(testPallet));
            when(palletRepository.save(any(Pallet.class))).thenReturn(testPallet);
            when(mapper.toResponse(any(Pallet.class))).thenReturn(palletResponse);

            // When
            palletService.cambiarEstado(1L, "EN_CAMARA", EMPRESA_ID);

            // Then
            ArgumentCaptor<Pallet> palletCaptor = ArgumentCaptor.forClass(Pallet.class);
            verify(palletRepository).save(palletCaptor.capture());
            assertThat(palletCaptor.getValue().getEstadoPallet()).isEqualTo("EN_CAMARA");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid ID")
        void cambiarEstado_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(palletRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> palletService.cambiarEstado(999L, "EN_CAMARA", EMPRESA_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
