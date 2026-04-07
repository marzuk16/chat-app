package com.marzuk.components.exception.handler;

import com.marzuk.components.exception.AppException;
import com.marzuk.components.pojos.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Response<Void>> handleAppException(AppException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(Response.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity
                .badRequest()
                .body(Response.error("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGeneric(Exception exception) {
        return ResponseEntity
                .internalServerError()
                .body(Response.error("Internal server error"));
    }
}
