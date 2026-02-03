package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.module.logistica.dto.AuditoriaEventoResponse;
import com.frutas.trazabilidad.module.logistica.service.AuditoriaEventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para consulta de auditoría y trazabilidad interna.
 * RBAC: ADMIN=R, PRODUCTOR=R, OPERADOR_PLANTA=R, OPERADOR_LOGISTICA=R, AUDITOR=R
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta de eventos de auditoría y trazabilidad interna")
public class AuditoriaEventoController {

    private final AuditoriaEventoService auditoriaService;

    @GetMapping
    @Operation(summary = "Listar eventos de auditoría", description = "Lista todos los eventos de auditoría de la empresa")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<List<AuditoriaEventoResponse>> listar() {
        List<AuditoriaEventoResponse> eventos = auditoriaService.listarPorEmpresa();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/entidad/{tipoEntidad}/{entidadId}")
    @Operation(summary = "Listar eventos por entidad", description = "Lista eventos de una entidad específica")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<List<AuditoriaEventoResponse>> listarPorEntidad(
            @PathVariable String tipoEntidad,
            @PathVariable Long entidadId) {
        List<AuditoriaEventoResponse> eventos = auditoriaService.listarPorEntidad(tipoEntidad, entidadId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/blockchain")
    @Operation(summary = "Listar cadena blockchain", description = "Lista la cadena de bloques de auditoría")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<List<AuditoriaEventoResponse>> listarCadenaBlockchain() {
        List<AuditoriaEventoResponse> cadena = auditoriaService.listarCadenaBlockchain();
        return ResponseEntity.ok(cadena);
    }

    @GetMapping("/blockchain/validar")
    @Operation(summary = "Validar integridad", description = "Valida la integridad de la cadena blockchain")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
    public ResponseEntity<Map<String, Boolean>> validarIntegridadCadena() {
        boolean valida = auditoriaService.validarIntegridadCadena();
        return ResponseEntity.ok(Map.of("integridadValida", valida));
    }
}
