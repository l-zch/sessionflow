package com.sessionflow.config;

/**
 * API 回應範例引用工具類
 * 提供統一的錯誤範例引用字串
 */
public final class ApiResponseTemplates {
    
    private ApiResponseTemplates() {
        // 工具類不允許實例化
    }
    
    // 錯誤範例引用常數
    public static final String VALIDATION_ERROR_REF = "#/components/examples/validation_error_example";
    public static final String INVALID_ARGUMENT_REF = "#/components/examples/invalid_argument_example";
    public static final String TASK_NOT_FOUND_REF = "#/components/examples/task_not_found_example";
    public static final String TAG_NOT_FOUND_REF = "#/components/examples/tag_not_found_example";
    public static final String SESSION_NOT_FOUND_REF = "#/components/examples/session_not_found_example";
    public static final String SESSION_RECORD_NOT_FOUND_REF = "#/components/examples/session_record_not_found_example";
    public static final String SCHEDULE_ENTRY_NOT_FOUND_REF = "#/components/examples/schedule_entry_not_found_example";
    public static final String TAG_NAME_CONFLICT_REF = "#/components/examples/tag_name_conflict_example";
    public static final String INVALID_TIME_RANGE_REF = "#/components/examples/invalid_time_range_example";
    public static final String JSON_PARSE_ERROR_REF = "#/components/examples/json_parse_error_example";
    public static final String TYPE_MISMATCH_ERROR_REF = "#/components/examples/type_mismatch_error_example";
    public static final String MISSING_PARAMETER_REF = "#/components/examples/missing_parameter_example";
    public static final String UNSUPPORTED_MEDIA_TYPE_REF = "#/components/examples/unsupported_media_type_example";
    public static final String INTERNAL_SERVER_ERROR_REF = "#/components/examples/internal_server_error_example";
} 