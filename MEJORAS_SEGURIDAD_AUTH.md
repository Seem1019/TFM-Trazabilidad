# Mejoras de Seguridad y Flujo de Autenticación

## Resumen Ejecutivo

Este documento detalla las mejoras de seguridad implementadas en el backend del sistema de trazabilidad, incluyendo un nuevo sistema de autenticación con refresh tokens, validación de estado de usuario, aislamiento multitenant, control de acceso basado en roles (RBAC), rate limiting y protección contra XSS.

---

## 1. Sistema de Refresh Tokens

### Problema Anterior
Un solo JWT con expiración larga (24h) o sin expiración clara, lo que representaba un riesgo de seguridad si el token era comprometido.

### Solución Implementada
Sistema dual de tokens con rotación automática:

| Token | Duración | Almacenamiento | Propósito |
|-------|----------|----------------|-----------|
| Access Token | 15 minutos | Cliente (memoria) | Autenticación de requests |
| Refresh Token | 7 días | Base de datos | Renovación de access token |

### Archivos Creados

#### `RefreshToken.java` - Entidad
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    private Long id;
    private String token;
    private User user;
    private Instant expiryDate;
    private boolean revoked;
    private String deviceInfo;
    private String ipAddress;
    private Instant createdAt;
}
```

#### `RefreshTokenRepository.java`
```java
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
    void deleteByUserId(Long userId);
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    void revokeAllByUserId(Long userId);
}
```

#### `RefreshTokenService.java`
```java
@Service
public class RefreshTokenService {
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);
    public RefreshToken validateRefreshToken(String token);
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String deviceInfo, String ipAddress);
    public void revokeToken(String token);
    public void revokeAllUserTokens(Long userId);
}
```

#### DTOs
- `TokenRefreshRequest.java` - Request para renovar token
- `TokenRefreshResponse.java` - Response con nuevos tokens

### Endpoints Nuevos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/refresh` | Renovar access token usando refresh token |
| POST | `/api/auth/logout` | Cerrar sesión (revoca refresh token actual) |
| POST | `/api/auth/logout-all` | Cerrar todas las sesiones del usuario |

---

## 2. Validación de Usuario en JwtAuthFilter

### Problema Anterior
El filtro JWT solo validaba el token, no verificaba si el usuario seguía activo o estaba bloqueado.

### Solución Implementada
Validación completa del estado del usuario en cada request:

```java
// En JwtAuthFilter.java
if (!user.getActivo()) {
    sendErrorResponse(response, HttpStatus.FORBIDDEN,
        "Usuario inactivo. Su cuenta ha sido deshabilitada.");
    return;
}

if (user.estaBloqueadoTemporalmente()) {
    sendErrorResponse(response, HttpStatus.FORBIDDEN,
        "Cuenta bloqueada temporalmente por múltiples intentos fallidos.");
    return;
}
```

### Beneficios
- Usuarios desactivados pierden acceso inmediatamente
- Cuentas bloqueadas por brute force no pueden usar tokens existentes
- Mayor control sobre sesiones activas

---

## 3. Aislamiento Multitenant por Empresa

### Problema Anterior
`PalletController/Service` y otros componentes no filtraban por empresa, permitiendo potencial acceso cross-tenant a datos de otras organizaciones.

### Solución Implementada

#### Patrón de Aislamiento
Todos los métodos de servicio ahora requieren `empresaId`:

```java
// Antes (VULNERABLE)
public PalletResponse buscarPorId(Long id) {
    return palletRepository.findById(id)...
}

// Después (SEGURO)
public PalletResponse buscarPorId(Long id, Long empresaId) {
    return palletRepository.findByIdAndEmpresaId(id, empresaId)...
}
```

#### Archivos Modificados

