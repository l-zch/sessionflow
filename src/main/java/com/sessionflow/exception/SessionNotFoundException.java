package com.sessionflow.exception;

public class SessionNotFoundException extends RuntimeException {
    
    public SessionNotFoundException(Long id) {
        super("Session with id " + id + " not found");
    }
    
    public SessionNotFoundException(String message) {
        super(message);
    }
} 