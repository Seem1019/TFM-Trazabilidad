package com.frutas.trazabilidad.module.produccion.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.exception.ConflictException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.module.produccion.dto.FincaRequest;
import com.frutas.trazabilidad.module.produccion.dto.FincaResponse;
import com.frutas.trazabilidad.module.produccion.entity.Finca;
import com.frutas.trazabilidad.module.produccion.mapper.FincaMapper;
import com.frutas.trazabilidad.module.produccion.repository.CertificacionRepository;
import com.frutas.trazabilidad.module.produccion.repository.FincaRepository;
import com.frutas.trazabilidad.module.produccion.repository.LoteRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FincaService.
 * Tests farm management including multi-company isolation and business rules.
 */
@ExtendWith(MockitoExtension.class)
class FincaServiceTest {

    @Mock
    private FincaRepository fincaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private CertificacionRepository certificacionRepository;

    @Mock
    private FincaMapper fincaMapper;

    @InjectMocks
    private FincaService fincaService;

    private Empresa testEmpresa;
    private Finca testFinca;
    private FincaRequest fincaRequest;
    private FincaResponse fincaResponse;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .activo(true)
                .build();

        testFinca = Finca.builder()
                .id(1L)
                .codigoFinca("FIN-001")
                .nombre("Finca La Esperanza")
                .ubicacion("Vereda El Progreso")
                .municipio("Villeta")
                .departamento("Cundinamarca")
                .pais("Colombia")
                .areaHectareas(50.0)
                .propietario("Juan Perez")
                .encargado("Carlos Rodriguez")
                .telefono("3001234567")
                .email("finca@ejemplo.com")
                .latitud(5.0167)
                .longitud(-74.4833)
                .empresa(testEmpresa)
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        fincaRequest = new FincaRequest();
        fincaRequest.setCodigoFinca("FIN-002");
        fincaRequest.setNombre("Finca Nueva");
        fincaRequest.setUbicacion("Vereda La Paz");
        fincaRequest.setMunicipio("Anapoima");
        fincaRequest.setDepartamento("Cundinamarca");
        fincaRequest.setPais("Colombia");
        fincaRequest.setAreaHectareas(30.0);

        fincaResponse = FincaResponse.builder()
                .id(1L)
                .codigoFinca("FIN-001")
                .nombre("Finca La Esperanza")
                .municipio("Villeta")
                .departamento("Cundinamarca")
                .pais("Colombia")
                .areaHectareas(50.0)
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("List Farms Tests")
    class ListFarmsTests {

        @Test
        @DisplayName("Should list all active farms for an enterprise")
        void listarPorEmpresa_shouldReturnActiveFarms() {
            // Given
            Finca finca2 = Finca.builder()
                    .id(2L)
                    .codigoFinca("FIN-002")
                    .nombre("Finca El Sol")
                    .empresa(testEmpresa)
                    .activo(true)
                    .build();

            when(fincaRepository.findByEmpresaIdAndActivoTrue(1L))
                    .thenReturn(Arrays.asList(testFinca, finca2));
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            List<FincaResponse> result = fincaService.listarPorEmpresa(1L);

            // Then
            assertThat(result).hasSize(2);
            verify(fincaRepository).findByEmpresaIdAndActivoTrue(1L);
        }

