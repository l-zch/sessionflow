package com.sessionflow.exception;

public class ScheduleEntryNotFoundException extends RuntimeException {
    
    public ScheduleEntryNotFoundException(Long id) {
        super("ScheduleEntry with id " + id + " not found");
    }
} 