package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.UserRequest;
import com.frutas.trazabilidad.dto.UserResponse;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.mapper.UserMapper;
import com.frutas.trazabilidad.repository.EmpresaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests all user management business rules including multi-company isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Empresa testEmpresa;
    private Empresa otherEmpresa;
    private User testUser;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .nombreComercial("Frutas Colombia")
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
                .email("usuario@frutascolombia.com")
                .passwordHash("$2a$10$hashedPassword")
                .nombre("Maria")
                .apellido("Garcia")
                .telefono("3001234567")
                .empresa(testEmpresa)
                .rol(TipoRol.PRODUCTOR)
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRequest = UserRequest.builder()
                .email("nuevo@frutascolombia.com")
                .password("password123")
                .nombre("Carlos")
                .apellido("Lopez")
                .telefono("3009876543")
                .rol(TipoRol.PRODUCTOR)
                .activo(true)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("usuario@frutascolombia.com")
                .nombre("Maria")
                .apellido("Garcia")
                .rol(TipoRol.PRODUCTOR)
                .activo(true)
                .empresaId(1L)
                .empresaNombre("Frutas Colombia")
                .build();
    }

    @Nested
    @DisplayName("List Users Tests")
    class ListUsersTests {

        @Test
        @DisplayName("Should list all active users for an enterprise")
        void listarPorEmpresa_shouldReturnActiveUsers() {
            // Given
            User user2 = User.builder()
                    .id(2L)
                    .email("otro@frutascolombia.com")
                    .nombre("Pedro")
                    .empresa(testEmpresa)
                    .activo(true)
                    .build();

            when(userRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(Arrays.asList(testUser, user2));
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            List<UserResponse> result = userService.listarPorEmpresa(1L);

            // Then
            assertThat(result).hasSize(2);
            verify(userRepository).findByEmpresaIdAndActivoTrue(1L);
        }

        @Test
        @DisplayName("Should list all users including inactive for an enterprise")
        void listarTodosPorEmpresa_shouldReturnAllUsers() {
            // Given
            User inactiveUser = User.builder()
                    .id(3L)
                    .email("inactivo@frutascolombia.com")
                    .empresa(testEmpresa)
                    .activo(false)
                    .build();

            when(userRepository.findByEmpresaId(1L)).thenReturn(Arrays.asList(testUser, inactiveUser));
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            List<UserResponse> result = userService.listarTodosPorEmpresa(1L);

            // Then
            assertThat(result).hasSize(2);
            verify(userRepository).findByEmpresaId(1L);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void listarPorEmpresa_withNoUsers_shouldReturnEmptyList() {
            // Given
            when(userRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(List.of());

            // When
            List<UserResponse> result = userService.listarPorEmpresa(1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find User By ID Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("Should find user by ID when belongs to same enterprise")
        void buscarPorId_withValidIdAndEnterprise_shouldReturnUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mapper.toResponse(testUser)).thenReturn(userResponse);

            // When
            UserResponse result = userService.buscarPorId(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("usuario@frutascolombia.com");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void buscarPorId_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.buscarPorId(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado con ID: 999");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user belongs to different enterprise")
        void buscarPorId_withDifferentEnterprise_shouldThrowIllegalArgumentException() {
            // Given - User belongs to testEmpresa (id=1), but we query with empresaId=2
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.buscarPorId(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El usuario no pertenece a la empresa del administrador");
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user with valid data")
        void crear_withValidData_shouldCreateUser() {
            // Given
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(testEmpresa));
            when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("$2a$10$encodedPassword");
            when(mapper.toEntity(eq(userRequest), eq(testEmpresa), anyString())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(testUser)).thenReturn(userResponse);

            // When
            UserResponse result = userService.crear(userRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(userRequest.getPassword());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email already exists")
        void crear_withDuplicateEmail_shouldThrowIllegalArgumentException() {
            // Given
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.crear(userRequest, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un usuario con el email: nuevo@frutascolombia.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password is null")
        void crear_withNullPassword_shouldThrowIllegalArgumentException() {
            // Given
            userRequest.setPassword(null);
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.crear(userRequest, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La contraseña es obligatoria al crear un usuario");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password is blank")
        void crear_withBlankPassword_shouldThrowIllegalArgumentException() {
            // Given
            userRequest.setPassword("   ");
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.crear(userRequest, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("La contraseña es obligatoria al crear un usuario");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when enterprise not found")
        void crear_withInvalidEnterprise_shouldThrowResourceNotFoundException() {
            // Given
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.crear(userRequest, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Empresa no encontrada con ID: 999");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode password before saving")
        void crear_shouldEncodePassword() {
            // Given
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(testEmpresa));
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
            when(mapper.toEntity(eq(userRequest), eq(testEmpresa), eq("$2a$10$encodedPassword"))).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any())).thenReturn(userResponse);

            // When
            userService.crear(userRequest, 1L);

            // Then
            verify(passwordEncoder).encode("password123");
            verify(mapper).toEntity(eq(userRequest), eq(testEmpresa), eq("$2a$10$encodedPassword"));
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user with valid data")
        void actualizar_withValidData_shouldUpdateUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("$2a$10$newPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            UserResponse result = userService.actualizar(1L, userRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(mapper).updateEntityFromRequest(eq(testUser), eq(userRequest), anyString());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update user without password when password is null")
        void actualizar_withNullPassword_shouldUpdateWithoutPassword() {
            // Given
            userRequest.setPassword(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            userService.actualizar(1L, userRequest, 1L);

            // Then
            verify(mapper).updateEntityFromRequest(testUser, userRequest, null);
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when changing to existing email")
        void actualizar_withDuplicateEmail_shouldThrowIllegalArgumentException() {
            // Given - testUser has different email than request
            testUser.setEmail("original@frutascolombia.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.actualizar(1L, userRequest, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ya existe un usuario con el email: nuevo@frutascolombia.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow updating when email unchanged even if exists")
        void actualizar_withSameEmail_shouldAllowUpdate() {
            // Given - User keeping same email
            testUser.setEmail("nuevo@frutascolombia.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            UserResponse result = userService.actualizar(1L, userRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository, never()).existsByEmail(any()); // Should not check email uniqueness
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user belongs to different enterprise")
        void actualizar_withDifferentEnterprise_shouldThrowIllegalArgumentException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.actualizar(1L, userRequest, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El usuario no pertenece a la empresa del administrador");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should soft delete user")
        void eliminar_shouldSoftDeleteUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.eliminar(1L, 1L);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getActivo()).isFalse();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void eliminar_withInvalidId_shouldThrowResourceNotFoundException() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.eliminar(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado con ID: 999");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user belongs to different enterprise")
        void eliminar_withDifferentEnterprise_shouldThrowIllegalArgumentException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.eliminar(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El usuario no pertenece a la empresa del administrador");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Change Status Tests")
    class ChangeStatusTests {

        @Test
        @DisplayName("Should activate user")
        void cambiarEstado_toActive_shouldActivateUser() {
            // Given
            testUser.setActivo(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            userService.cambiarEstado(1L, true, 1L);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getActivo()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate user")
        void cambiarEstado_toInactive_shouldDeactivateUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(mapper.toResponse(any(User.class))).thenReturn(userResponse);

            // When
            userService.cambiarEstado(1L, false, 1L);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getActivo()).isFalse();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user belongs to different enterprise")
        void cambiarEstado_withDifferentEnterprise_shouldThrowIllegalArgumentException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.cambiarEstado(1L, false, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El usuario no pertenece a la empresa del administrador");

            verify(userRepository, never()).save(any());
        }
    }
}
