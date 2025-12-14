package com.ecommerce.inventory.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<ObjectNode> handleInventoryItemNotFoundException(InventoryItemNotFoundException ex, WebRequest request) {
        log.error("Inventory item not found: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ObjectNode> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        log.error("Insufficient stock: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(InvalidInventoryOperationException.class)
    public ResponseEntity<ObjectNode> handleInvalidInventoryOperationException(InvalidInventoryOperationException ex, WebRequest request) {
        log.error("Invalid inventory operation: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ObjectNode> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("timestamp", Instant.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.set("errors", objectMapper.valueToTree(errors));
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ObjectNode> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());
        String message = "Data integrity violation. Please check your input.";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message = ex.getCause().getMessage();
        }
        return buildErrorResponse(new RuntimeException(message), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ObjectNode> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> String.format("%s: %s", 
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.joining(", "));
        return buildErrorResponse(new RuntimeException(errorMessage), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ObjectNode> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        log.error("Optimistic locking failure: {}", ex.getMessage());
        return buildErrorResponse(
                new RuntimeException("The data was modified by another transaction. Please refresh and try again."),
                HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ObjectNode> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("Message not readable: {}", ex.getMessage());
        return buildErrorResponse(
                new RuntimeException("Malformed JSON request. Please check your request body."),
                HttpStatus.BAD_REQUEST,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ObjectNode> handleAllUncaughtException(Exception ex, WebRequest request) {
        log.error("Unhandled error occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                new RuntimeException("An unexpected error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    private ResponseEntity<ObjectNode> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("timestamp", Instant.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, status);
    }
    
    private ResponseEntity<ObjectNode> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(response, status);
    }
}
