package com.schambeck.marketplace.exception;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) { super(message); }
}
