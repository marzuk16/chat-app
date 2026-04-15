# Task Plan — Issue #41: Implement User Registration Endpoint

## What

`POST /auth/register` — accepts email, password, and username. Validates input, hashes password with BCrypt, saves a `Credential` entity, and returns `userId` with 201 Created.

All requests arrive via the API gateway (which appends the `/api` prefix). The auth service trusts the gateway completely — no JWT validation at the service level.

## Services & Layers Involved

| Layer | Location |
|---|---|
| Auto-configuration | `lib/authorizer` — add `SERVICE` mode + `PasswordEncoder` bean |
| DTOs | `lib/components` — `RegisterRequest`, `RegisterResponse` |
| Route constants | `packages/auth` — `common/AuthRoutes.java` |
| Service | `packages/auth` — `features/credentials/CredentialService.java` |
| Controller | `packages/auth` — `features/credentials/CredentialController.java` |
| Config | `packages/auth` — `application.yml` (wire `app.authorizer.mode=service`) |

## Changes by Module

### `lib/authorizer`

1. Add `SERVICE` to `AuthorizerProperties.Mode` enum.
2. Add `ServiceSecurityConfig` nested class to `AuthorizerAutoConfiguration`:
   - `@ConditionalOnProperty(name = "app.authorizer.mode", havingValue = "service")`
   - `SecurityFilterChain` that disables CSRF, sets STATELESS session, and permits all requests.
3. Add unconditional `PasswordEncoder` bean (`BCryptPasswordEncoder`) to `AuthorizerAutoConfiguration`.

### `lib/components`

New DTOs under `pojos/dto/auth/`:

- **`RegisterRequest`** — `@Data`, fields: `email` (`@NotBlank @Email`), `password` (`@NotBlank @Size(min=8, max=72) @Pattern`), `username` (`@NotBlank @Size(min=3, max=30)`). Username is accepted for contract consistency with downstream services (issue #42) but not persisted — no username column in `credentials`.
- **`RegisterResponse`** — `@Data @Builder`, field: `userId` (UUID).

### `packages/auth`

1. **`common/AuthRoutes.java`** — constants: `BASE = "/auth"`, `REGISTER = "/register"`.
2. **`features/credentials/CredentialService.java`**:
   - Constructor injection of `CredentialRepository` and `PasswordEncoder`.
   - `register(RegisterRequest)` → check duplicate email (`DuplicateResourceException` on conflict) → BCrypt-encode password → build and save `Credential` (role=USER, locked=false, emailVerified=false) → return `RegisterResponse` with generated UUID.
3. **`features/credentials/CredentialController.java`**:
   - `@RestController @RequestMapping(AuthRoutes.BASE) @RequiredArgsConstructor`
   - `@PostMapping(AuthRoutes.REGISTER) @ResponseStatus(HttpStatus.CREATED)` returns `Response<RegisterResponse>` directly (no `ResponseEntity`).
4. **`application.yml`** — add `app.authorizer.mode: service`.

## Reusable Components

- `DuplicateResourceException` (lib/components) — triggers 409 via `GlobalExceptionHandler`.
- `MethodArgumentNotValidException` handling (lib/components) — triggers 400 with field error messages.
- `Response<T>` envelope (lib/components) — wraps all responses.

## Tests

### Unit — `CredentialServiceTest`
- Happy path: valid input → credential saved, UUID returned.
- Duplicate email → `DuplicateResourceException` thrown.
- Raw password never stored (verify `passwordEncoder.encode()` called, hash persisted).

### Integration — `RegistrationIntegrationTest` (Testcontainers + PostgreSQL)
- `201` valid request → userId in body, BCrypt hash in DB.
- `409` duplicate email → error response.
- `400` invalid email, weak password, blank fields → validation error messages in response.

## Acceptance Criteria

| Criterion | Solution |
|---|---|
| Valid registration → 201 + userId | Controller + Service |
| Duplicate email → 409 | `DuplicateResourceException` → `GlobalExceptionHandler` |
| Invalid email format → 400 | `@Email` → `MethodArgumentNotValidException` handler |
| Weak password → 400 with message | `@Pattern` → `MethodArgumentNotValidException` handler |
| Password stored as BCrypt hash | `passwordEncoder.encode()` in service; bean from authorizer |
