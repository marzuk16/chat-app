# Task Plan — Issue #27: Add JWT Utility Class to `common-lib`

## What

A `JwtUtil` class in `lib/components` that **parses and validates** JWTs (no token issuance). Provides: `extractAllClaims`, `extractUserId`, `extractRole`, `extractEmail`, `isTokenExpired`, and `validateToken`. The signing key is externalized via Spring config.

**Labels:** common, Feature, priority: critical
**Depends on:** #24 (already merged — `UnauthorizedException` is available)
**Blocks:** #28

---

## Services / Modules Touched

- `lib/components` only — shared library consumed by `auth`, `api-gateway`, and future services.

---

## Components

| Component | Package | Responsibility |
|---|---|---|
| `JwtProperties` | `com.marzuk.components.security` | `@ConfigurationProperties(prefix = "jwt")` holding the signing key |
| `JwtUtil` | `com.marzuk.components.security` | Stateless utility — parse claims, validate tokens |

---

## Design

### `JwtProperties`
- `@ConfigurationProperties(prefix = "jwt")` with a `secretKey` field.
- Each consuming service provides `jwt.secret-key` in its own config.

### `JwtUtil`
- `@Component` with constructor injection of `JwtProperties`.
- Methods:
  - `extractAllClaims(String token)` → `Claims` — parses and returns all claims; throws `UnauthorizedException` on failure.
  - `extractUserId(String token)` → `UUID` — reads the `sub` claim.
  - `extractRole(String token)` → `String` — reads a `role` claim.
  - `extractEmail(String token)` → `String` — reads an `email` claim.
  - `isTokenExpired(String token)` → `boolean` — checks `exp` claim against current time.
  - `validateToken(String token)` → `Claims` — calls `extractAllClaims` + checks expiry; throws `UnauthorizedException` for malformed/expired tokens.

### JJWT dependency
- Add `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, and `jjwt-jackson` (0.12.x) to `lib/components/pom.xml`.

---

## SOLID / KISS Notes

- **SRP**: `JwtUtil` only parses/validates; `JwtProperties` only holds config.
- **OCP**: Consumers can read additional claims via `extractAllClaims` without modifying `JwtUtil`.
- **KISS**: No abstract factory, no strategy pattern — direct JJWT calls only.

---

## Files to Create / Modify

| Action | File |
|---|---|
| Modify | `lib/components/pom.xml` — add JJWT dependencies |
| Create | `lib/components/src/main/java/com/marzuk/components/security/JwtProperties.java` |
| Create | `lib/components/src/main/java/com/marzuk/components/security/JwtUtil.java` |
| Create | `lib/components/src/test/java/com/marzuk/components/security/JwtUtilTest.java` |

---

## Tests

Unit tests only — no Spring context, no Testcontainers. Construct `JwtUtil` directly with a test `JwtProperties`. Generate real JWTs via JJWT builder using a test secret key.

| Test | What it verifies |
|---|---|
| `extractUserId_returnsCorrectUuid` | Parses `sub` from a valid token |
| `extractRole_returnsCorrectRole` | Parses `role` claim |
| `extractEmail_returnsCorrectEmail` | Parses `email` claim |
| `isTokenExpired_returnsFalseForValidToken` | Non-expired token → `false` |
| `isTokenExpired_returnsTrueForExpiredToken` | Expired token → `true` |
| `validateToken_returnsClaimsForValidToken` | Valid token passes validation |
| `validateToken_throwsForExpiredToken` | Expired token → `UnauthorizedException` |
| `validateToken_throwsForMalformedToken` | Garbage string → `UnauthorizedException` |
| `validateToken_throwsForWrongSigningKey` | Token signed with different key → `UnauthorizedException` |
