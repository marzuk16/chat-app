package com.marzuk.components.kafka;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import java.time.temporal.ChronoUnit;

class EventEnvelopeTest {

    @Test
    void of_setsAllFieldsCorrectly() {
        String eventType = "USER_CREATED";
        String source = "auth-service";
        String payload = "some-payload";

        EventEnvelope<String> envelope = EventEnvelope.of(eventType, source, payload);

        assertThat(envelope.getEventId()).isNotNull();
        assertThat(envelope.getEventType()).isEqualTo(eventType);
        assertThat(envelope.getSource()).isEqualTo(source);
        assertThat(envelope.getPayload()).isEqualTo(payload);
        assertThat(envelope.getTimestamp()).isNotNull();
    }

    @Test
    void of_generatesUniqueEventIds() {
        EventEnvelope<String> first = EventEnvelope.of("EVENT", "service", "payload");
        EventEnvelope<String> second = EventEnvelope.of("EVENT", "service", "payload");

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }

    @Test
    void of_timestampIsCloseToNow() {
        Instant before = Instant.now();
        EventEnvelope<String> envelope = EventEnvelope.of("EVENT", "service", "payload");
        Instant after = Instant.now();

        assertThat(envelope.getTimestamp()).isBetween(before, after);
    }

    @Test
    void builder_allowsManualFieldOverride() {
        UUID fixedId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Instant fixedTime = Instant.parse("2024-01-01T00:00:00Z");

        EventEnvelope<Integer> envelope = EventEnvelope.<Integer>builder()
                .eventId(fixedId)
                .eventType("ORDER_PLACED")
                .timestamp(fixedTime)
                .source("order-service")
                .payload(42)
                .build();

        assertThat(envelope.getEventId()).isEqualTo(fixedId);
        assertThat(envelope.getTimestamp()).isEqualTo(fixedTime);
        assertThat(envelope.getPayload()).isEqualTo(42);
    }
}
