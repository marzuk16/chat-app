package com.marzuk.components.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AppExceptionTest {

    @Test
    void resourceNotFoundException_hasNotFoundStatus() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    @Test
    void badRequestException_hasBadRequestStatus() {
        BadRequestException exception = new BadRequestException("Invalid input");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("Invalid input");
    }

    @Test
    void duplicateResourceException_hasConflictStatus() {
        DuplicateResourceException exception =
                new DuplicateResourceException("Email already exists");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).isEqualTo("Email already exists");
    }

    @Test
    void unauthorizedException_hasUnauthorizedStatus() {
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void accessDeniedException_hasForbiddenStatus() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getMessage()).isEqualTo("Access denied");
    }

    @Test
    void allExceptions_extendAppException() {
        assertThat(new ResourceNotFoundException("x")).isInstanceOf(AppException.class);
        assertThat(new BadRequestException("x")).isInstanceOf(AppException.class);
        assertThat(new DuplicateResourceException("x")).isInstanceOf(AppException.class);
        assertThat(new UnauthorizedException("x")).isInstanceOf(AppException.class);
        assertThat(new AccessDeniedException("x")).isInstanceOf(AppException.class);
    }
}
