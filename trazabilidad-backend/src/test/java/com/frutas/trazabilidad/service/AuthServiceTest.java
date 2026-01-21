package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.dto.LoginRequest;
import com.frutas.trazabilidad.dto.LoginResponse;
import com.frutas.trazabilidad.dto.PasswordResetConfirmRequest;
import com.frutas.trazabilidad.dto.PasswordResetRequest;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.PasswordResetToken;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.exception.ForbiddenException;
import com.frutas.trazabilidad.exception.ResourceNotFoundException;
import com.frutas.trazabilidad.exception.UnauthorizedException;
import com.frutas.trazabilidad.repository.PasswordResetTokenRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import com.frutas.trazabilidad.security.JwtUtil;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests all authentication business rules including login, password reset, and token validation.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Empresa testEmpresa;

    @BeforeEach
    void setUp() {
        testEmpresa = Empresa.builder()
                .id(1L)
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .nombreComercial("Frutas Colombia")
                .activo(true)
                .build();

        testUser = User.builder()
                .id(1L)
                .email("admin@frutascolombia.com")
                .passwordHash("$2a$10$hashedPassword")
                .nombre("Juan")
                .apellido("Perez")
                .empresa(testEmpresa)
                .rol(TipoRol.ADMIN)
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_withValidCredentials_shouldReturnLoginResponse() {
            // Given
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token-123");

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token-123");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo("admin@frutascolombia.com");
            assertThat(response.getUser().getNombre()).isEqualTo("Juan");
            assertThat(response.getUser().getEmpresaId()).isEqualTo(1L);
            assertThat(response.getUser().getEmpresaNombre()).isEqualTo("Frutas Colombia S.A.S");

            verify(userRepository).save(any(User.class)); // Verify ultimo acceso updated
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not found")
        void login_withNonExistentEmail_shouldThrowUnauthorizedException() {
            // Given
            LoginRequest request = new LoginRequest("unknown@email.com", "password");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Credenciales inválidas");

            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when password is incorrect")
        void login_withIncorrectPassword_shouldThrowUnauthorizedException() {
            // Given
            testUser.setIntentosFallidos(0);
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "wrongpassword");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Credenciales inválidas");

            verify(jwtUtil, never()).generateToken(any());
            // Now saves because we track failed attempts
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is inactive")
        void login_withInactiveUser_shouldThrowForbiddenException() {
            // Given
            testUser.setActivo(false);
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Usuario inactivo. Contacte al administrador");

            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should update ultimo acceso on successful login")
        void login_success_shouldUpdateUltimoAcceso() {
            // Given
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

            // When
            authService.login(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getUltimoAcceso()).isNotNull();
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void login_success_shouldResetFailedAttempts() {
            // Given
            testUser.setIntentosFallidos(3);
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

            // When
            authService.login(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIntentosFallidos()).isEqualTo(0);
            assertThat(userCaptor.getValue().getBloqueadoHasta()).isNull();
        }
    }

    @Nested
    @DisplayName("Failed Login Attempt Lockout Tests")
    class FailedLoginLockoutTests {

        @Test
        @DisplayName("Should increment failed attempts on wrong password")
        void login_withWrongPassword_shouldIncrementFailedAttempts() {
            // Given
            testUser.setIntentosFallidos(0);
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "wrongpassword");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Credenciales inválidas");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIntentosFallidos()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should show remaining attempts on failed login")
        void login_withWrongPassword_shouldShowRemainingAttempts() {
            // Given
            testUser.setIntentosFallidos(2);
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "wrongpassword");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Intentos restantes: 2");
        }

        @Test
        @DisplayName("Should lock account after 5 failed attempts")
        void login_afterMaxFailedAttempts_shouldLockAccount() {
            // Given
            testUser.setIntentosFallidos(4); // One more attempt will lock
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "wrongpassword");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Cuenta bloqueada temporalmente");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIntentosFallidos()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject login when account is temporarily locked")
        void login_whenAccountLocked_shouldThrowForbiddenException() {
            // Given
            testUser.setIntentosFallidos(5);
            testUser.setBloqueadoHasta(LocalDateTime.now().plusMinutes(30));
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("Cuenta bloqueada temporalmente");

            // Password should not even be checked when locked
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should allow login after lock expires")
        void login_afterLockExpires_shouldAllowLogin() {
            // Given - Lock expired 1 minute ago
            testUser.setIntentosFallidos(5);
            testUser.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).thenReturn(true);
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");

            // Verify failed attempts were reset
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIntentosFallidos()).isEqualTo(0);
            assertThat(userCaptor.getValue().getBloqueadoHasta()).isNull();
        }
    }

    @Nested
    @DisplayName("Password Reset Request Tests")
    class PasswordResetRequestTests {

        @Test
        @DisplayName("Should generate reset token for valid email")
        void requestPasswordReset_withValidEmail_shouldGenerateToken() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

            // When
            String token = authService.requestPasswordReset(request);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            verify(resetTokenRepository).deleteByUserId(testUser.getId());
            verify(resetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when email not found")
        void requestPasswordReset_withUnknownEmail_shouldThrowResourceNotFoundException() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("unknown@email.com");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.requestPasswordReset(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No se encontró un usuario con el email proporcionado");

            verify(resetTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ForbiddenException for inactive user")
        void requestPasswordReset_withInactiveUser_shouldThrowForbiddenException() {
            // Given
            testUser.setActivo(false);
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.requestPasswordReset(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Usuario inactivo. Contacte al administrador");

            verify(resetTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should delete previous tokens before creating new one")
        void requestPasswordReset_shouldDeletePreviousTokens() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

            // When
            authService.requestPasswordReset(request);

            // Then - Verify deletion happens before save
            var inOrder = inOrder(resetTokenRepository);
            inOrder.verify(resetTokenRepository).deleteByUserId(testUser.getId());
            inOrder.verify(resetTokenRepository).save(any(PasswordResetToken.class));
        }

        @Test
        @DisplayName("Should create token with 1 hour expiration")
        void requestPasswordReset_shouldCreateTokenWith1HourExpiration() {
            // Given
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

            // When
            authService.requestPasswordReset(request);

            // Then
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(resetTokenRepository).save(tokenCaptor.capture());
            PasswordResetToken savedToken = tokenCaptor.getValue();

            assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());
            assertThat(savedToken.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(2));
            assertThat(savedToken.getUsed()).isFalse();
            assertThat(savedToken.getUser()).isEqualTo(testUser);
        }
    }

    @Nested
    @DisplayName("Password Reset Confirm Tests")
    class PasswordResetConfirmTests {

        private PasswordResetToken validToken;

        @BeforeEach
        void setUp() {
            validToken = PasswordResetToken.builder()
                    .id(1L)
                    .token("valid-token-123")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
        }

        @Test
        @DisplayName("Should reset password with valid token")
        void confirmPasswordReset_withValidToken_shouldUpdatePassword() {
            // Given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-token-123");
            request.setNewPassword("newPassword123");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode(request.getNewPassword())).thenReturn("$2a$10$newHashedPassword");

            // When
            authService.confirmPasswordReset(request);

            // Then
            verify(userRepository).save(any(User.class));
            verify(resetTokenRepository).save(any(PasswordResetToken.class));
            assertThat(validToken.getUsed()).isTrue();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for invalid token")
        void confirmPasswordReset_withInvalidToken_shouldThrowResourceNotFoundException() {
            // Given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("invalid-token");
            request.setNewPassword("newPassword");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.confirmPasswordReset(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Token de recuperación inválido");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for already used token")
        void confirmPasswordReset_withUsedToken_shouldThrowIllegalArgumentException() {
            // Given
            validToken.setUsed(true);
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-token-123");
            request.setNewPassword("newPassword");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(validToken));

            // When/Then
            assertThatThrownBy(() -> authService.confirmPasswordReset(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Este token ya fue utilizado");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for expired token")
        void confirmPasswordReset_withExpiredToken_shouldThrowIllegalArgumentException() {
            // Given
            validToken.setExpiryDate(LocalDateTime.now().minusHours(1));
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-token-123");
            request.setNewPassword("newPassword");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(validToken));

            // When/Then
            assertThatThrownBy(() -> authService.confirmPasswordReset(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Este token ha expirado");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode new password before saving")
        void confirmPasswordReset_shouldEncodePassword() {
            // Given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-token-123");
            request.setNewPassword("newPlainPassword");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode("newPlainPassword")).thenReturn("$2a$10$encodedNewPassword");

            // When
            authService.confirmPasswordReset(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("$2a$10$encodedNewPassword");
        }

        @Test
        @DisplayName("Should mark token as used after successful reset")
        void confirmPasswordReset_shouldMarkTokenAsUsed() {
            // Given
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-token-123");
            request.setNewPassword("newPassword");

            when(resetTokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(validToken));
            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

            // When
            authService.confirmPasswordReset(request);

            // Then
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(resetTokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getUsed()).isTrue();
        }
    }
}
