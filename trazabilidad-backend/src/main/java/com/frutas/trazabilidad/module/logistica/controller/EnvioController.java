package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.logistica.dto.EnvioRequest;
import com.frutas.trazabilidad.module.logistica.dto.EnvioResponse;
import com.frutas.trazabilidad.module.logistica.service.EnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de envíos.
 * Implementa RBAC y aislamiento multitenant.
 */
@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Gestión de envíos y logística de exportación")
public class EnvioController {

    private final EnvioService envioService;

    @PostMapping
    @Operation(summary = "Crear envío", description = "Crea un nuevo envío de exportación")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EnvioResponse>> crear(
            @Valid @RequestBody EnvioRequest request,
            @AuthenticationPrincipal User user) {
        EnvioResponse response = envioService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Envío creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar envío", description = "Actualiza los datos de un envío existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EnvioResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EnvioRequest request,
            @AuthenticationPrincipal User user) {
        EnvioResponse response = envioService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Envío actualizado exitosamente"));
    }

    @PostMapping("/{id}/pallets")
    @Operation(summary = "Asignar pallets al envío", description = "Asigna una lista de pallets a un envío")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<EnvioResponse>> asignarPallets(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body,
            @AuthenticationPrincipal User user) {
        List<Long> palletsIds = body.get("palletsIds");
        EnvioResponse response = envioService.asignarPallets(id, palletsIds, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Pallets asignados exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del envío", description = "Actualiza el estado de un envío")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EnvioResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String estado = body.get("estado");
        EnvioResponse response = envioService.cambiarEstado(id, estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Estado actualizado exitosamente"));
    }

    @PostMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar envío", description = "Cierra un envío, generando hash de integridad")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<EnvioResponse>> cerrar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EnvioResponse response = envioService.cerrar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Envío cerrado exitosamente"));
    }

    @GetMapping
    @Operation(summary = "Listar envíos", description = "Lista todos los envíos de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EnvioResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<EnvioResponse> envios = envioService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(envios, "Envíos obtenidos exitosamente"));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar envíos por estado", description = "Lista envíos filtrados por estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<EnvioResponse>>> listarPorEstado(
            @PathVariable String estado,
            @AuthenticationPrincipal User user) {
        List<EnvioResponse> envios = envioService.listarPorEstado(estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(envios, "Envíos por estado obtenidos"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener envío por ID", description = "Obtiene los detalles de un envío específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<EnvioResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EnvioResponse envio = envioService.obtenerPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(envio, "Envío obtenido exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar envío", description = "Elimina (desactiva) un envío")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        envioService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Envío eliminado exitosamente"));
    }
}
