package com.marzuk.components.pojos.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserSummaryDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializationRoundTripPreservesAllFields() throws Exception {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UserSummaryDTO original =
                UserSummaryDTO.builder()
                        .id(id)
                        .username("jdoe")
                        .displayName("John Doe")
                        .avatarUrl("https://example.com/avatar.png")
                        .build();

        String json = objectMapper.writeValueAsString(original);
        UserSummaryDTO deserialized = objectMapper.readValue(json, UserSummaryDTO.class);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void nullOptionalFieldsSerializeWithoutError() throws Exception {
        UserSummaryDTO dto =
                UserSummaryDTO.builder()
                        .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                        .username("jdoe")
                        .displayName(null)
                        .avatarUrl(null)
                        .build();

        String json = objectMapper.writeValueAsString(dto);
        UserSummaryDTO deserialized = objectMapper.readValue(json, UserSummaryDTO.class);

        assertThat(deserialized.getDisplayName()).isNull();
        assertThat(deserialized.getAvatarUrl()).isNull();
        assertThat(deserialized.getUsername()).isEqualTo("jdoe");
    }
}
