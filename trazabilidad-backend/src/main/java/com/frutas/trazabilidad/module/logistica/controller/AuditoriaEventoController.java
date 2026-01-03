package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.module.logistica.dto.AuditoriaEventoResponse;
import com.frutas.trazabilidad.module.logistica.service.AuditoriaEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaEventoController {

    private final AuditoriaEventoService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaEventoResponse>> listar() {
        List<AuditoriaEventoResponse> eventos = auditoriaService.listarPorEmpresa();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/entidad/{tipoEntidad}/{entidadId}")
    public ResponseEntity<List<AuditoriaEventoResponse>> listarPorEntidad(
            @PathVariable String tipoEntidad,
            @PathVariable Long entidadId) {
        List<AuditoriaEventoResponse> eventos = auditoriaService.listarPorEntidad(tipoEntidad, entidadId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/blockchain")
    public ResponseEntity<List<AuditoriaEventoResponse>> listarCadenaBlockchain() {
        List<AuditoriaEventoResponse> cadena = auditoriaService.listarCadenaBlockchain();
        return ResponseEntity.ok(cadena);
    }

    @GetMapping("/blockchain/validar")
    public ResponseEntity<Map<String, Boolean>> validarIntegridadCadena() {
        boolean valida = auditoriaService.validarIntegridadCadena();
        return ResponseEntity.ok(Map.of("integridadValida", valida));
    }
}