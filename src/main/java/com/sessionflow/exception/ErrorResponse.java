package com.sessionflow.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "錯誤回應")
public class ErrorResponse {
    
    @Schema(description = "錯誤代碼", example = "VALIDATION_ERROR")
    private String code;
    
    @Schema(description = "錯誤訊息", example = "請求參數驗證失敗")
    private String message;
    
    @Schema(description = "錯誤詳細資訊", example = "Invalid input data")
    private String details;
    
    @Schema(description = "錯誤發生時間", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
} 