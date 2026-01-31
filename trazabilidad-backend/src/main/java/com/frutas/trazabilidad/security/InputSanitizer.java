package com.frutas.trazabilidad.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utilidad para sanitización de inputs de usuario.
 * Previene XSS y otros ataques de inyección.
 */
@Component
public class InputSanitizer {

    // Patrones para detectar contenido malicioso
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PATTERN = Pattern.compile(
            "data:", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "expression\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
            "<[^>]+>", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitiza un string removiendo contenido potencialmente malicioso.
     * Escapa caracteres HTML y remueve scripts.
     *
     * @param input String a sanitizar
     * @return String sanitizado o null si el input era null
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input;

        // Remover scripts
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remover event handlers (onclick, onmouseover, etc.)
        sanitized = EVENT_PATTERN.matcher(sanitized).replaceAll("");

        // Remover javascript: y data: URIs
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = DATA_PATTERN.matcher(sanitized).replaceAll("");

        // Remover expression()
        sanitized = EXPRESSION_PATTERN.matcher(sanitized).replaceAll("");

        // Escapar caracteres HTML básicos
        sanitized = escapeHtml(sanitized);

        return sanitized.trim();
    }

    /**
     * Sanitización estricta que también remueve cualquier tag HTML.
     *
     * @param input String a sanitizar
     * @return String sin ningún tag HTML
     */
    public String sanitizeStrict(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = sanitize(input);
        // Remover cualquier tag HTML restante
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

        return sanitized.trim();
    }

    /**
     * Escapa caracteres HTML especiales.
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Valida que una URL no contenga javascript: u otros esquemas peligrosos.
     *
     * @param url URL a validar
     * @return true si la URL es segura
     */
    public boolean isUrlSafe(String url) {
        if (url == null || url.isBlank()) {
            return true; // URLs vacías son seguras
        }

        String lowerUrl = url.toLowerCase().trim();

        // Solo permitir http, https
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            return false;
        }

        // Rechazar si contiene javascript o data
        if (JAVASCRIPT_PATTERN.matcher(url).find() || DATA_PATTERN.matcher(url).find()) {
            return false;
        }

        return true;
    }

    /**
     * Sanitiza una URL, retornando null si es insegura.
     */
    public String sanitizeUrl(String url) {
        if (!isUrlSafe(url)) {
            return null;
        }
        return url;
    }
}
