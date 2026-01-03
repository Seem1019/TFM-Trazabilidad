package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionRequest;
import com.frutas.trazabilidad.module.logistica.dto.DocumentoExportacionResponse;
import com.frutas.trazabilidad.module.logistica.service.DocumentoExportacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documentos-exportacion")
@RequiredArgsConstructor
public class DocumentoExportacionController {

    private final DocumentoExportacionService documentoService;

    @PostMapping
    public ResponseEntity<DocumentoExportacionResponse> crear(@Valid @RequestBody DocumentoExportacionRequest request) {
        DocumentoExportacionResponse response = documentoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoExportacionResponse> actualizar(@PathVariable Long id, @Valid @RequestBody DocumentoExportacionRequest request) {
        DocumentoExportacionResponse response = documentoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DocumentoExportacionResponse> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String estado = body.get("estado");
        DocumentoExportacionResponse response = documentoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/envio/{envioId}")
    public ResponseEntity<List<DocumentoExportacionResponse>> listarPorEnvio(@PathVariable Long envioId) {
        List<DocumentoExportacionResponse> documentos = documentoService.listarPorEnvio(envioId);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoExportacionResponse> obtenerPorId(@PathVariable Long id) {
        DocumentoExportacionResponse documento = documentoService.obtenerPorId(id);
        return ResponseEntity.ok(documento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        documentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}