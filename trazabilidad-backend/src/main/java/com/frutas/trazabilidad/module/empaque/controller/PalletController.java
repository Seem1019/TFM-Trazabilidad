package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.dto.PalletRequest;
import com.frutas.trazabilidad.module.empaque.dto.PalletResponse;
import com.frutas.trazabilidad.module.empaque.service.PalletService;
import com.frutas.trazabilidad.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de pallets.
 * Todos los endpoints filtran por empresa del usuario autenticado.
 */
@RestController
@RequestMapping("/api/pallets")
@RequiredArgsConstructor
@Tag(name = "Pallets", description = "Gestión de pallets para empaque")
public class PalletController {

    private final PalletService palletService;

    @GetMapping
    @Operation(summary = "Listar pallets", description = "Lista todos los pallets de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<PalletResponse> pallets = palletService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets obtenidos exitosamente"));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar pallets por estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorEstado(
            @PathVariable String estado,
            @AuthenticationPrincipal User user) {
        List<PalletResponse> pallets = palletService.listarPorEstado(user.getEmpresa().getId(), estado);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por estado obtenidos"));
    }

    @GetMapping("/destino")
    @Operation(summary = "Listar pallets por destino")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorDestino(
            @RequestParam String destino,
            @AuthenticationPrincipal User user) {
        List<PalletResponse> pallets = palletService.listarPorDestino(user.getEmpresa().getId(), destino);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por destino obtenidos"));
    }

    @GetMapping("/tipo-fruta/{tipoFruta}")
    @Operation(summary = "Listar pallets por tipo de fruta")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarPorTipoFruta(
            @PathVariable String tipoFruta,
            @AuthenticationPrincipal User user) {
        List<PalletResponse> pallets = palletService.listarPorTipoFruta(user.getEmpresa().getId(), tipoFruta);
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets por tipo de fruta obtenidos"));
    }

    @GetMapping("/listos-envio")
    @Operation(summary = "Listar pallets listos para envío")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<List<PalletResponse>>> listarListosParaEnvio(
            @AuthenticationPrincipal User user) {
        List<PalletResponse> pallets = palletService.listarListosParaEnvio(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallets, "Pallets listos para envío obtenidos"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pallet por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<PalletResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        PalletResponse pallet = palletService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet obtenido exitosamente"));
    }

    @PostMapping
    @Operation(summary = "Crear pallet")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<PalletResponse>> crear(
            @Valid @RequestBody PalletRequest request,
            @AuthenticationPrincipal User user) {
        PalletResponse pallet = palletService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pallet")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
    public ResponseEntity<ApiResponse<PalletResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PalletRequest request,
            @AuthenticationPrincipal User user) {
        PalletResponse pallet = palletService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallet, "Pallet actualizado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del pallet")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA')")
    public ResponseEntity<ApiResponse<PalletResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @AuthenticationPrincipal User user) {
        PalletResponse pallet = palletService.cambiarEstado(id, estado, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(pallet, "Estado actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pallet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        palletService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Pallet eliminado exitosamente"));
    }
}
