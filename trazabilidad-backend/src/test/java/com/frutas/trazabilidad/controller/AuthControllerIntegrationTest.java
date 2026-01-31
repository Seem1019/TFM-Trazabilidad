package com.frutas.trazabilidad.controller;

import com.frutas.trazabilidad.dto.LoginRequest;
import com.frutas.trazabilidad.dto.LoginResponse;
import com.frutas.trazabilidad.dto.PasswordResetConfirmRequest;
import com.frutas.trazabilidad.dto.PasswordResetRequest;
import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.Empresa;
import com.frutas.trazabilidad.entity.PasswordResetToken;
import com.frutas.trazabilidad.entity.TipoRol;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.EmpresaRepository;
import com.frutas.trazabilidad.repository.PasswordResetTokenRepository;
import com.frutas.trazabilidad.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AuthController.
 * Tests the complete authentication flow by calling the controller methods directly.
 * This approach avoids MockMvc dependencies that may not be available in Spring Boot 4.x.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Empresa testEmpresa;
    private User testUser;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Create mock HttpServletRequest
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Test-Agent");
        mockRequest = request;

        // Use JdbcTemplate to delete all data in correct order (respecting foreign keys)
        // JdbcTemplate handles transactions automatically
        // Order matters: child tables before parent tables
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM password_reset_tokens");
        jdbcTemplate.execute("DELETE FROM auditoria_eventos");
        jdbcTemplate.execute("DELETE FROM documentos_exportacion");
        jdbcTemplate.execute("DELETE FROM eventos_logisticos");
        jdbcTemplate.execute("DELETE FROM controles_calidad");
        jdbcTemplate.execute("DELETE FROM etiquetas_pallets");
        jdbcTemplate.execute("DELETE FROM etiquetas");
        jdbcTemplate.execute("DELETE FROM pallets");
        jdbcTemplate.execute("DELETE FROM clasificaciones");
        jdbcTemplate.execute("DELETE FROM recepciones_planta");
        jdbcTemplate.execute("DELETE FROM cosechas");
        jdbcTemplate.execute("DELETE FROM actividades_agronomicas");
        jdbcTemplate.execute("DELETE FROM lotes");
        jdbcTemplate.execute("DELETE FROM certificaciones");
        jdbcTemplate.execute("DELETE FROM fincas");
        jdbcTemplate.execute("DELETE FROM envios");
        jdbcTemplate.execute("DELETE FROM usuarios");
        jdbcTemplate.execute("DELETE FROM empresas");

        // Create test empresa
        testEmpresa = Empresa.builder()
                .nit("900123456-1")
                .razonSocial("Frutas Colombia S.A.S")
                .nombreComercial("Frutas Colombia")
                .email("info@frutascolombia.com")
                .activo(true)
                .build();
        testEmpresa = empresaRepository.saveAndFlush(testEmpresa);

        // Create test user
        testUser = User.builder()
                .email("admin@frutascolombia.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .nombre("Juan")
                .apellido("Perez")
                .empresa(testEmpresa)
                .rol(TipoRol.ADMIN)
                .activo(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return OK for health check")
        void health_shouldReturnOk() {
            ResponseEntity<ApiResponse<String>> response = authController.health();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Servidor operativo");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_withValidCredentials_shouldReturnToken() {
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");

            ResponseEntity<ApiResponse<LoginResponse>> response = authController.login(request, mockRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Login exitoso");
            assertThat(response.getBody().getData()).isNotNull();
            assertThat(response.getBody().getData().getAccessToken()).isNotNull();
            assertThat(response.getBody().getData().getRefreshToken()).isNotNull();
            assertThat(response.getBody().getData().getUser().getEmail()).isEqualTo("admin@frutascolombia.com");
            assertThat(response.getBody().getData().getUser().getNombre()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Should throw exception for invalid email")
        void login_withInvalidEmail_shouldThrowException() {
            LoginRequest request = new LoginRequest("wrong@email.com", "password123");

            try {
                authController.login(request, mockRequest);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - invalid credentials should throw exception
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw exception for invalid password")
        void login_withInvalidPassword_shouldThrowException() {
            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "wrongpassword");

            try {
                authController.login(request, mockRequest);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - invalid credentials should throw exception
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw exception for inactive user")
        void login_withInactiveUser_shouldThrowException() {
            testUser.setActivo(false);
            userRepository.save(testUser);

            LoginRequest request = new LoginRequest("admin@frutascolombia.com", "password123");

            try {
                authController.login(request, mockRequest);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - inactive user should throw exception
                assertThat(e).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Password Reset Request Tests")
    class PasswordResetRequestTests {

        @Test
        @DisplayName("Should generate reset token for valid email")
        void requestPasswordReset_withValidEmail_shouldReturnToken() {
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");

            ResponseEntity<ApiResponse<String>> response = authController.requestPasswordReset(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
            assertThat(response.getBody().getData()).isNotNull();
            assertThat(response.getBody().getData()).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw exception for unknown email")
        void requestPasswordReset_withUnknownEmail_shouldThrowException() {
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("unknown@email.com");

            try {
                authController.requestPasswordReset(request);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - unknown email should throw exception
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw exception for inactive user")
        void requestPasswordReset_withInactiveUser_shouldThrowException() {
            testUser.setActivo(false);
            userRepository.save(testUser);

            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail("admin@frutascolombia.com");

            try {
                authController.requestPasswordReset(request);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - inactive user should throw exception
                assertThat(e).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Password Reset Confirm Tests")
    class PasswordResetConfirmTests {

        @Test
        @DisplayName("Should reset password with valid token")
        void confirmPasswordReset_withValidToken_shouldResetPassword() {
            // Create reset token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("valid-test-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("valid-test-token");
            request.setNewPassword("newPassword123");

            ResponseEntity<ApiResponse<Void>> response = authController.confirmPasswordReset(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Contraseña actualizada exitosamente");

            // Verify can login with new password
            LoginRequest loginRequest = new LoginRequest("admin@frutascolombia.com", "newPassword123");
            ResponseEntity<ApiResponse<LoginResponse>> loginResponse = authController.login(loginRequest, mockRequest);

            assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(loginResponse.getBody()).isNotNull();
            assertThat(loginResponse.getBody().getSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void confirmPasswordReset_withInvalidToken_shouldThrowException() {
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("invalid-token");
            request.setNewPassword("newPassword123");

            try {
                authController.confirmPasswordReset(request);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - invalid token should throw exception
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw exception for already used token")
        void confirmPasswordReset_withUsedToken_shouldThrowException() {
            // Create used token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("used-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(true)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("used-token");
            request.setNewPassword("newPassword123");

            try {
                authController.confirmPasswordReset(request);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - used token should throw exception
                assertThat(e).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void confirmPasswordReset_withExpiredToken_shouldThrowException() {
            // Create expired token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(LocalDateTime.now().minusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
            request.setToken("expired-token");
            request.setNewPassword("newPassword123");

            try {
                authController.confirmPasswordReset(request);
                assertThat(false).as("Should have thrown exception").isTrue();
            } catch (Exception e) {
                // Expected - expired token should throw exception
                assertThat(e).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Complete Authentication Flow Tests")
    class CompleteFlowTests {

        @Test
        @DisplayName("Should complete full password reset flow")
        void completePasswordResetFlow_shouldWork() {
            // Step 1: Request password reset
            PasswordResetRequest resetRequest = new PasswordResetRequest();
            resetRequest.setEmail("admin@frutascolombia.com");

            ResponseEntity<ApiResponse<String>> resetResponse = authController.requestPasswordReset(resetRequest);

            assertThat(resetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resetResponse.getBody()).isNotNull();
            String token = resetResponse.getBody().getData();
            assertThat(token).isNotNull();

            // Step 2: Confirm password reset
            PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest();
            confirmRequest.setToken(token);
            confirmRequest.setNewPassword("brandNewPassword123");

            ResponseEntity<ApiResponse<Void>> confirmResponse = authController.confirmPasswordReset(confirmRequest);

            assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Step 3: Login with new password
            LoginRequest loginRequest = new LoginRequest("admin@frutascolombia.com", "brandNewPassword123");

            ResponseEntity<ApiResponse<LoginResponse>> loginResponse = authController.login(loginRequest, mockRequest);

            assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(loginResponse.getBody()).isNotNull();
            assertThat(loginResponse.getBody().getData()).isNotNull();
            assertThat(loginResponse.getBody().getData().getAccessToken()).isNotNull();

            // Step 4: Old password should not work
            LoginRequest oldLoginRequest = new LoginRequest("admin@frutascolombia.com", "password123");

            try {
                authController.login(oldLoginRequest, mockRequest);
                assertThat(false).as("Should have thrown exception for old password").isTrue();
            } catch (Exception e) {
                // Expected - old password should no longer work
                assertThat(e).isNotNull();
            }
        }
    }
}
