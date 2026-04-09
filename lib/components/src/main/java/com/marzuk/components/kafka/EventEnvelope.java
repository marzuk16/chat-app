package com.marzuk.components.kafka;

import com.marzuk.components.pojos.enums.EventSource;
import com.marzuk.components.pojos.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEnvelope<T> {

    private UUID eventId;
    private EventType eventType;
    private Instant timestamp;
    private EventSource source;
    private T payload;

    public static <T> EventEnvelope<T> of(EventType eventType, EventSource source, T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(Instant.now())
                .source(source)
                .payload(payload)
                .build();
    }
}
