package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作階段回應")
public class SessionResponse {
    
    @NotNull
    @Schema(description = "工作階段 ID", example = "1", nullable = false)
    private Long id;
    
    @NotNull
    @Schema(description = "工作階段標題", example = "專案開發時間", nullable = false)
    private String title;
    
    @Schema(description = "關聯的任務 ID", example = "1", nullable = true)
    private Long taskId;
    
    @NotNull
    @Schema(description = "開始時間", example = "2024-01-15T14:00:00", nullable = false)
    private LocalDateTime startTime;
    
    @Schema(description = "結束提醒時間", example = "2024-01-15T16:00:00", nullable = true)
    private LocalDateTime endReminder;
    
    @Schema(description = "備註", example = "專注於核心功能開發", nullable = true)
    private String note;
} 