package com.schambeck.marketplace.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entity, Integer id) {
        super(entity.getSimpleName() + " id " + id + " not found.");
    }
    
    EntityNotFoundException(String message) {
        super(message);
    }
}
