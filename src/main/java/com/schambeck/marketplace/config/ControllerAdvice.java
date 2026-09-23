package com.schambeck.marketplace.config;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import com.schambeck.marketplace.exception.GameContextValidationException;
import com.schambeck.marketplace.exception.OfferValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
class ControllerAdvice {
    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(error);
    }

    @ExceptionHandler(GameContextValidationException.class)
    ResponseEntity<Map<String, String>> handleGameContextValidationException(GameContextValidationException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OfferValidationException.class)
    ResponseEntity<Map<String, String>> handleOfferValidationException(OfferValidationException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

}