**Entidad Pallet.java:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "empresa_id", nullable = false)
private Empresa empresa;
```

**PalletRepository.java:**
```java
Optional<Pallet> findByIdAndEmpresaId(Long id, Long empresaId);
List<Pallet> findByEmpresaIdAndActivoTrueOrderByFechaPaletizadoDesc(Long empresaId);
boolean existsByCodigoPalletAndEmpresaId(String codigo, Long empresaId);
```

**PalletController.java:**
```java
@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA')")
public ResponseEntity<ApiResponse<PalletResponse>> obtenerPorId(
        @PathVariable Long id,
        @AuthenticationPrincipal User user) {

    PalletResponse response = palletService.buscarPorId(id, user.getEmpresa().getId());
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

#### TenantContext - Helper Centralizado
```java
@Component
public class TenantContext {
    public User getCurrentUser();
    public Long getCurrentEmpresaId();
    public Empresa getCurrentEmpresa();
    public boolean isAdmin();
    public boolean hasRole(String role);
}
```

#### Componentes con Multitenant Aplicado
- `PalletController/Service`
- `EnvioController/Service`
- `FincaController`
- `DocumentoExportacionController/Service`
- `EventoLogisticoController/Service`

---

## 4. Control de Acceso Basado en Roles (RBAC)

### Roles del Sistema

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `ADMIN` | Administrador | Acceso total a todas las operaciones |
| `PRODUCTOR` | Productor agrícola | Gestión de fincas, lotes y producción |
| `OPERADOR_PLANTA` | Operador de planta | Operaciones de planta, empaque y pallets |
| `OPERADOR_LOGISTICA` | Operador logístico | Gestión de envíos, documentos y eventos |
| `AUDITOR` | Auditor | Solo lectura de toda la información |

### Implementación con @PreAuthorize

```java
// Solo lectura - todos los roles
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTOR', 'OPERADOR_PLANTA', 'OPERADOR_LOGISTICA', 'AUDITOR')")
public ResponseEntity<ApiResponse<List<PalletResponse>>> listar(...) { }

// Crear - roles operativos
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_PLANTA')")
public ResponseEntity<ApiResponse<PalletResponse>> crear(...) { }

// Eliminar - solo admin
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Void>> eliminar(...) { }
```

---

## 5. Rate Limiting

### Configuración

**application.yml:**
```yaml
app:
  rate-limit:
    enabled: true
    requests-per-minute: 60
    login-requests-per-minute: 10
```

### Implementación

**RateLimitingFilter.java:**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // 60 requests/minuto para endpoints generales
    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
            .build();
    }

    // 10 requests/minuto para login (protección brute force)
    private Bucket createLoginBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
            .build();
    }
}
```

### Respuesta cuando se excede el límite
```json
{
  "success": false,
  "message": "Demasiadas solicitudes. Intente de nuevo más tarde.",
  "timestamp": "2024-01-15T10:30:00Z"
}
```
HTTP Status: `429 Too Many Requests`

---

## 6. Protección contra XSS

### InputSanitizer - Utilidad de Sanitización

```java
@Component
public class InputSanitizer {

    // Sanitización básica
    public String sanitize(String input) {
        // Remueve: <script>, onclick=, javascript:, data:, expression()
        // Escapa: <, >, ", ', &, /
    }

    // Sanitización estricta (remueve todos los tags HTML)
    public String sanitizeStrict(String input);

    // Validación de URLs
    public boolean isUrlSafe(String url) {
        // Solo permite http:// y https://
        // Rechaza javascript:, data:, etc.
    }

    public String sanitizeUrl(String url);
}
```

### Aplicación en Controllers

**DocumentoExportacionController.java:**
```java
@PostMapping
public ResponseEntity<ApiResponse<DocumentoExportacionResponse>> crear(
        @Valid @RequestBody DocumentoExportacionRequest request,
        @AuthenticationPrincipal User user) {

    // Sanitizar campos de texto
    sanitizeRequest(request);

    // Validar URL
    if (request.getUrlArchivo() != null && !sanitizer.isUrlSafe(request.getUrlArchivo())) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("URL no válida o potencialmente peligrosa"));
    }

    // Procesar...
}

