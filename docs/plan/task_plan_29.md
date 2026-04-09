# Task Plan — Issue #29: Add Kafka Event Envelope to common-lib

## What
Add a generic `EventEnvelope<T>` class to `lib/components` that wraps Kafka event payloads
with standard metadata (eventId, eventType, timestamp, source). Include a static factory
method and Kafka-compatible JSON serializer/deserializer.

## Service / Module
`lib/components` only — shared library addition.

## New Files
All under package `com.marzuk.components.kafka`:

| File | Purpose |
|---|---|
| `EventEnvelope.java` | Generic envelope POJO with `of()` static factory |
| `EventEnvelopeSerializer.java` | Kafka `Serializer<EventEnvelope<?>>` using Jackson |
| `EventEnvelopeDeserializer.java` | Kafka `Deserializer<EventEnvelope<?>>` using Jackson |

## Design Details

### `EventEnvelope<T>`
- Fields: `UUID eventId`, `String eventType`, `Instant timestamp`, `String source`, `T payload`
- Lombok: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Static factory: `EventEnvelope.of(String eventType, String source, T payload)`
  — generates `UUID.randomUUID()` and `Instant.now()`

### `EventEnvelopeSerializer`
- Implements `org.apache.kafka.common.serialization.Serializer<EventEnvelope<?>>`
- Uses Jackson `ObjectMapper` with `JavaTimeModule` for `Instant` serialization

### `EventEnvelopeDeserializer`
- Implements `org.apache.kafka.common.serialization.Deserializer<EventEnvelope<?>>`
- Uses Jackson `ObjectMapper` with `JavaTimeModule`
- Deserializes payload as raw `JsonNode` by default; consumers convert to their specific type

## Dependency Change
Add `spring-kafka` as an **optional** dependency to `lib/components/pom.xml` to provide
the `Serializer`/`Deserializer` interfaces without forcing Kafka on modules that don't need it.

## SOLID Alignment
- **SRP**: `EventEnvelope` is a pure data carrier; serialization logic is in separate classes
- **OCP**: Generic `<T>` payload allows extension without modifying the envelope
- **DIP**: Services depend on the `EventEnvelope` abstraction, not on raw Kafka byte handling

## KISS Check
- No abstract factory pattern or registry — just a static `of()` method
- No custom type resolver for the generic payload — deserialize as `JsonNode` and let consumers handle it
- Three classes total, no Spring configuration beans needed

## Tests
Under `com.marzuk.components.kafka` in `src/test/java`:

| Test class | What it verifies |
|---|---|
| `EventEnvelopeTest` | `of()` factory generates UUID and timestamp; all fields set correctly |
| `EventEnvelopeSerializerTest` | Serialization round-trip: serialize → deserialize → assert equality; null handling; different payload types (String, Map, custom POJO) |
