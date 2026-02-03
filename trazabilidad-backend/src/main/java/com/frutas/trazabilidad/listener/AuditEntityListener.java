package com.frutas.trazabilidad.listener;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.module.logistica.service.AuditoriaEventoService;
import com.frutas.trazabilidad.repository.UserRepository;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Listener JPA para auditoría automática de entidades críticas.
 * Se activa en operaciones de persistencia, actualización y eliminación.
 *
 * NOTA: Para evitar ConcurrentModificationException, las entidades de tipo
 * AuditoriaEvento y User se excluyen de la auditoría automática.
 */
@Component
@Slf4j
public class AuditEntityListener {

    private static AuditoriaEventoService auditoriaService;
    private static UserRepository userRepository;

    @Autowired
    public void init(@Lazy AuditoriaEventoService auditoriaService, @Lazy UserRepository userRepository) {
        AuditEntityListener.auditoriaService = auditoriaService;
        AuditEntityListener.userRepository = userRepository;
    }

    /**
     * Verifica si la entidad debe ser excluida de la auditoría automática
     * para evitar recursión infinita o ConcurrentModificationException.
     *
     * NOTA: User se excluye porque los JPA listeners no pueden usar @Async correctamente
     * (Spring no intercepta las llamadas dentro del contexto de Hibernate).
     * La auditoría de usuarios se maneja manualmente en UserService.
     */
    private boolean debeExcluirDeAuditoria(Object entity) {
        String className = entity.getClass().getSimpleName();
        // Excluir entidades que causan problemas
        return className.equals("AuditoriaEvento") ||
               className.equals("RefreshToken") ||
               className.equals("User");
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        if (debeExcluirDeAuditoria(entity)) {
            return;
        }

        try {
            User usuario = obtenerUsuarioActual();
            if (usuario == null) {
                log.warn("No se pudo obtener usuario para auditoría de creación");
                return;
            }

            AuditInfo info = extraerInfoEntidad(entity);

            // Usar método asíncrono para evitar ConcurrentModificationException
            auditoriaService.registrarCreacionAsync(
                    info.tipoEntidad,
                    info.id,
                    info.codigo,
                    "Creación de " + info.tipoEntidad.toLowerCase() + ": " + info.codigo,
                    usuario.getId()
            );

            log.debug("Auditoría automática: CREATE {} ID:{}", info.tipoEntidad, info.id);
        } catch (Exception e) {
            log.error("Error en auditoría automática PostPersist: {}", e.getMessage());
        }
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        if (debeExcluirDeAuditoria(entity)) {
            return;
        }

        try {
            User usuario = obtenerUsuarioActual();
            if (usuario == null) {
                log.warn("No se pudo obtener usuario para auditoría de actualización");
                return;
            }

            AuditInfo info = extraerInfoEntidad(entity);

            auditoriaService.registrarActualizacionAsync(
                    info.tipoEntidad,
                    info.id,
                    info.codigo,
                    "Actualización de " + info.tipoEntidad.toLowerCase() + ": " + info.codigo,
                    null,
                    capturarEstadoActual(entity),
                    usuario.getId()
            );

            log.debug("Auditoría automática: UPDATE {} ID:{}", info.tipoEntidad, info.id);
        } catch (Exception e) {
            log.error("Error en auditoría automática PostUpdate: {}", e.getMessage());
        }
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        if (debeExcluirDeAuditoria(entity)) {
            return;
        }

        try {
            User usuario = obtenerUsuarioActual();
            if (usuario == null) {
                log.warn("No se pudo obtener usuario para auditoría de eliminación");
                return;
            }

            AuditInfo info = extraerInfoEntidad(entity);

            auditoriaService.registrarEliminacionAsync(
                    info.tipoEntidad,
                    info.id,
                    info.codigo,
                    "Eliminación de " + info.tipoEntidad.toLowerCase() + ": " + info.codigo,
                    usuario.getId()
            );

            log.debug("Auditoría automática: DELETE {} ID:{}", info.tipoEntidad, info.id);
        } catch (Exception e) {
            log.error("Error en auditoría automática PreRemove: {}", e.getMessage());
        }
    }

    private User obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }

            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            log.warn("No se pudo obtener usuario del contexto de seguridad: {}", e.getMessage());
            return null;
        }
    }

    private AuditInfo extraerInfoEntidad(Object entity) {
        AuditInfo info = new AuditInfo();
        String className = entity.getClass().getSimpleName();

        info.tipoEntidad = determinarTipoEntidad(className);
        info.id = extraerCampo(entity, "id", Long.class);
        info.codigo = extraerCodigoEntidad(entity, className);

        return info;
    }

    private String determinarTipoEntidad(String className) {
        // Mapeo de nombres de clase a tipos de entidad
        switch (className) {
            case "Finca": return "FINCA";
            case "Lote": return "LOTE";
            case "Cosecha": return "COSECHA";
            case "ActividadAgronomica": return "ACTIVIDAD";
            case "Certificacion": return "CERTIFICACION";
            case "RecepcionPlanta": return "RECEPCION";
            case "Clasificacion": return "CLASIFICACION";
            case "Etiqueta": return "ETIQUETA";
            case "Pallet": return "PALLET";
            case "ControlCalidad": return "CONTROL_CALIDAD";
            case "Envio": return "ENVIO";
            case "EventoLogistico": return "EVENTO_LOGISTICO";
            case "DocumentoExportacion": return "DOCUMENTO";
            case "User": return "USER";
            default: return className.toUpperCase();
        }
    }

    private String extraerCodigoEntidad(Object entity, String className) {
        // Intentar extraer el campo de código específico de cada entidad
        String codigo = null;

        switch (className) {
            case "Finca":
                codigo = extraerCampo(entity, "nombre", String.class);
                break;
            case "Lote":
                codigo = extraerCampo(entity, "codigoLote", String.class);
                break;
            case "Cosecha":
                codigo = extraerCampo(entity, "codigoCosecha", String.class);
                break;
            case "Certificacion":
                codigo = extraerCampo(entity, "numeroCertificado", String.class);
                break;
            case "RecepcionPlanta":
                codigo = extraerCampo(entity, "codigoRecepcion", String.class);
                break;
            case "Clasificacion":
                codigo = extraerCampo(entity, "codigoClasificacion", String.class);
                break;
            case "Etiqueta":
                codigo = extraerCampo(entity, "codigoEtiqueta", String.class);
                break;
            case "Pallet":
                codigo = extraerCampo(entity, "codigoPallet", String.class);
                break;
            case "Envio":
                codigo = extraerCampo(entity, "codigoEnvio", String.class);
                break;
            case "User":
                codigo = extraerCampo(entity, "email", String.class);
                break;
            default:
                codigo = "ID-" + extraerCampo(entity, "id", Long.class);
        }

        return codigo != null ? codigo : "UNKNOWN";
    }

    private <T> T extraerCampo(Object entity, String fieldName, Class<T> type) {
        try {
            Field field = buscarCampo(entity.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(entity);
                return type.isInstance(value) ? type.cast(value) : null;
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer campo {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private Field buscarCampo(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private String capturarEstadoActual(Object entity) {
        // Captura simple del estado para auditoría
        return String.format("{entity: '%s', id: %s}",
                entity.getClass().getSimpleName(),
                extraerCampo(entity, "id", Long.class));
    }

    private static class AuditInfo {
        String tipoEntidad;
        Long id;
        String codigo;
    }
}
