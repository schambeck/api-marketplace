package com.schambeck.marketplace.exception;

public class InventoryValidationException extends RuntimeException {
    public InventoryValidationException(String message) {
        super(message);
    }
}
