package com.sessionflow.exception;

public class TagNameAlreadyExistsException extends RuntimeException {
    
    public TagNameAlreadyExistsException(String name) {
        super("Tag with name '" + name + "' already exists");
    }
} 