# Task Plan — Issue #28: Add JWT Authentication Filter to Common Lib

## What
Create a reusable `JwtAuthenticationFilter` (extending `OncePerRequestFilter`) in `lib/components` that:
- Extracts the JWT from the `Authorization: Bearer <token>` header
- Validates it via the existing `JwtUtil`
- Sets the `SecurityContext` with userId and role
- Skips configurable public paths (`app.security.public-paths`)
- Returns a 401 JSON response (using the existing `Response` envelope) for missing/invalid tokens

## Services Touched

| Service | Role |
|---|---|
| `lib/components` | Houses the filter and `SecurityProperties` (reusable by any service) |
| `packages/auth` | Issues tokens; has public and protected endpoints — needs Spring Security |
| `packages/api-gateway` | Validates JWT at the perimeter before routing to downstream services |

All other services are behind the gateway and do not need Spring Security.

## Dependency Changes

| Module | Change |
|---|---|
| `lib/components/pom.xml` | Add `spring-boot-starter-security` as `<optional>true</optional>` (needed to compile the filter; not forced on consumers) |
| `packages/auth/pom.xml` | Add `spring-boot-starter-security` explicitly |
| `packages/api-gateway/pom.xml` | Add `spring-boot-starter-security` explicitly |

## Layers Involved

### lib/components — `com.marzuk.components.security`

**`SecurityProperties.java`** (new)
- `@ConfigurationProperties(prefix = "app.security")`
- `@Data`, `@Component`
- Field: `List<String> publicPaths`
- Each service configures its own public paths via `application.yml`

**`JwtAuthenticationFilter.java`** (new)
- Extends `OncePerRequestFilter`
- Constructor-injected: `JwtUtil`, `SecurityProperties`, `ObjectMapper`
- `doFilterInternal` logic:
  1. If request URI matches any `publicPaths` entry (via `AntPathMatcher`) → skip, call `filterChain.doFilter()`
  2. Extract `Authorization` header → if missing or not `Bearer ` prefix → write 401 JSON, return
  3. Call `jwtUtil.validateToken(token)` → if false → write 401 JSON, return
  4. Extract `userId` (UUID) and `role` from token
  5. Create `UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)))` and set on `SecurityContextHolder`
  6. Call `filterChain.doFilter()`
- Error response: `Response.error("Unauthorized")` serialized as JSON, status 401

### packages/auth — `com.marzuk.auth.config`

**`SecurityConfig.java`** (new, auth-specific)
- `@Configuration`, `@EnableWebSecurity`
- Constructor-injected: `JwtAuthenticationFilter`
- Configures `SecurityFilterChain`:
  - Disable CSRF
  - Session management: STATELESS
  - Permit `app.security.public-paths` entries
  - All other requests: authenticated
  - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

### packages/api-gateway — `com.marzuk.gateway.config`

**`SecurityConfig.java`** (new, gateway-specific)
- Same shape as auth's `SecurityConfig`
- Gateway's `app.security.public-paths` will differ (e.g., `/api/auth/**`)

## SOLID Alignment

- **SRP**: Filter handles authentication only; `JwtUtil` handles token parsing; `SecurityProperties` handles configuration
- **OCP**: Public paths are configured per-service via YAML — no code changes to add/remove paths
- **DIP**: Filter depends on `JwtUtil` (shared abstraction), not on JJWT directly

## KISS Check

No unnecessary complexity. Single filter class, simple list-based path exclusion with `AntPathMatcher`, no custom annotations or abstract base classes.

## Proposed Tests

### Unit Tests — `JwtAuthenticationFilterTest` (lib/components)

All tests use constructor-injected mocks (`JwtUtil`, `HttpServletRequest`, `HttpServletResponse`, `FilterChain`).

| Test case | Expectation |
|---|---|
| Valid token | `SecurityContext` set with correct userId as principal and role as granted authority |
| Missing `Authorization` header | 401 response, `SecurityContext` empty |
| Header present but no `Bearer ` prefix | 401 response |
| Expired token (`validateToken` returns false) | 401 response |
| Malformed/tampered token (`validateToken` returns false) | 401 response |
| Request URI matches a public path | Filter skipped, `filterChain.doFilter()` called, no token extraction |

## Size
**M** — Full layer stack in one shared library, plus SecurityConfig in two services.
