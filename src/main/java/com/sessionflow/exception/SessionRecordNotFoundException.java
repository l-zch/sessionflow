package com.sessionflow.exception;

/**
 * 工作階段紀錄未找到例外
 */
public class SessionRecordNotFoundException extends RuntimeException {
    
    public SessionRecordNotFoundException(Long id) {
        super("SessionRecord with id " + id + " not found");
    }
} 