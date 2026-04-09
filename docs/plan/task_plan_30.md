# Task Plan — Issue #30: Add shared constants to common-lib

## What
Add three utility constant classes to `lib/components` so all services reference a single source of truth for Kafka topic names, HTTP header keys, and Spring Security role strings.

## Services / Modules Touched
- `lib/components` only

## New Package
`com.marzuk.components.constants`

## Classes

### `KafkaTopics.java`
Static `String` constants for all Kafka topic names:
- `USER_REGISTERED`
- `USER_LOCKED`
- `USER_PRESENCE_CHANGED`
- `MESSAGE_SENT`
- `CHAT_USER_ACTIVITY`
- `ADMIN_BROADCAST`

### `AppHeaders.java`
Static `String` constants for HTTP header keys:
- `USER_ID` → `"X-User-Id"`
- `USER_ROLE` → `"X-User-Role"`
- `REQUEST_ID` → `"X-Request-Id"`

### `Roles.java`
Static `String` constants matching Spring Security format:
- `USER` → `"ROLE_USER"`
- `ADMIN` → `"ROLE_ADMIN"`

## Design Notes
- All classes use `@NoArgsConstructor(access = AccessLevel.NONE)` (Lombok) to prevent instantiation — no manual private constructors.
- Plain `static final String` fields — no enums, no config injection. Compile-time constants.
- **SOLID**: SRP — each class owns one concern.
- **KISS**: simplest correct shape; no unnecessary abstraction.

## Tests
None required — static constant declarations contain no logic to test.

## Size
S (3 new files, 1 new package, 1 module)
