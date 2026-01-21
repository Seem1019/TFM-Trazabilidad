package com.frutas.trazabilidad.module.logistica.service;

import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.entity.Envio;
import com.frutas.trazabilidad.module.logistica.entity.EventoLogistico;
import com.frutas.trazabilidad.module.logistica.mapper.EventoLogisticoMapper;
import com.frutas.trazabilidad.module.logistica.repository.EnvioRepository;
import com.frutas.trazabilidad.module.logistica.repository.EventoLogisticoRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventoLogisticoService.
 * Tests logistics event management including creation, updates, and multi-tenant isolation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventoLogisticoServiceTest {

    @Mock
    private EventoLogisticoRepository eventoRepository;

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventoLogisticoMapper eventoMapper;

    @Mock
    private AuditoriaEventoService auditoriaService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventoLogisticoService eventoLogisticoService;

    private User testUser;
    private User otherUser;
    private Empresa testEmpresa;
    private Empresa otherEmpresa;
    private Envio testEnvio;
    private EventoLogistico testEvento;
    private EventoLogisticoRequest eventoRequest;
    private EventoLogisticoResponse eventoResponse;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .activo(true)
                .build();

        otherEmpresa = Empresa.builder()
                .id(2L)
                .nit("900654321-1")
                .razonSocial("Otra Empresa S.A.S")
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

        otherUser = User.builder()
                .id(2L)
                .email("operador@otraempresa.com")
                .nombre("Pedro")
                .apellido("Garcia")
                .empresa(otherEmpresa)
                .rol(TipoRol.OPERADOR_LOGISTICA)
                .activo(true)
                .build();

        testEnvio = new Envio();
        testEnvio.setId(1L);
        testEnvio.setCodigoEnvio("ENV-001");
        testEnvio.setEstado("EN_TRANSITO");
        testEnvio.setUsuario(testUser);
        testEnvio.setActivo(true);

        testEvento = new EventoLogistico();
        testEvento.setId(1L);
        testEvento.setTipoEvento("CARGA");
        testEvento.setFechaEvento(LocalDate.now());
        testEvento.setUbicacion("Planta de empaque");
        testEvento.setCiudad("Bogota");
        testEvento.setPais("Colombia");
        testEvento.setEnvio(testEnvio);
        testEvento.setActivo(true);

        eventoRequest = new EventoLogisticoRequest();
        eventoRequest.setEnvioId(1L);
        eventoRequest.setTipoEvento("CARGA");
        eventoRequest.setFechaEvento(LocalDate.now());
        eventoRequest.setUbicacion("Planta de empaque");
        eventoRequest.setCiudad("Bogota");
        eventoRequest.setPais("Colombia");

        eventoResponse = EventoLogisticoResponse.builder()
                .id(1L)
                .tipoEvento("CARGA")
                .ubicacion("Planta de empaque")
                .ciudad("Bogota")
                .pais("Colombia")
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operador@frutascolombia.com");
        when(userRepository.findByEmail("operador@frutascolombia.com")).thenReturn(Optional.of(testUser));
    }

    private void mockSecurityContextForOtherUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operador@otraempresa.com");
        when(userRepository.findByEmail("operador@otraempresa.com")).thenReturn(Optional.of(otherUser));
    }

    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event with valid data")
        void crear_withValidData_shouldCreateEvent() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(eventoMapper.toEntity(eq(eventoRequest), eq(testEnvio))).thenReturn(testEvento);
            when(eventoRepository.save(any(EventoLogistico.class))).thenReturn(testEvento);
            when(eventoMapper.toResponse(any(EventoLogistico.class))).thenReturn(eventoResponse);

            // When
            EventoLogisticoResponse result = eventoLogisticoService.crear(eventoRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTipoEvento()).isEqualTo("CARGA");
            verify(eventoRepository).save(any(EventoLogistico.class));
            verify(auditoriaService).registrarCreacion(
                    eq("EVENTO_LOGISTICO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw exception when shipment not found")
        void crear_withInvalidEnvioId_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(999L)).thenReturn(Optional.empty());
            eventoRequest.setEnvioId(999L);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.crear(eventoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Envío no encontrado con ID: 999");

            verify(eventoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user has no permission for shipment")
        void crear_withNoPermission_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.crear(eventoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para registrar eventos en este envío");

            verify(eventoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Event Tests")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event with valid data")
        void actualizar_withValidData_shouldUpdateEvent() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(eventoRepository.save(any(EventoLogistico.class))).thenReturn(testEvento);
            when(eventoMapper.toResponse(any(EventoLogistico.class))).thenReturn(eventoResponse);

            // When
            EventoLogisticoResponse result = eventoLogisticoService.actualizar(1L, eventoRequest);

            // Then
            assertThat(result).isNotNull();
            verify(eventoMapper).updateEntity(eq(testEvento), eq(eventoRequest));
            verify(auditoriaService).registrarActualizacion(
                    eq("EVENTO_LOGISTICO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    any(),
                    any(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void actualizar_withInvalidId_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.actualizar(999L, eventoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Evento logístico no encontrado con ID: 999");

            verify(eventoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void actualizar_withNoPermission_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.actualizar(1L, eventoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para modificar este evento");

            verify(eventoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("List Events By Shipment Tests")
    class ListEventsByShipmentTests {

        @Test
        @DisplayName("Should list events by shipment ID")
        void listarPorEnvio_shouldReturnEvents() {
            // Given
            mockSecurityContext();
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(eventoRepository.findByEnvioIdOrderByFechaAsc(1L))
                    .thenReturn(Arrays.asList(testEvento));
            when(eventoMapper.toResponse(any(EventoLogistico.class))).thenReturn(eventoResponse);

            // When
            List<EventoLogisticoResponse> result = eventoLogisticoService.listarPorEnvio(1L);

            // Then
            assertThat(result).hasSize(1);
            verify(eventoRepository).findByEnvioIdOrderByFechaAsc(1L);
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void listarPorEnvio_withNoPermission_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(envioRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.listarPorEnvio(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para ver eventos de este envío");
        }
    }

    @Nested
    @DisplayName("Get Event By ID Tests")
    class GetEventByIdTests {

        @Test
        @DisplayName("Should return event by ID")
        void obtenerPorId_shouldReturnEvent() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);
            when(eventoMapper.toResponse(testEvento)).thenReturn(eventoResponse);

            // When
            EventoLogisticoResponse result = eventoLogisticoService.obtenerPorId(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTipoEvento()).isEqualTo("CARGA");
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void obtenerPorId_withNoPermission_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.obtenerPorId(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para ver este evento");
        }
    }

    @Nested
    @DisplayName("Delete Event Tests")
    class DeleteEventTests {

        @Test
        @DisplayName("Should soft delete event")
        void eliminar_shouldSoftDeleteEvent() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(true);

            // When
            eventoLogisticoService.eliminar(1L);

            // Then
            ArgumentCaptor<EventoLogistico> captor = ArgumentCaptor.forClass(EventoLogistico.class);
            verify(eventoRepository).save(captor.capture());
            assertThat(captor.getValue().getActivo()).isFalse();

            verify(auditoriaService).registrarEliminacion(
                    eq("EVENTO_LOGISTICO"),
                    anyLong(),
                    anyString(),
                    anyString(),
                    eq(testUser)
            );
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void eliminar_withInvalidId_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.eliminar(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Evento logístico no encontrado con ID: 999");

            verify(eventoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void eliminar_withNoPermission_shouldThrowException() {
            // Given
            mockSecurityContext();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 1L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.eliminar(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para eliminar este evento");

            verify(eventoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Multi-Tenant Isolation Tests")
    class MultiTenantIsolationTests {

        @Test
        @DisplayName("Should prevent user from accessing events from another company")
        void crear_fromDifferentCompany_shouldThrowException() {
            // Given - User from company 2 trying to create event in company 1's shipment
            mockSecurityContextForOtherUser();
            when(envioRepository.findById(1L)).thenReturn(Optional.of(testEnvio));
            when(envioRepository.existsByIdAndEmpresaId(1L, 2L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.crear(eventoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para registrar eventos en este envío");
        }

        @Test
        @DisplayName("Should prevent user from viewing events from another company")
        void obtenerPorId_fromDifferentCompany_shouldThrowException() {
            // Given - User from company 2 trying to view company 1's event
            mockSecurityContextForOtherUser();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 2L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.obtenerPorId(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para ver este evento");
        }

        @Test
        @DisplayName("Should prevent user from deleting events from another company")
        void eliminar_fromDifferentCompany_shouldThrowException() {
            // Given - User from company 2 trying to delete company 1's event
            mockSecurityContextForOtherUser();
            when(eventoRepository.findById(1L)).thenReturn(Optional.of(testEvento));
            when(eventoRepository.existsByIdAndEmpresaId(1L, 2L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.eliminar(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para eliminar este evento");

            verify(eventoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent user from listing events from another company's shipment")
        void listarPorEnvio_fromDifferentCompany_shouldThrowException() {
            // Given - User from company 2 trying to list company 1's shipment events
            mockSecurityContextForOtherUser();
            when(envioRepository.existsByIdAndEmpresaId(1L, 2L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> eventoLogisticoService.listarPorEnvio(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("No tiene permisos para ver eventos de este envío");
        }
    }
}
