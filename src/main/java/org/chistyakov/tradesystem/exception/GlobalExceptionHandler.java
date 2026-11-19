package org.chistyakov.tradesystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(new ErrorResponse("IDEMPOTENT_CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ErrorResponse> handleOrderValidation(OrderValidationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                             .body(new ErrorResponse("VALIDATION_FAILED", e.getMessage()));
    }

    @ExceptionHandler(OrderConflictException.class)
    public ResponseEntity<ErrorResponse> handleOrderConflict(OrderConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(new ErrorResponse("ORDER_CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity.badRequest()
                         .body(new ErrorResponse("BAD_REQUEST","Missing header: " + e.getHeaderName()));
    }
}
