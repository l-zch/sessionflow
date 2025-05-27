package com.sessionflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TagNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTagNameAlreadyExists(TagNameAlreadyExistsException ex) {
        log.warn("Tag name conflict: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TAG_NAME_CONFLICT",
            "標籤名稱重複",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTagNotFound(TagNotFoundException ex) {
        log.warn("Tag not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TAG_NOT_FOUND",
            "標籤不存在",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex) {
        log.warn("Task not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TASK_NOT_FOUND",
            "任務不存在",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException ex) {
        log.warn("Session not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "SESSION_NOT_FOUND",
            "工作階段不存在",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(SessionRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionRecordNotFound(SessionRecordNotFoundException ex) {
        log.warn("SessionRecord not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "SESSION_RECORD_NOT_FOUND",
            "工作階段紀錄不存在",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ScheduleEntryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScheduleEntryNotFound(ScheduleEntryNotFoundException ex) {
        log.warn("ScheduleEntry not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "SCHEDULE_ENTRY_NOT_FOUND",
            "排程不存在",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(InvalidTimeRangeException ex) {
        log.warn("Invalid time range: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_TIME_RANGE",
            "時間區間錯誤",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            "參數錯誤",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNSUPPORTED_MEDIA_TYPE",
            "不支援的媒體類型",
            ex.getMessage(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "請求參數驗證失敗",
            errors.toString(),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "JSON_PARSE_ERROR",
            "JSON 格式錯誤",
            "Invalid JSON format in request body",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type conversion error: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH_ERROR",
            "參數類型錯誤",
            message,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER",
            "缺少必要參數",
            message,
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "伺服器內部錯誤",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 