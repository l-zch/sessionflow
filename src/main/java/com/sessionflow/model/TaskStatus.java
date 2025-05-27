package com.sessionflow.model;

public enum TaskStatus {
    PENDING("PENDING"),
    COMPLETE("COMPLETE");
    
    private final String value;
    
    TaskStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
} 