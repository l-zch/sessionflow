package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作階段建立請求")
public class SessionRequest {
    
    @NotBlank(message = "Session title cannot be blank")
    @Schema(description = "工作階段標題", example = "專案開發時間")
    private String title;
    
    @Schema(description = "關聯的任務 ID", example = "1")
    private Long taskId;
    
    @Schema(description = "結束提醒時間", example = "2024-01-15T16:00:00")
    private LocalDateTime endReminder;
    
    @Schema(description = "備註", example = "專注於核心功能開發")
    private String note;
    
    // Custom constructor for basic fields
    public SessionRequest(String title) {
        this.title = title;
    }
} 