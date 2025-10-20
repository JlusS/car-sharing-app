package com.carrental.carrent.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entity, Long id) {
        super(entity + " not found with id: " + id);
    }

    public EntityNotFoundException(Class<?> entityClass, Long id) {
        super(entityClass.getSimpleName() + " not found with id: " + id);
    }
}
