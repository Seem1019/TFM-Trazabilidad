package com.frutas.trazabilidad.module.produccion.controller;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.module.produccion.dto.CosechaRequest;
import com.frutas.trazabilidad.module.produccion.dto.CosechaResponse;
import com.frutas.trazabilidad.module.produccion.service.CosechaService;
import com.frutas.trazabilidad.security.JwtUtil;
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
 * Controlador REST para gestión de cosechas.
 */
@RestController
@RequestMapping("/api/cosechas")
@RequiredArgsConstructor
@Tag(name = "Cosechas", description = "Gestión de cosechas")
public class CosechaController {

    private final CosechaService cosechaService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Listar todas las cosechas", description = "Obtiene todas las cosechas de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<CosechaResponse>>> listar(@RequestHeader("Authorization") String token) {
        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CosechaResponse> cosechas = cosechaService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success(cosechas, "Cosechas obtenidas exitosamente"));
    }

    @GetMapping("/lote/{loteId}")
    @Operation(summary = "Listar cosechas por lote", description = "Obtiene todas las cosechas de un lote específico")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<CosechaResponse>>> listarPorLote(
            @PathVariable Long loteId,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CosechaResponse> cosechas = cosechaService.listarPorLote(loteId, empresaId);
        return ResponseEntity.ok(ApiResponse.success(cosechas, "Cosechas del lote obtenidas exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cosecha por ID", description = "Obtiene los detalles de una cosecha específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<CosechaResponse>> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CosechaResponse cosecha = cosechaService.buscarPorId(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(cosecha, "Cosecha encontrada"));
    }

    @PostMapping
    @Operation(summary = "Registrar cosecha", description = "Registra una nueva cosecha")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<CosechaResponse>> registrar(
            @Valid @RequestBody CosechaRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CosechaResponse cosecha = cosechaService.registrar(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cosecha, "Cosecha registrada exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cosecha", description = "Actualiza los datos de una cosecha existente")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR')")
    public ResponseEntity<ApiResponse<CosechaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CosechaRequest request,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        CosechaResponse cosecha = cosechaService.actualizar(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(cosecha, "Cosecha actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cosecha", description = "Elimina (desactiva) una cosecha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        cosechaService.eliminar(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cosecha eliminada exitosamente"));
    }

    @GetMapping("/recientes")
    @Operation(summary = "Cosechas recientes", description = "Obtiene las cosechas de los últimos 30 días")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<CosechaResponse>>> recientes(
            @RequestHeader("Authorization") String token) {

        Long empresaId = jwtUtil.extractEmpresaId(token.substring(7));
        List<CosechaResponse> cosechas = cosechaService.listarRecientes(empresaId);
        return ResponseEntity.ok(ApiResponse.success(cosechas, "Cosechas recientes obtenidas"));
    }
}