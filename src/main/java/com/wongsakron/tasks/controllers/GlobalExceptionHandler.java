package com.wongsakron.tasks.controllers;

import com.wongsakron.tasks.domain.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    // This class handles exceptions globally across the application

    @ExceptionHandler(IllegalArgumentException.class) // Handle specific exceptions
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            WebRequest request
    ){
        var body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                IllegalStateException ex,
                WebRequest request
                ){
        var body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException ex,
            WebRequest request
    ){
        var msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .findFirst().orElse("Validation error");
        var body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), msg, request.getDescription(false));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex,
            WebRequest request
    ){
        var body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getDescription(false));
        return ResponseEntity.badRequest().body(body);
    }
}