        @Test
        @DisplayName("Should return empty list when no farms exist")
        void listarPorEmpresa_withNoFarms_shouldReturnEmptyList() {
            // Given
            when(fincaRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(List.of());

            // When
            List<FincaResponse> result = fincaService.listarPorEmpresa(1L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should include total lotes and certifications in response")
        void listarPorEmpresa_shouldIncludeTotals() {
            // Given
            when(fincaRepository.findByEmpresaIdAndActivoTrue(1L))
                    .thenReturn(List.of(testFinca));
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(1L)).thenReturn(5L);
            when(certificacionRepository.countCertificacionesVigentes(1L)).thenReturn(2L);

            // When
            List<FincaResponse> result = fincaService.listarPorEmpresa(1L);

            // Then
            assertThat(result).hasSize(1);
            verify(loteRepository).countByFincaIdAndActivoTrue(1L);
            verify(certificacionRepository).countCertificacionesVigentes(1L);
        }
    }

    @Nested
    @DisplayName("Find Farm Tests")
    class FindFarmTests {

        @Test
        @DisplayName("Should find farm by ID when belongs to enterprise")
        void buscarPorId_withValidIdAndEnterprise_shouldReturnFarm() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(fincaMapper.toResponse(testFinca)).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(1L)).thenReturn(3L);
            when(certificacionRepository.countCertificacionesVigentes(1L)).thenReturn(1L);

            // When
            FincaResponse result = fincaService.buscarPorId(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCodigoFinca()).isEqualTo("FIN-001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when farm not found or not belonging to enterprise")
        void buscarPorId_withInvalidIdOrEnterprise_shouldThrowResourceNotFoundException() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(1L, 2L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fincaService.buscarPorId(1L, 2L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create Farm Tests")
    class CreateFarmTests {

        @Test
        @DisplayName("Should create farm with valid data")
        void crear_withValidData_shouldCreateFarm() {
            // Given
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(testEmpresa));
            when(fincaRepository.existsByCodigoFincaAndEmpresaId("FIN-002", 1L)).thenReturn(false);
            when(fincaMapper.toEntity(fincaRequest)).thenReturn(testFinca);
            when(fincaRepository.save(any(Finca.class))).thenReturn(testFinca);
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            FincaResponse result = fincaService.crear(fincaRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(fincaRepository).save(any(Finca.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when enterprise not found")
        void crear_withInvalidEnterprise_shouldThrowResourceNotFoundException() {
            // Given
            when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fincaService.crear(fincaRequest, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(fincaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ConflictException when codigo already exists for enterprise")
        void crear_withDuplicateCodigo_shouldThrowConflictException() {
            // Given
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(testEmpresa));
            when(fincaRepository.existsByCodigoFincaAndEmpresaId("FIN-002", 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> fincaService.crear(fincaRequest, 1L))
                    .isInstanceOf(ConflictException.class);

            verify(fincaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should set enterprise and active status")
        void crear_shouldSetEnterpriseAndActiveStatus() {
            // Given
            Finca newFinca = Finca.builder().build();

            when(empresaRepository.findById(1L)).thenReturn(Optional.of(testEmpresa));
            when(fincaRepository.existsByCodigoFincaAndEmpresaId("FIN-002", 1L)).thenReturn(false);
            when(fincaMapper.toEntity(fincaRequest)).thenReturn(newFinca);
            when(fincaRepository.save(any(Finca.class))).thenReturn(testFinca);
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            fincaService.crear(fincaRequest, 1L);

            // Then
            ArgumentCaptor<Finca> fincaCaptor = ArgumentCaptor.forClass(Finca.class);
            verify(fincaRepository).save(fincaCaptor.capture());
            assertThat(fincaCaptor.getValue().getEmpresa()).isEqualTo(testEmpresa);
            assertThat(fincaCaptor.getValue().getActivo()).isTrue();
        }
    }

    @Nested
    @DisplayName("Update Farm Tests")
    class UpdateFarmTests {

        @Test
        @DisplayName("Should update farm with valid data")
        void actualizar_withValidData_shouldUpdateFarm() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(fincaRepository.save(any(Finca.class))).thenReturn(testFinca);
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            FincaResponse result = fincaService.actualizar(1L, fincaRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(fincaMapper).updateEntityFromRequest(eq(fincaRequest), eq(testFinca));
            verify(fincaRepository).save(testFinca);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when farm not found")
        void actualizar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(999L, 1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fincaService.actualizar(999L, fincaRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(fincaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ConflictException when changing to existing codigo")
        void actualizar_withDuplicateCodigo_shouldThrowConflictException() {
            // Given - finca has FIN-001, trying to change to FIN-002 which exists
            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(fincaRepository.existsByCodigoFincaAndEmpresaId("FIN-002", 1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> fincaService.actualizar(1L, fincaRequest, 1L))
                    .isInstanceOf(ConflictException.class);

            verify(fincaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow update when keeping same codigo")
        void actualizar_withSameCodigo_shouldAllowUpdate() {
            // Given
            testFinca.setCodigoFinca("FIN-002"); // Same as request
            fincaRequest.setCodigoFinca("FIN-002");

            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(fincaRepository.save(any(Finca.class))).thenReturn(testFinca);
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            FincaResponse result = fincaService.actualizar(1L, fincaRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(fincaRepository, never()).existsByCodigoFincaAndEmpresaId(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("Delete Farm Tests")
    class DeleteFarmTests {

        @Test
        @DisplayName("Should soft delete farm when no active lotes")
        void eliminar_withNoActiveLotes_shouldSoftDelete() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(loteRepository.countByFincaIdAndActivoTrue(1L)).thenReturn(0L);

            // When
            fincaService.eliminar(1L, 1L);

            // Then
            ArgumentCaptor<Finca> fincaCaptor = ArgumentCaptor.forClass(Finca.class);
            verify(fincaRepository).save(fincaCaptor.capture());
            assertThat(fincaCaptor.getValue().getActivo()).isFalse();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when farm not found")
        void eliminar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(999L, 1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> fincaService.eliminar(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(fincaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ConflictException when farm has active lotes")
        void eliminar_withActiveLotes_shouldThrowConflictException() {
            // Given
            when(fincaRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(testFinca));
            when(loteRepository.countByFincaIdAndActivoTrue(1L)).thenReturn(3L);

            // When/Then
            assertThatThrownBy(() -> fincaService.eliminar(1L, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("No se puede eliminar la finca porque tiene 3 lotes activos");

            verify(fincaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Search Farm Tests")
    class SearchFarmTests {

        @Test
        @DisplayName("Should search farms by name")
        void buscarPorNombre_shouldReturnMatchingFarms() {
            // Given
            when(fincaRepository.findByEmpresaIdAndNombreContaining(1L, "Esperanza"))
                    .thenReturn(List.of(testFinca));
            when(fincaMapper.toResponse(any(Finca.class))).thenReturn(fincaResponse);
            when(loteRepository.countByFincaIdAndActivoTrue(anyLong())).thenReturn(0L);
            when(certificacionRepository.countCertificacionesVigentes(anyLong())).thenReturn(0L);

            // When
            List<FincaResponse> result = fincaService.buscarPorNombre("Esperanza", 1L);

            // Then
            assertThat(result).hasSize(1);
            verify(fincaRepository).findByEmpresaIdAndNombreContaining(1L, "Esperanza");
        }

        @Test
        @DisplayName("Should return empty list when no farms match")
        void buscarPorNombre_withNoMatch_shouldReturnEmptyList() {
            // Given
            when(fincaRepository.findByEmpresaIdAndNombreContaining(1L, "NoExiste"))
                    .thenReturn(List.of());

            // When
            List<FincaResponse> result = fincaService.buscarPorNombre("NoExiste", 1L);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
