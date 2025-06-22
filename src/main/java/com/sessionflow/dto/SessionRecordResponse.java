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
@Schema(description = "工作階段紀錄回應")
public class SessionRecordResponse {
    
    @NotNull
    @Schema(description = "紀錄 ID", example = "1", nullable = false)
    private Long id;
    
    @NotNull
    @Schema(description = "工作階段標題", example = "專案開發時間", nullable = false)
    private String title;
    
    @Schema(description = "關聯的任務 ID", example = "1", nullable = true)
    private Long taskId;
    
    @NotNull
    @Schema(description = "開始時間", example = "2024-01-15T14:00:00", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull
    @Schema(description = "結束時間", example = "2024-01-15T16:00:00", nullable = false)
    private LocalDateTime endAt;
    
    @Schema(description = "計畫備註（來自 Session）", example = "專注於核心功能開發", nullable = true)
    private String plannedNote;
    
    @Schema(description = "完成備註", example = "完成了主要功能的 80%", nullable = true)
    private String completionNote;
} 