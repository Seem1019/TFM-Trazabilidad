package com.frutas.trazabilidad.service;

import com.frutas.trazabilidad.module.logistica.service.AuditoriaEventoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener para eventos de auditoría de usuarios.
 * Se ejecuta DESPUÉS de que la transacción principal se complete exitosamente,
 * evitando ConcurrentModificationException en JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserAuditEventListener {

    private final AuditoriaEventoService auditoriaService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAuditEvent(UserService.UserAuditEvent event) {
        try {
            if (event.ejecutorId() == null) {
                log.warn("No se pudo registrar auditoría de usuario: ejecutor no identificado");
                return;
            }

            String descripcion = switch (event.operacion()) {
                case "CREATE" -> "Creación de usuario: " + event.userEmail();
                case "UPDATE" -> "Actualización de usuario: " + event.userEmail();
                case "DELETE" -> "Eliminación de usuario: " + event.userEmail();
                default -> event.operacion() + " de usuario: " + event.userEmail();
            };

            switch (event.operacion()) {
                case "CREATE" -> auditoriaService.registrarCreacionAsync(
                        "USER",
                        event.userId(),
                        event.userEmail(),
                        descripcion,
                        event.ejecutorId()
                );
                case "UPDATE" -> auditoriaService.registrarActualizacionAsync(
                        "USER",
                        event.userId(),
                        event.userEmail(),
                        descripcion,
                        null,
                        null,
                        event.ejecutorId()
                );
                case "DELETE" -> auditoriaService.registrarEliminacionAsync(
                        "USER",
                        event.userId(),
                        event.userEmail(),
                        descripcion,
                        event.ejecutorId()
                );
            }

            log.debug("Evento de auditoría de usuario procesado: {} - {}", event.operacion(), event.userEmail());
        } catch (Exception e) {
            log.error("Error al procesar evento de auditoría de usuario: {}", e.getMessage());
        }
    }
}
