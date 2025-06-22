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
@Schema(description = "排程回應")
public class ScheduleEntryResponse {
    
    @NotNull
    @Schema(description = "排程 ID", example = "1", nullable = false)
    private Long id;
    
    @NotNull
    @Schema(description = "排程標題", example = "團隊會議", nullable = false)
    private String title;
    
    @Schema(description = "關聯的任務 ID", example = "1", nullable = true)
    private Long taskId;
    
    @NotNull
    @Schema(description = "開始時間", example = "2024-01-15T10:00:00", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull
    @Schema(description = "結束時間", example = "2024-01-15T11:00:00", nullable = false)
    private LocalDateTime endAt;
    
    @Schema(description = "備註", example = "討論專案進度和下週計畫", nullable = true)
    private String note;
} 