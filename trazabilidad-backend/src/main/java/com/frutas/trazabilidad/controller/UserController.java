package com.frutas.trazabilidad.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.dto.UserRequest;
import com.frutas.trazabilidad.dto.UserResponse;
import com.frutas.trazabilidad.security.JwtUtil;
import com.frutas.trazabilidad.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de usuarios.
 * Solo accesible para usuarios con rol ADMIN.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (solo ADMIN)")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios activos", description = "Obtiene todos los usuarios activos de la empresa")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listar(@RequestHeader("Authorization") String token) {
        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<UserResponse> users = userService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success(users, "Usuarios obtenidos exitosamente"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene todos los usuarios (activos e inactivos) de la empresa")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listarTodos(@RequestHeader("Authorization") String token) {
        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<UserResponse> users = userService.listarTodosPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success(users, "Usuarios obtenidos exitosamente"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    public ResponseEntity<ApiResponse<UserResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        UserResponse user = userService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(user, "Usuario encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en la empresa")
    public ResponseEntity<ApiResponse<UserResponse>> crear(
            @Valid @RequestBody UserRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        UserResponse user = userService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Usuario creado exitosamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<ApiResponse<UserResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        UserResponse user = userService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(user, "Usuario actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina (desactiva) un usuario")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        userService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado de usuario", description = "Activa o desactiva un usuario")
    public ResponseEntity<ApiResponse<UserResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        UserResponse user = userService.cambiarEstado(id, activo, empresaId);
        return ResponseEntity.ok(ApiResponse.success(user, "Estado del usuario actualizado exitosamente"));
    }
}
