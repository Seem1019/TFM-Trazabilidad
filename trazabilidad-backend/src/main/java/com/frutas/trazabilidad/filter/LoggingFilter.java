package com.frutas.trazabilidad.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filtro que registra automáticamente información de cada request y response.
 *
 * Logs incluyen:
 * - Método HTTP y URI
 * - Tiempo de procesamiento
 * - Status code de respuesta
 * - IP del cliente
 * - User agent
 *
 * Nota: No loguea el body completo para evitar logs excesivamente grandes,
 * pero se puede activar en desarrollo si es necesario.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // Ejecutar después de RequestCorrelationFilter
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Wrapper para poder leer el request/response body múltiples veces
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Continuar con la cadena de filtros
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Loguear información del request/response
            logRequestResponse(requestWrapper, responseWrapper, duration);

            // IMPORTANTE: Copiar el contenido del response al output stream real
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Registra información del request y response
     */
    private void logRequestResponse(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long duration
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // Log con nivel según status code
        if (status >= 500) {
            log.error("HTTP {} {} → {} | {}ms | IP: {} | UA: {}",
                    method, uri, status, duration, clientIp, userAgent);
        } else if (status >= 400) {
            log.warn("HTTP {} {} → {} | {}ms | IP: {} | UA: {}",
                    method, uri, status, duration, clientIp, userAgent);
        } else {
            log.info("HTTP {} {} → {} | {}ms | IP: {} | UA: {}",
                    method, uri, status, duration, clientIp, userAgent);
        }

        // En desarrollo, loguear también el body (opcional)
        if (log.isDebugEnabled()) {
            logRequestBody(request);
            logResponseBody(response);
        }
    }

    /**
     * Loguea el body del request (solo en modo DEBUG)
     */
    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            log.debug("Request Body: {}", body);
        }
    }

    /**
     * Loguea el body del response (solo en modo DEBUG)
     */
    private void logResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            log.debug("Response Body: {}", body);
        }
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * No aplicar el filtro a recursos estáticos
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs");
    }
}