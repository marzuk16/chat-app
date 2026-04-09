package com.marzuk.components.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;

public class EventEnvelopeSerializer implements Serializer<EventEnvelope<?>> {

    private final ObjectMapper objectMapper;

    public EventEnvelopeSerializer() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public byte[] serialize(String topic, EventEnvelope<?> envelope) {
        if (envelope == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(envelope);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to serialize EventEnvelope", exception);
        }
    }
}
