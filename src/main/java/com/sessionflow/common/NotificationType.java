package com.sessionflow.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    SCHEDULE_ENTRY_CREATE,
    SCHEDULE_ENTRY_UPDATE,
    SCHEDULE_ENTRY_DELETE,
    SESSION_CREATE,
    SESSION_DELETE,
    SESSION_RECORD_CREATE,
    SESSION_RECORD_UPDATE,
    SESSION_RECORD_DELETE,
    TAG_CREATE,
    TAG_UPDATE,
    TAG_DELETE,
    TASK_CREATE,
    TASK_UPDATE,
    TASK_DELETE;
    
    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
