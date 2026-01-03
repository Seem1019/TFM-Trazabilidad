package com.frutas.trazabilidad.module.logistica.controller;

import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoRequest;
import com.frutas.trazabilidad.module.logistica.dto.EventoLogisticoResponse;
import com.frutas.trazabilidad.module.logistica.service.EventoLogisticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos-logisticos")
@RequiredArgsConstructor
public class EventoLogisticoController {

    private final EventoLogisticoService eventoService;

    @PostMapping
    public ResponseEntity<EventoLogisticoResponse> crear(@Valid @RequestBody EventoLogisticoRequest request) {
        EventoLogisticoResponse response = eventoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoLogisticoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EventoLogisticoRequest request) {
        EventoLogisticoResponse response = eventoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/envio/{envioId}")
    public ResponseEntity<List<EventoLogisticoResponse>> listarPorEnvio(@PathVariable Long envioId) {
        List<EventoLogisticoResponse> eventos = eventoService.listarPorEnvio(envioId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoLogisticoResponse> obtenerPorId(@PathVariable Long id) {
        EventoLogisticoResponse evento = eventoService.obtenerPorId(id);
        return ResponseEntity.ok(evento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}