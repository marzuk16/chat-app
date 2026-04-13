package com.marzuk.components.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.marzuk.components.pojos.enums.EventSource;
import com.marzuk.components.pojos.enums.EventType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void of_setsAllFieldsCorrectly() {
        EventType eventType = EventType.USER_REGISTERED;
        EventSource source = EventSource.AUTH;
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
        EventEnvelope<String> first =
                EventEnvelope.of(EventType.MESSAGE_SENT, EventSource.CHAT, "payload");
        EventEnvelope<String> second =
                EventEnvelope.of(EventType.MESSAGE_SENT, EventSource.CHAT, "payload");

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }

    @Test
    void of_timestampIsCloseToNow() {
        Instant before = Instant.now();
        EventEnvelope<String> envelope =
                EventEnvelope.of(EventType.MESSAGE_SENT, EventSource.CHAT, "payload");
        Instant after = Instant.now();

        assertThat(envelope.getTimestamp()).isBetween(before, after);
    }

    @Test
    void builder_allowsManualFieldOverride() {
        UUID fixedId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Instant fixedTime = Instant.parse("2024-01-01T00:00:00Z");

        EventEnvelope<Integer> envelope =
                EventEnvelope.<Integer>builder()
                        .eventId(fixedId)
                        .eventType(EventType.ADMIN_BROADCAST)
                        .timestamp(fixedTime)
                        .source(EventSource.USER)
                        .payload(42)
                        .build();

        assertThat(envelope.getEventId()).isEqualTo(fixedId);
        assertThat(envelope.getTimestamp()).isEqualTo(fixedTime);
        assertThat(envelope.getPayload()).isEqualTo(42);
    }
}
