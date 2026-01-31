package com.frutas.trazabilidad.controller;

import com.frutas.trazabilidad.dto.*;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login, logout y gestión de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario y retorna access token (15 min) y refresh token (7 días)")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);

        LoginResponse response = authService.login(request, deviceInfo, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(response, "Login exitoso"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token",
            description = "Usa el refresh token para obtener un nuevo access token sin re-autenticarse")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpRequest) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);

        TokenRefreshResponse response = authService.refreshToken(request, deviceInfo, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(response, "Token renovado exitosamente"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "Revoca el refresh token actual, invalidando la sesión")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Sesión cerrada exitosamente"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Cerrar todas las sesiones",
            description = "Revoca todos los refresh tokens del usuario (logout global)")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
        authService.logoutAll(user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Todas las sesiones cerradas exitosamente"));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que el servidor esté funcionando")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Server is running", "Servidor operativo"));
    }

    @PostMapping("/password-reset/request")
    @Operation(summary = "Solicitar recuperación de contraseña",
            description = "Genera un token de recuperación y lo envía por email (en desarrollo retorna el token)")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        String token = authService.requestPasswordReset(request);
        // En desarrollo retornamos el token directamente, en producción solo confirmación
        return ResponseEntity.ok(ApiResponse.success(token,
                "Se ha generado un token de recuperación. En producción se enviaría por email"));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirmar cambio de contraseña",
            description = "Confirma el cambio de contraseña usando el token recibido. Cierra todas las sesiones activas.")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
