package com.frutas.trazabilidad.exception;

import com.frutas.trazabilidad.dto.ApiResponse;
import com.frutas.trazabilidad.dto.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== Excepciones de negocio ====================

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex, HttpServletRequest request) {
        log.warn("CustomException: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    // ==================== Validación ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();

            errors.add(ErrorDetail.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });

        // Construir mensaje legible con los errores específicos
        String mensajeDetallado = errors.stream()
                .map(e -> e.getMessage())
                .collect(Collectors.joining(". "));

        log.warn("Validation error - Path: {} - Errors: {}", request.getRequestURI(), mensajeDetallado);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensajeDetallado)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            // Extraer solo el nombre del campo (sin el prefijo del método)
            if (fieldName.contains(".")) {
                fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
            }
            errors.add(ErrorDetail.builder()
                    .field(fieldName)
                    .message(violation.getMessage())
                    .rejectedValue(violation.getInvalidValue())
                    .build());
        });

        String mensajeDetallado = errors.stream()
                .map(ErrorDetail::getMessage)
                .collect(Collectors.joining(". "));

        log.warn("Constraint violation - Path: {} - Errors: {}", request.getRequestURI(), mensajeDetallado);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensajeDetallado)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ==================== Seguridad ====================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials attempt - Path: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("Credenciales inválidas. Verifique su usuario y contraseña.")
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied - Path: {} - User: {}", request.getRequestURI(), MDC.get("userId"));

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("No tiene permisos para realizar esta acción. Contacte al administrador si cree que es un error.")
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ==================== Base de datos ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String mensaje = extraerMensajeIntegridad(ex);
        log.warn("Data integrity violation - Path: {} - Detail: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionException(TransactionSystemException ex, HttpServletRequest request) {
        // Intentar extraer la causa raíz
        Throwable causa = ex.getRootCause();
        String mensaje;

        if (causa instanceof ConstraintViolationException cve) {
            // Delegar al handler de ConstraintViolation
            return handleConstraintViolation(cve, request);
        }

        if (causa != null) {
            mensaje = "Error al guardar los datos: " + traducirMensajeError(causa.getMessage());
        } else {
            mensaje = "Error al procesar la transacción. Intente nuevamente.";
        }

        log.error("Transaction error - Path: {} - Cause: {}", request.getRequestURI(),
                causa != null ? causa.getMessage() : ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ==================== Request malformado ====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String mensaje = "El formato de los datos enviados no es válido. Verifique los campos e intente nuevamente.";

        // Intentar dar más detalle sobre el error de parsing
        String causa = ex.getMostSpecificCause().getMessage();
        if (causa != null) {
            if (causa.contains("LocalDate")) {
                mensaje = "Formato de fecha inválido. Use el formato AAAA-MM-DD (ejemplo: 2026-01-15).";
            } else if (causa.contains("LocalDateTime")) {
                mensaje = "Formato de fecha y hora inválido.";
            } else if (causa.contains("Double") || causa.contains("Integer") || causa.contains("Long") || causa.contains("Number")) {
                mensaje = "Se esperaba un valor numérico en uno de los campos. Verifique los datos ingresados.";
            } else if (causa.contains("enum") || causa.contains("Enum")) {
                mensaje = "Uno de los valores seleccionados no es válido.";
            }
        }

        log.warn("Message not readable - Path: {} - Detail: {}", request.getRequestURI(), causa);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensaje = String.format("El parámetro '%s' tiene un valor inválido: '%s'.", ex.getName(), ex.getValue());

        log.warn("Type mismatch - Path: {} - Param: {} - Value: {}", request.getRequestURI(), ex.getName(), ex.getValue());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String mensaje = String.format("El parámetro '%s' es obligatorio.", ex.getParameterName());

        log.warn("Missing parameter - Path: {} - Param: {}", request.getRequestURI(), ex.getParameterName());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String mensaje = String.format("El método HTTP '%s' no está soportado para esta ruta.", ex.getMethod());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensaje)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("La ruta solicitada no existe: " + request.getRequestURI())
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ==================== Catch-all ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        // Intentar extraer un mensaje útil de la cadena de excepciones
        String mensajeAmigable = extraerMensajeAmigable(ex);

        log.error("Unhandled exception - Path: {} - Type: {} - Error: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(mensajeAmigable)
                .path(request.getRequestURI())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ==================== Métodos de ayuda ====================

    /**
     * Extrae un mensaje amigable de una excepción genérica,
     * recorriendo la cadena de causas para encontrar algo útil.
     */
    private String extraerMensajeAmigable(Exception ex) {
        // Recorrer la cadena de causas buscando algo específico
        Throwable current = ex;
        int depth = 0;
        while (current != null && depth < 10) {
            String msg = current.getMessage();
            if (msg != null) {
                // Violaciones de integridad de datos
                if (current instanceof org.hibernate.exception.ConstraintViolationException) {
                    return extraerMensajeConstraint(msg);
                }
                // ConcurrentModification
                if (current instanceof java.util.ConcurrentModificationException) {
                    return "Error de concurrencia al guardar los datos. Intente nuevamente.";
                }
            }
            current = current.getCause();
            depth++;
        }

        // Si no encontramos nada específico, mensaje genérico pero orientativo
        String tipo = ex.getClass().getSimpleName();
        if (tipo.contains("Timeout") || tipo.contains("timeout")) {
            return "La operación tardó demasiado. Intente nuevamente.";
        }
        if (tipo.contains("Connection") || tipo.contains("connection")) {
            return "Error de conexión con la base de datos. Intente nuevamente en unos minutos.";
        }

        return "Ocurrió un error inesperado. Si el problema persiste, contacte al administrador. (Ref: " + MDC.get("requestId") + ")";
    }

    /**
     * Extrae un mensaje amigable de una violación de integridad de datos.
     */
    private String extraerMensajeIntegridad(DataIntegrityViolationException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        if (causa == null) {
            return "Error de integridad de datos. Verifique que no haya registros duplicados.";
        }

        String causaLower = causa.toLowerCase();

        // Unique constraint violation
        if (causaLower.contains("unique") || causaLower.contains("duplicate") || causaLower.contains("duplicar")) {
            String campo = extraerCampoDeConstraint(causa);
            if (campo != null) {
                return "Ya existe un registro con ese valor de " + campo + ". Use un valor diferente.";
            }
            return "Ya existe un registro con esos datos. Verifique que no haya duplicados.";
        }

        // Foreign key violation
        if (causaLower.contains("foreign key") || causaLower.contains("fk_") || causaLower.contains("referential")) {
            if (causaLower.contains("delete") || causaLower.contains("update")) {
                return "No se puede eliminar este registro porque tiene datos relacionados. Elimine primero los registros dependientes.";
            }
            return "El registro referenciado no existe. Verifique los datos seleccionados.";
        }

        // Not null violation
        if (causaLower.contains("not-null") || causaLower.contains("not null") || causaLower.contains("cannot be null")) {
            String campo = extraerCampoDeConstraint(causa);
            if (campo != null) {
                return "El campo " + campo + " es obligatorio.";
            }
            return "Hay campos obligatorios sin completar.";
        }

        // Check constraint
        if (causaLower.contains("check")) {
            return "Uno de los valores ingresados no cumple con las restricciones del sistema.";
        }

        return "Error de integridad de datos. Verifique la información ingresada.";
    }

    /**
     * Intenta extraer el nombre del campo de un mensaje de constraint de PostgreSQL.
     */
    private String extraerCampoDeConstraint(String mensaje) {
        // Formato PostgreSQL: "Key (campo)=(valor) already exists"
        if (mensaje.contains("Key (")) {
            int start = mensaje.indexOf("Key (") + 5;
            int end = mensaje.indexOf(")", start);
            if (end > start) {
                String campo = mensaje.substring(start, end);
                return campo.replace("_", " ");
            }
        }
        // Formato PostgreSQL: column "campo" of relation
        if (mensaje.contains("column \"")) {
            int start = mensaje.indexOf("column \"") + 8;
            int end = mensaje.indexOf("\"", start);
            if (end > start) {
                String campo = mensaje.substring(start, end);
                return campo.replace("_", " ");
            }
        }
        return null;
    }

    /**
     * Extrae un mensaje amigable de un constraint name de Hibernate.
     */
    private String extraerMensajeConstraint(String mensaje) {
        if (mensaje == null) return "Error de restricción en la base de datos.";

        if (mensaje.contains("unique") || mensaje.contains("duplicate")) {
            return "Ya existe un registro con esos datos. Verifique que no haya duplicados.";
        }
        if (mensaje.contains("foreign key")) {
            return "El registro referenciado no existe o no se puede modificar.";
        }

        return "Error de restricción en la base de datos. Verifique los datos ingresados.";
    }

    /**
     * Traduce mensajes de error técnicos a mensajes amigables.
     */
    private String traducirMensajeError(String mensaje) {
        if (mensaje == null) return "Error desconocido.";

        if (mensaje.contains("ConcurrentModification")) {
            return "Error de concurrencia. Intente nuevamente.";
        }
        if (mensaje.contains("could not execute statement")) {
            return "No se pudieron guardar los datos. Verifique la información ingresada.";
        }
        if (mensaje.contains("Connection")) {
            return "Error de conexión con la base de datos.";
        }

        // Si no coincide con ningún patrón conocido, devolver un mensaje genérico
        return "Intente nuevamente. Si el problema persiste, contacte al administrador.";
    }
}
