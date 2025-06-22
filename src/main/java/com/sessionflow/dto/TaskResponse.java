package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任務回應")
public class TaskResponse {
    
    @NotNull
    @Schema(description = "任務 ID", example = "1", nullable = false)
    private Long id;
    
    @NotNull
    @Schema(description = "任務標題", example = "完成專案文件", nullable = false)
    private String title;
    
    @NotNull
    @Schema(description = "標籤列表", nullable = false)
    private List<TagResponse> tags;
    
    @Schema(description = "截止時間", example = "2024-01-15T18:00:00", nullable = true)
    private LocalDateTime dueTime;
    
    @Schema(description = "完成時間", example = "2024-01-14T16:30:00", nullable = true)
    private LocalDateTime completedAt;
    
    @Schema(description = "任務備註", example = "需要包含技術規格和使用者手冊", nullable = true)
    private String note;
    
    @NotNull
    @Schema(description = "任務狀態", example = "PENDING", allowableValues = {"PENDING", "COMPLETE"}, nullable = false)
    private String status;
    
    // Custom constructor for basic fields
    public TaskResponse(Long id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }
} 