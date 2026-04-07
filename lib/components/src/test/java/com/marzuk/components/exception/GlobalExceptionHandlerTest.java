package com.marzuk.components.exception;

import com.marzuk.components.exception.handler.GlobalExceptionHandler;
import com.marzuk.components.pojos.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleAppException_returnsCorrectStatusAndMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        ResponseEntity<Response<Void>> response = handler.handleAppException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void handleAppException_mapsEachStatusCorrectly() {
        assertThat(handler.handleAppException(new BadRequestException("x")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleAppException(new DuplicateResourceException("x")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.handleAppException(new UnauthorizedException("x")).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleAppException(new AccessDeniedException("x")).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleValidation_returnsBadRequestWithFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));
        bindingResult.addError(new FieldError("target", "password", "size must be between 8 and 64"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Response<Void>> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors())
                .containsExactlyInAnyOrder("must not be blank", "size must be between 8 and 64");
    }

    @Test
    void handleGeneric_returns500WithoutLeakingDetails() {
        Exception exception = new RuntimeException("DB connection pool exhausted at host 10.0.0.5");

        ResponseEntity<Response<Void>> response = handler.handleGeneric(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
        assertThat(response.getBody().getErrors()).isNull();
    }
}
