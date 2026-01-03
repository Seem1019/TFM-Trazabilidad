package com.frutas.trazabilidad.module.empaque.controller;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadRequest;
import com.frutas.trazabilidad.module.empaque.dto.ControlCalidadResponse;
import com.frutas.trazabilidad.module.empaque.service.ControlCalidadService;
import com.frutas.trazabilidad.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para gestión de controles de calidad.
 */
@RestController
@RequestMapping("/api/controles-calidad")
@RequiredArgsConstructor
public class ControlCalidadController {

    private final ControlCalidadService controlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listar(
            @AuthenticationPrincipal User user) {
        List<ControlCalidadResponse> controles = controlService.listarPorEmpresa(user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles de calidad obtenidos exitosamente"));
    }

    @GetMapping("/clasificacion/{clasificacionId}")
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listarPorClasificacion(
            @PathVariable Long clasificacionId,
            @AuthenticationPrincipal User user) {
        List<ControlCalidadResponse> controles = controlService.listarPorClasificacion(clasificacionId, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles de clasificación obtenidos"));
    }

    @GetMapping("/pallet/{palletId}")
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listarPorPallet(
            @PathVariable Long palletId) {
        List<ControlCalidadResponse> controles = controlService.listarPorPallet(palletId);
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles de pallet obtenidos"));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listarPorTipo(
            @PathVariable String tipo,
            @AuthenticationPrincipal User user) {
        List<ControlCalidadResponse> controles = controlService.listarPorTipo(user.getEmpresa().getId(), tipo);
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles por tipo obtenidos"));
    }

    @GetMapping("/resultado/{resultado}")
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listarPorResultado(
            @PathVariable String resultado,
            @AuthenticationPrincipal User user) {
        List<ControlCalidadResponse> controles = controlService.listarPorResultado(user.getEmpresa().getId(), resultado);
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles por resultado obtenidos"));
    }

    @GetMapping("/rango-fechas")
    public ResponseEntity<ApiResponse<List<ControlCalidadResponse>>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal User user) {
        List<ControlCalidadResponse> controles = controlService.listarPorRangoFechas(
                user.getEmpresa().getId(), desde, hasta);
        return ResponseEntity.ok(ApiResponse.success(controles, "Controles por rango de fechas obtenidos"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ControlCalidadResponse>> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ControlCalidadResponse control = controlService.buscarPorId(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(control, "Control de calidad obtenido exitosamente"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ControlCalidadResponse>> crear(
            @Valid @RequestBody ControlCalidadRequest request,
            @AuthenticationPrincipal User user) {
        ControlCalidadResponse control = controlService.crear(request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(control, "Control de calidad creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ControlCalidadResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ControlCalidadRequest request,
            @AuthenticationPrincipal User user) {
        ControlCalidadResponse control = controlService.actualizar(id, request, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(control, "Control de calidad actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        controlService.eliminar(id, user.getEmpresa().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Control de calidad eliminado exitosamente"));
    }
}