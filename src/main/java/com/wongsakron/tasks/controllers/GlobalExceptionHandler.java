package com.wongsakron.tasks.controllers;

import com.wongsakron.tasks.domain.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    // This class handles exceptions globally across the application

    @ExceptionHandler({IllegalArgumentException.class}) // Handle specific exceptions
    public ResponseEntity<ErrorResponse> handleException(
            RuntimeException ex, WebRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // Set the HTTP status code
                ex.getMessage(), // Get the message from the exception
                request.getDescription(false) // Get the details of the request that caused the exception
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
