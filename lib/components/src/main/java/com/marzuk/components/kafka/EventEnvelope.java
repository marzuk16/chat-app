package com.marzuk.components.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEnvelope<T> {

    private UUID eventId;
    private String eventType;
    private Instant timestamp;
    private String source;
    private T payload;

    public static <T> EventEnvelope<T> of(String eventType, String source, T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(Instant.now())
                .source(source)
                .payload(payload)
                .build();
    }
}
