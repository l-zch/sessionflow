package com.sessionflow.exception;

public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(Long id) {
        super("Task with id " + id + " not found");
    }
    
    public TaskNotFoundException(String message) {
        super(message);
    }
} 