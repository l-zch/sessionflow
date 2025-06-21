package com.sessionflow.exception;

import java.time.LocalDateTime;

/**
 * 統一錯誤定義
 */
public enum ErrorDefinition {
    
    // 驗證錯誤
    VALIDATION_ERROR("VALIDATION_ERROR", "請求參數驗證失敗"),
    
    // 資源不存在錯誤
    TAG_NOT_FOUND("TAG_NOT_FOUND", "標籤不存在"),
    TASK_NOT_FOUND("TASK_NOT_FOUND", "任務不存在"),
    SESSION_NOT_FOUND("SESSION_NOT_FOUND", "工作階段不存在"),
    SESSION_RECORD_NOT_FOUND("SESSION_RECORD_NOT_FOUND", "工作階段紀錄不存在"),
    SCHEDULE_ENTRY_NOT_FOUND("SCHEDULE_ENTRY_NOT_FOUND", "排程不存在"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "請求的資源不存在"),
    
    // 衝突錯誤
    TAG_NAME_CONFLICT("TAG_NAME_CONFLICT", "標籤名稱重複"),
    
    // 業務邏輯錯誤
    INVALID_TIME_RANGE("INVALID_TIME_RANGE", "時間區間錯誤"),
    INVALID_ARGUMENT("INVALID_ARGUMENT", "參數錯誤"),
    
    // 請求格式錯誤
    JSON_PARSE_ERROR("JSON_PARSE_ERROR", "JSON 格式錯誤"),
    TYPE_MISMATCH_ERROR("TYPE_MISMATCH_ERROR", "參數類型錯誤"),
    MISSING_PARAMETER("MISSING_PARAMETER", "缺少必要參數"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", "不支援的媒體類型"),
    
    // 系統錯誤
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "伺服器內部錯誤");
    
    private final String code;
    private final String message;
    
    ErrorDefinition(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 建立 ErrorResponse
     */
    public ErrorResponse createResponse(String details) {
        return new ErrorResponse(code, message, details, LocalDateTime.now());
    }
    
    /**
     * 建立 ErrorResponse（使用預設詳細訊息）
     */
    public ErrorResponse createResponse() {
        return createResponse(message);
    }
    
    /**
     * 生成 Swagger 範例 JSON
     */
    public String generateExample(String sampleDetails) {
        return """
            {
              "code": "%s",
              "message": "%s",
              "details": "%s",
              "timestamp": "2024-01-15T10:30:00"
            }
            """.formatted(code, message, sampleDetails);
    }
    
    /**
     * 獲取預設範例詳細訊息
     */
    public String getDefaultExampleDetails() {
        return switch (this) {
            case VALIDATION_ERROR -> "{title=任務標題不能為空}";
            case TASK_NOT_FOUND -> "Task with id 1 not found";
            case TAG_NOT_FOUND -> "Tag with id 1 not found";
            case SESSION_NOT_FOUND -> "Session with id 1 not found";
            case SESSION_RECORD_NOT_FOUND -> "SessionRecord with id 1 not found";
            case SCHEDULE_ENTRY_NOT_FOUND -> "ScheduleEntry with id 1 not found";
            case TAG_NAME_CONFLICT -> "Tag name 'Work' already exists";
            case INVALID_ARGUMENT -> "Invalid status value: INVALID_STATUS";
            case INVALID_TIME_RANGE -> "Start time must be before end time";
            case JSON_PARSE_ERROR -> "Invalid JSON format in request body";
            case TYPE_MISMATCH_ERROR -> "Invalid value 'abc' for parameter 'id'. Expected type: Long";
            case MISSING_PARAMETER -> "Required parameter 'title' is missing";
            case UNSUPPORTED_MEDIA_TYPE -> "Content type 'text/plain' not supported";
            case INTERNAL_SERVER_ERROR -> "An unexpected error occurred";
            case RESOURCE_NOT_FOUND -> "The requested resource at /non-existent/path could not be found";
        };
    }
    
    /**
     * 生成預設範例
     */
    public String generateDefaultExample() {
        return generateExample(getDefaultExampleDetails());
    }
} 