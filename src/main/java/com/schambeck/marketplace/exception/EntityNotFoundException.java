package com.schambeck.marketplace.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entity, Integer id) {
        super(entity.getSimpleName() + " id " + id + " not found.");
    }
    
    public EntityNotFoundException(Class<?> entity, UUID id) {
        super(entity.getSimpleName() + " id " + id + " not found.");
    }
    
    public EntityNotFoundException(Class<?> entity, String id) {
        super(entity.getSimpleName() + " id " + id + " not found.");
    }
    
    EntityNotFoundException(String message) {
        super(message);
    }
}
