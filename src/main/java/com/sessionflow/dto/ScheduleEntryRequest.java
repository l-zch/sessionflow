package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排程建立/更新請求")
public class ScheduleEntryRequest {
    
    @NotBlank(message = "排程標題不能為空")
    @Size(max = 255, message = "排程標題長度不能超過 255 字元")
    @Schema(description = "排程標題", example = "團隊會議")
    private String title;
    
    @Schema(description = "關聯的任務 ID", example = "1")
    private Long taskId;
    
    @NotNull(message = "開始時間不能為空")
    @Schema(description = "開始時間", example = "2024-01-15T10:00:00")
    private LocalDateTime startAt;
    
    @NotNull(message = "結束時間不能為空")
    @Schema(description = "結束時間", example = "2024-01-15T11:00:00")
    private LocalDateTime endAt;
    
    @Size(max = 500, message = "備註長度不能超過 500 字元")
    @Schema(description = "備註", example = "討論專案進度和下週計畫")
    private String note;
} 