package com.marzuk.components.exception.handler;

import com.marzuk.components.exception.AppException;
import com.marzuk.components.pojos.response.Response;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Response<Void>> handleAppException(AppException exception) {
        log.warn("Application exception [{}]: {}", exception.getStatus(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus())
                .body(Response.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(
            MethodArgumentNotValidException exception) {
        List<String> errors =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();

        log.warn("Validation failed on {}: {}", exception.getObjectName(), errors);
        return ResponseEntity.badRequest().body(Response.error("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneric(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.internalServerError().body(Response.error("Internal server error"));
    }
}