private void sanitizeRequest(DocumentoExportacionRequest request) {
    request.setNumeroDocumento(sanitizer.sanitizeStrict(request.getNumeroDocumento()));
    request.setTipoDocumento(sanitizer.sanitizeStrict(request.getTipoDocumento()));
    request.setObservaciones(sanitizer.sanitize(request.getObservaciones()));
}
```

---

## 7. Configuración JWT Actualizada

### JwtUtil.java

```java
@Component
public class JwtUtil {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.access-token-expiration:900000}") // 15 min
    private Long accessTokenExpiration;

    @Value("${spring.security.jwt.refresh-token-expiration:604800000}") // 7 días
    private Long refreshTokenExpiration;

    public String generateAccessToken(User user) {
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("userId", user.getId())
            .claim("empresaId", user.getEmpresa().getId())
            .claim("rol", user.getRol().name())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(getSigningKey())
            .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
```

---

## 8. Estructura de LoginResponse

### Antes
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "email": "admin@empresa.com",
    "nombre": "Juan"
  }
}
```

### Después
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "admin@empresa.com",
    "nombre": "Juan",
    "apellido": "Pérez",
    "rol": "ADMIN",
    "empresaId": 1,
    "empresaNombre": "Frutas Colombia S.A.S"
  }
}
```

---

## 9. Flujo de Autenticación Completo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO DE AUTENTICACIÓN                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────┐                    ┌──────────┐                    ┌──────────┐
│  Cliente │                    │  Backend │                    │ Database │
└────┬─────┘                    └────┬─────┘                    └────┬─────┘
     │                               │                               │
     │  1. POST /api/auth/login      │                               │
     │  {email, password}            │                               │
     │──────────────────────────────>│                               │
     │                               │                               │
     │                               │  2. Buscar usuario            │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │                               │  3. Validar:                  │
     │                               │     - Password correcto       │
     │                               │     - Usuario activo          │
     │                               │     - No bloqueado            │
     │                               │                               │
     │                               │  4. Crear RefreshToken        │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │  5. Response:                 │                               │
     │  {accessToken, refreshToken}  │                               │
     │<──────────────────────────────│                               │
     │                               │                               │
     │═══════════════════════════════════════════════════════════════│
     │                     REQUESTS AUTENTICADOS                     │
     │═══════════════════════════════════════════════════════════════│
     │                               │                               │
     │  6. GET /api/pallets          │                               │
     │  Authorization: Bearer {token}│                               │
     │──────────────────────────────>│                               │
     │                               │                               │
     │                               │  7. JwtAuthFilter:            │
     │                               │     - Validar JWT             │
     │                               │     - Cargar usuario          │
     │                               │     - Verificar activo        │
     │                               │     - Verificar !bloqueado    │
     │                               │                               │
     │                               │  8. @PreAuthorize             │
     │                               │     - Verificar rol           │
     │                               │                               │
     │                               │  9. Service con empresaId     │
     │                               │     - Filtrar por tenant      │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │  10. Response: datos          │                               │
     │<──────────────────────────────│                               │
     │                               │                               │
     │═══════════════════════════════════════════════════════════════│
     │                     RENOVACIÓN DE TOKEN                       │
     │═══════════════════════════════════════════════════════════════│
     │                               │                               │
     │  11. POST /api/auth/refresh   │                               │
     │  {refreshToken}               │                               │
     │──────────────────────────────>│                               │
     │                               │                               │
     │                               │  12. Validar refresh token    │
     │                               │  13. Verificar usuario activo │
     │                               │  14. Rotar refresh token      │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │  15. Response:                │                               │
     │  {newAccessToken,             │                               │
     │   newRefreshToken}            │                               │
     │<──────────────────────────────│                               │
     │                               │                               │
     │═══════════════════════════════════════════════════════════════│
     │                          LOGOUT                               │
     │═══════════════════════════════════════════════════════════════│
     │                               │                               │
     │  16. POST /api/auth/logout    │                               │
     │  {refreshToken}               │                               │
     │──────────────────────────────>│                               │
     │                               │                               │
     │                               │  17. Revocar token            │
     │                               │──────────────────────────────>│
     │                               │<──────────────────────────────│
     │                               │                               │
     │  18. Response: OK             │                               │
     │<──────────────────────────────│                               │
     │                               │                               │
```

---

## 10. Tests Actualizados

### AuthServiceTest.java
- Tests para login con access y refresh tokens
- Tests para refresh token (válido, usuario inactivo, usuario bloqueado)
- Tests para logout y logout-all
- Tests para bloqueo por intentos fallidos

### AuthControllerIntegrationTest.java
- Usa `MockHttpServletRequest` para simular requests HTTP
- Tests de integración completos del flujo de autenticación
- Limpieza de tabla `refresh_tokens` en setUp

### PalletServiceTest.java
- Todos los tests incluyen `empresaId`
- Mock de `EmpresaRepository`
- Tests de aislamiento multitenant

---

## 11. Dependencias Agregadas

**pom.xml:**
```xml
<!-- Bucket4j para Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

---

## 12. Configuración Final

**application.yml:**
```yaml
spring:
  docker:
    compose:
      enabled: false

  security:
    jwt:
      secret: ${JWT_SECRET:mi-super-secreto-jwt-256-bits}
      access-token-expiration: 900000      # 15 minutos
      refresh-token-expiration: 604800000  # 7 días

app:
  rate-limit:
    enabled: true
    requests-per-minute: 60
    login-requests-per-minute: 10
```

---

## Checklist de Seguridad

- [x] Tokens de corta duración (15 min)
- [x] Refresh tokens con rotación
- [x] Validación de usuario activo en cada request
- [x] Validación de bloqueo temporal
- [x] Aislamiento multitenant por empresa
- [x] RBAC con @PreAuthorize
- [x] Rate limiting general y específico para login
- [x] Protección XSS con sanitización de inputs
- [x] Validación de URLs seguras
- [x] Revocación de tokens en logout
- [x] Revocación de todos los tokens en cambio de contraseña
- [x] Almacenamiento seguro de refresh tokens en BD

---

*Documento generado: Enero 2024*
*Versión: 1.0*
