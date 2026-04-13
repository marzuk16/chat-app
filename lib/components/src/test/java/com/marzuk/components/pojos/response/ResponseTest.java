package com.marzuk.components.pojos.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseTest {

    @Test
    void successFactorySetsTrueWithDataAndTimestamp() {
        Response<String> response = Response.success("payload");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorFactorySetsFalseWithMessageAndTimestamp() {
        Response<Void> response = Response.error("something went wrong");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("something went wrong");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void errorWithErrorsFactoryPopulatesErrorsList() {
        List<String> validationErrors = List.of("name is required", "email is invalid");
        Response<Void> response = Response.error("validation failed", validationErrors);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("validation failed");
        assertThat(response.getErrors()).containsExactlyElementsOf(validationErrors);
        assertThat(response.getTimestamp()).isNotNull();
    }
}
