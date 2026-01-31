package com.frutas.trazabilidad.security;

import com.frutas.trazabilidad.entity.User;
import com.frutas.trazabilidad.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta todas las peticiones HTTP y valida el token JWT.
 * Verifica que el token sea válido y que el usuario esté activo y no bloqueado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extraer token del header Authorization
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7); // Remover "Bearer "
            String email = jwtUtil.validateTokenAndGetEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user == null) {
                    log.warn("Token válido pero usuario no encontrado: {}", email);
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
                    return;
                }

                // VALIDACIÓN CRÍTICA: Verificar que el usuario sigue activo
                if (!user.getActivo()) {
                    log.warn("Token válido pero usuario inactivo: {}", email);
                    sendErrorResponse(response, HttpStatus.FORBIDDEN, "Usuario inactivo. Contacte al administrador.");
                    return;
                }

                // VALIDACIÓN CRÍTICA: Verificar que el usuario no está bloqueado
                if (user.estaBloqueadoTemporalmente()) {
                    log.warn("Token válido pero usuario bloqueado temporalmente: {}", email);
                    sendErrorResponse(response, HttpStatus.FORBIDDEN, "Cuenta bloqueada temporalmente.");
                    return;
                }

                // Establecer autenticación en el contexto de seguridad
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuario autenticado vía JWT: {} - Empresa: {}",
                        email, user.getEmpresa().getId());
            }

        } catch (Exception e) {
            log.error("Error al procesar token JWT: {}", e.getMessage());
            // No establecer autenticación, permitir que Spring Security maneje el acceso denegado
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Envía una respuesta de error JSON al cliente.
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                message,
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
