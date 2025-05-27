package com.sessionflow.exception;

public class TagNotFoundException extends RuntimeException {
    
    public TagNotFoundException(Long id) {
        super("Tag with id " + id + " not found");
    }
} 