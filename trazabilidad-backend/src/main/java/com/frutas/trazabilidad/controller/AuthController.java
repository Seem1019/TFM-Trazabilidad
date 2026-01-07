package com.frutas.trazabilidad.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.dto.LoginRequest;
import com.frutas.trazabilidad.dto.LoginResponse;
import com.frutas.trazabilidad.dto.PasswordResetConfirmRequest;
import com.frutas.trazabilidad.dto.PasswordResetRequest;
import com.frutas.trazabilidad.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login y registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login exitoso"));
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
               description = "Confirma el cambio de contraseña usando el token recibido")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
    }
}