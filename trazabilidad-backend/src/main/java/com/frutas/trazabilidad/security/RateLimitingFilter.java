package com.frutas.trazabilidad.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro para implementar Rate Limiting por IP.
 * Protege contra abusos y ataques DoS.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.login-requests-per-minute:10}")
    private int loginRequestsPerMinute;

    // Cache de buckets por IP
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBucketCache = new ConcurrentHashMap<>();

    // Limpieza periódica del cache (cada 10 minutos)
    private long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 600000; // 10 minutos

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        // Limpieza periódica del cache
        cleanupIfNeeded();

        // Seleccionar bucket según el tipo de endpoint
        Bucket bucket;
        if (isLoginEndpoint(path)) {
            bucket = getLoginBucket(clientIp);
        } else if (isPublicEndpoint(path)) {
            // Endpoints públicos no tienen rate limiting estricto
            filterChain.doFilter(request, response);
            return;
        } else {
            bucket = getBucket(clientIp);
        }

        // Intentar consumir un token
        if (bucket.tryConsume(1)) {
            // Agregar headers de rate limiting
            addRateLimitHeaders(response, bucket);
            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido
            log.warn("Rate limit excedido para IP: {} en path: {}", clientIp, path);
            sendRateLimitExceededResponse(response);
        }
    }

    /**
     * Obtiene o crea un bucket para la IP dada (endpoints generales).
     */
    private Bucket getBucket(String clientIp) {
        return bucketCache.computeIfAbsent(clientIp, ip -> createBucket(requestsPerMinute));
    }

    /**
     * Obtiene o crea un bucket para la IP dada (endpoints de login).
     * Más restrictivo para prevenir ataques de fuerza bruta.
     */
    private Bucket getLoginBucket(String clientIp) {
        return loginBucketCache.computeIfAbsent(clientIp, ip -> createBucket(loginRequestsPerMinute));
    }

    /**
     * Crea un nuevo bucket con la capacidad especificada.
     */
    private Bucket createBucket(int tokensPerMinute) {
        Bandwidth limit = Bandwidth.classic(
                tokensPerMinute,
                Refill.greedy(tokensPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Verifica si es un endpoint de login/auth.
     */
    private boolean isLoginEndpoint(String path) {
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/auth/password-reset");
    }

    /**
     * Verifica si es un endpoint público (sin rate limiting estricto).
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/public/") ||
                path.startsWith("/api/etiquetas/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator/health");
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies.
     */
    private String getClientIp(HttpServletRequest request) {
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
     * Agrega headers estándar de rate limiting a la respuesta.
     */
    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket) {
        response.addHeader("X-Rate-Limit-Remaining",
                String.valueOf(bucket.getAvailableTokens()));
    }

    /**
     * Envía respuesta de rate limit excedido (429 Too Many Requests).
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"Demasiadas solicitudes. Intente de nuevo en un minuto.\",\"data\":null,\"timestamp\":\"%s\"}",
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Limpia buckets antiguos del cache para evitar memory leaks.
     */
    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            bucketCache.clear();
            loginBucketCache.clear();
            lastCleanup = now;
            log.debug("Cache de rate limiting limpiado");
        }
    }
}
