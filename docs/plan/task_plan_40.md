# Task Plan — Issue #40: Create Credential Entity and Repository

## What

Create the `Credential` JPA entity and `CredentialRepository` in the `auth` service. This is the persistence foundation for authentication — registration, login, account locking, and email verification all depend on it.

## Services and Layers Involved

- **`lib/components`** — new `Role` enum (shared across services)
- **`packages/auth`** — entity, repository, Liquibase migration, dependency addition

## Reusable Components

- `Role` enum placed in `lib/components` under `com.marzuk.components.pojos.enums.auth` so other services (e.g. `user`, `api-gateway`) can reference it without depending on `auth`.

## Package Structure

```
lib/components/.../pojos/enums/auth/
└── Role.java

packages/auth/.../
├── entities/
│   └── Credential.java
└── features/
    └── credentials/
        └── CredentialRepository.java
```

## Implementation Steps

### 1. Role Enum — `lib/components`

**File:** `lib/components/src/main/java/com/marzuk/components/pojos/enums/auth/Role.java`

```java
public enum Role { USER, ADMIN }
```

### 2. Add Liquibase to `packages/auth/pom.xml`

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### 3. Liquibase Changelog

```
packages/auth/src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/
    └── 001-create-credentials-table.yaml
```

Creates the `credentials` table with all columns and the unique constraint on `email`.  
`application.yml` — `ddl-auto: validate`, add `spring.liquibase.change-log`.

### 4. Credential Entity

**File:** `packages/auth/src/main/java/com/marzuk/auth/entities/Credential.java`

Extends `BaseEntity`. Uses Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@Builder`, `@AllArgsConstructor`).

| Field           | Type      | Column                          |
|-----------------|-----------|---------------------------------|
| `email`         | `String`  | `unique, not null`              |
| `passwordHash`  | `String`  | `not null`                      |
| `role`          | `Role`    | `@Enumerated(STRING)`, `not null` |
| `locked`        | `boolean` | `not null`                      |
| `emailVerified` | `boolean` | `not null`                      |
| `failedAttempts`| `int`     | `not null`                      |
| `lockedUntil`   | `Instant` | nullable                        |

### 5. CredentialRepository

**File:** `packages/auth/src/main/java/com/marzuk/auth/features/credentials/CredentialRepository.java`

```java
public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByEmail(String email);
}
```

## Tests

### Unit — `CredentialTest.java`

- Builder defaults: `locked=false`, `failedAttempts=0`, `role=USER`, `emailVerified=false`

### Integration — `CredentialRepositoryIntegrationTest.java` (Testcontainers + PostgreSQL)

- Liquibase runs automatically against the container
- Save and retrieve by email (`findByEmail`)
- `findByEmail` returns `Optional.empty()` for unknown email
- Duplicate email throws `DataIntegrityViolationException`

## Files to Create/Modify

| Action     | File |
|------------|------|
| **Create** | `lib/components/.../pojos/enums/auth/Role.java` |
| **Modify** | `packages/auth/pom.xml` — add `liquibase-core` |
| **Create** | `packages/auth/.../resources/db/changelog/db.changelog-master.yaml` |
| **Create** | `packages/auth/.../resources/db/changelog/changes/001-create-credentials-table.yaml` |
| **Modify** | `packages/auth/.../resources/application.yml` |
| **Create** | `packages/auth/.../entities/Credential.java` |
| **Create** | `packages/auth/.../features/credentials/CredentialRepository.java` |
| **Create** | `packages/auth/.../test/.../entities/CredentialTest.java` |
| **Create** | `packages/auth/.../test/.../features/credentials/CredentialRepositoryIntegrationTest.java` |
