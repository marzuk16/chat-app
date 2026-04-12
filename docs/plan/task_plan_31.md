# Task Plan — Issue #31: Add shared DTOs to common-lib

## What

Create `UserSummaryDTO` — a lightweight, pure DTO carrying `id`, `username`, `displayName`, and `avatarUrl` — in `lib/components` so any service can reference a user without coupling to the `user` service's JPA entity.

## Service / Module

`lib/components` only.

## Layers Involved

No layered stack — this is a plain data carrier with no controller, service, or repository.

## Implementation

**File:** `lib/components/src/main/java/com/marzuk/components/pojos/dto/user/UserSummaryDTO.java`
**Package:** `com.marzuk.components.pojos.dto.user`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private UUID id;
    private String username;
    private String displayName;
    private String avatarUrl;
}
```

Design decisions:
- Lombok class (not a record) — consistent with `Response`, `PageResponse`, `EventEnvelope` in the same module.
- `@Data` provides equals/hashCode/toString.
- `@Builder` for clean construction in service/mapper code.
- No JPA annotations — pure DTO per acceptance criteria.
- No Jackson annotations needed — camelCase fields serialize correctly with Spring Boot defaults.

## Reusable Components

The DTO itself is the reusable component, placed in `lib/components` for cross-service consumption.

## Tests

**File:** `lib/components/src/test/java/com/marzuk/components/pojos/dto/user/UserSummaryDTOTest.java`

- **Serialization round-trip** — build a `UserSummaryDTO`, serialize to JSON with `ObjectMapper`, deserialize back, assert equality.
- **Null optional fields** — verify `displayName` and `avatarUrl` can be null without breaking serialization.

No integration tests required — no infrastructure involved.

## Dependencies

Depends on issue #24. Confirm #24 is merged before implementing.
