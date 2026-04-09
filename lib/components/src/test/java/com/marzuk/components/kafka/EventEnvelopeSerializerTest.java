package com.marzuk.components.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventEnvelopeSerializerTest {

    private EventEnvelopeSerializer serializer;
    private EventEnvelopeDeserializer deserializer;

    @BeforeEach
    void setUp() {
        serializer = new EventEnvelopeSerializer();
        deserializer = new EventEnvelopeDeserializer();
    }

    @Test
    void serialize_returnsNullForNullInput() {
        byte[] result = serializer.serialize("topic", null);

        assertThat(result).isNull();
    }

    @Test
    void deserialize_returnsNullForNullInput() {
        EventEnvelope<?> result = deserializer.deserialize("topic", null);

        assertThat(result).isNull();
    }

    @Test
    void roundTrip_withStringPayload() {
        EventEnvelope<String> original = EventEnvelope.of("USER_CREATED", "auth-service", "user-123");

        byte[] bytes = serializer.serialize("test-topic", original);
        EventEnvelope<?> deserialized = deserializer.deserialize("test-topic", bytes);

        assertThat(deserialized.getEventId()).isEqualTo(original.getEventId());
        assertThat(deserialized.getEventType()).isEqualTo(original.getEventType());
        assertThat(deserialized.getSource()).isEqualTo(original.getSource());
        assertThat(deserialized.getTimestamp()).isEqualTo(original.getTimestamp());
        assertThat(deserialized.getPayload()).isInstanceOf(JsonNode.class);
        assertThat(deserialized.getPayload().toString()).contains("user-123");
    }

    @Test
    void roundTrip_withMapPayload() {
        Map<String, Object> payloadData = Map.of("userId", "abc", "action", "login");
        EventEnvelope<Map<String, Object>> original = EventEnvelope.of("AUTH_EVENT", "auth-service", payloadData);

        byte[] bytes = serializer.serialize("test-topic", original);
        EventEnvelope<?> deserialized = deserializer.deserialize("test-topic", bytes);

        assertThat(deserialized.getEventType()).isEqualTo("AUTH_EVENT");
        JsonNode payloadNode = (JsonNode) deserialized.getPayload();
        assertThat(payloadNode.get("action").asText()).isEqualTo("login");
        assertThat(payloadNode.get("userId").asText()).isEqualTo("abc");
    }

    @Test
    void roundTrip_withCustomPojoPayload() {
        UserCreatedPayload payloadData = new UserCreatedPayload("user-42", "alice@example.com");
        EventEnvelope<UserCreatedPayload> original = EventEnvelope.of("USER_CREATED", "auth-service", payloadData);

        byte[] bytes = serializer.serialize("test-topic", original);
        EventEnvelope<?> deserialized = deserializer.deserialize("test-topic", bytes);

        JsonNode payloadNode = (JsonNode) deserialized.getPayload();
        assertThat(payloadNode.get("userId").asText()).isEqualTo("user-42");
        assertThat(payloadNode.get("email").asText()).isEqualTo("alice@example.com");
    }

    record UserCreatedPayload(String userId, String email) {}
}
