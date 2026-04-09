# Task Plan — Issue #172: Replace HMAC JWT secret with RSA asymmetric key pair

## What
Replace the shared HMAC symmetric secret (`JWT_SECRET_KEY`) with an RSA 4096-bit asymmetric key pair.
The auth service holds the private key (for signing tokens), while all other services only receive
the public key (for verification). This follows the principle of least privilege — a compromised
downstream service cannot forge tokens.

## Services Touched
- `lib/components` — shared security classes
- `packages/auth` — config only (will use both keys)
- `packages/api-gateway` — config only (public key only)
- `infrastructure/bash/init_scripts/initialize.sh` — key generation

## Changes by File

### 1. `infrastructure/bash/init_scripts/initialize.sh`
- Generate RSA 4096 private key with `openssl genrsa`
- Extract the public key with `openssl rsa -pubout`
- Base64-encode both keys (single-line) for safe storage in `.env`
- Write `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` to `.env`

### 2. `.env.example`
- Replace `JWT_SECRET_KEY=...` with `JWT_PRIVATE_KEY=...` and `JWT_PUBLIC_KEY=...`

### 3. `lib/components` — `JwtProperties.java`
- Remove `secretKey` field
- Add `publicKey` (required — all services need it for verification)
- Add `privateKey` (optional — only auth needs it for signing)

### 4. `lib/components` — `JwtUtil.java`
- Constructor decodes Base64 `publicKey` -> `RSAPublicKey`
- Optionally decodes Base64 `privateKey` -> `RSAPrivateKey` (if provided)
- `verifyWith(publicKey)` replaces `verifyWith(signingKey)` for parsing
- Expose `getSigningKey()` so the auth service can sign tokens later (returns `RSAPrivateKey`)

### 5. `packages/auth/src/main/resources/application.yml`
- Replace `jwt.secretKey` with `jwt.privateKey: ${JWT_PRIVATE_KEY}` and `jwt.publicKey: ${JWT_PUBLIC_KEY}`

### 6. `packages/api-gateway/src/main/resources/application.yml`
- Replace `jwt.secretKey` with `jwt.publicKey: ${JWT_PUBLIC_KEY}` only

### 7. `lib/components` — `JwtUtilTest.java`
- Generate a test RSA key pair programmatically (`KeyPairGenerator.getInstance("RSA")`)
- Update `buildValidToken` / `buildExpiredToken` to sign with the test private key
- Update `JwtUtil` instantiation to use Base64-encoded test keys
- Update `validateToken_throwsForWrongSigningKey` to use a different RSA key pair

## No Changes Needed
- `SecurityConfig.java` — injects `JwtUtil` as before
- `JwtAuthenticationFilter.java` — calls `jwtUtil.validateToken()` as before
- `SecurityProperties.java` — unchanged

## KISS Check
- No new classes introduced; only modifying existing ones
- RSA key loading uses standard JDK (`KeyFactory` + `X509EncodedKeySpec` / `PKCS8EncodedKeySpec`) — no new dependencies
- Optional `privateKey` avoids splitting `JwtUtil` into two classes prematurely; a simple null check suffices

## Tests

### Unit tests (updated `JwtUtilTest.java`)
- All existing tests re-verified with RSA keys
- `extractUserId`, `extractRole`, `extractEmail` — valid token signed with test private key
- `isTokenExpired` — valid + expired tokens
- `validateToken` — valid, expired, malformed, wrong-key scenarios
- New: verify JwtUtil constructs successfully without a private key (verify-only mode)

### No new integration tests needed
This is a pure in-memory crypto change with no external infrastructure.
