package com.marzuk.components.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;

public class EventEnvelopeDeserializer implements Deserializer<EventEnvelope<?>> {

    private final ObjectMapper objectMapper;

    public EventEnvelopeDeserializer() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public EventEnvelope<?> deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            // Deserialize payload as JsonNode so each consumer can convert to its own type
            return objectMapper.readValue(
                    data,
                    objectMapper
                            .getTypeFactory()
                            .constructParametricType(EventEnvelope.class, JsonNode.class));
        } catch (Exception exception) {
            throw new RuntimeException("Failed to deserialize EventEnvelope", exception);
        }
    }
}
