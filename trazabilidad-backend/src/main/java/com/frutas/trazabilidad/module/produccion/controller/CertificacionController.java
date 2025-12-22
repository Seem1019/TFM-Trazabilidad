package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.module.produccion.dto.CertificacionRequest;
import com.frutas.trazabilidad.module.produccion.dto.CertificacionResponse;
import com.frutas.trazabilidad.module.produccion.service.CertificacionService;
import com.frutas.trazabilidad.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de certificaciones.
 */
@RestController
@RequestMapping("/api/certificaciones")
@RequiredArgsConstructor
@Tag(name = "Certificaciones", description = "Gestión de certificaciones de fincas")
public class CertificacionController {

    private final CertificacionService certificacionService;
    private final JwtUtil jwtUtil;

    @GetMapping("/finca/{fincaId}")
    @Operation(summary = "Listar certificaciones por finca", description = "Obtiene todas las certificaciones de una finca")
    public ResponseEntity<ApiResponse<List<CertificacionResponse>>> listarPorFinca(
            @PathVariable Long fincaId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CertificacionResponse> certificaciones = certificacionService.listarPorFinca(fincaId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(certificaciones, "Certificaciones obtenidas exitosamente"));
    }

    @GetMapping("/finca/{fincaId}/vigentes")
    @Operation(summary = "Listar certificaciones vigentes", description = "Obtiene las certificaciones vigentes de una finca")
    public ResponseEntity<ApiResponse<List<CertificacionResponse>>> listarVigentes(
            @PathVariable Long fincaId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CertificacionResponse> certificaciones = certificacionService.listarVigentesPorFinca(fincaId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(certificaciones, "Certificaciones vigentes obtenidas"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener certificación por ID", description = "Obtiene los detalles de una certificación específica")
    public ResponseEntity<ApiResponse<CertificacionResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CertificacionResponse certificacion = certificacionService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(certificacion, "Certificación encontrada"));
    }

    @PostMapping
    @Operation(summary = "Crear certificación", description = "Crea una nueva certificación para una finca")
    public ResponseEntity<ApiResponse<CertificacionResponse>> crear(
            @Valid @RequestBody CertificacionRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CertificacionResponse certificacion = certificacionService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(certificacion, "Certificación creada exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar certificación", description = "Actualiza los datos de una certificación existente")
    public ResponseEntity<ApiResponse<CertificacionResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CertificacionRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CertificacionResponse certificacion = certificacionService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(certificacion, "Certificación actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar certificación", description = "Elimina (desactiva) una certificación")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        certificacionService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Certificación eliminada exitosamente"));
    }

    @GetMapping("/proximas-vencer")
    @Operation(summary = "Certificaciones próximas a vencer", description = "Obtiene certificaciones que vencen en los próximos 30 días")
    public ResponseEntity<ApiResponse<List<CertificacionResponse>>> proximasAVencer(
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CertificacionResponse> certificaciones = certificacionService.listarProximasAVencer(empresaId);
        return ResponseEntity.ok(ApiResponse.success(certificaciones, "Certificaciones próximas a vencer obtenidas"));
    }
}