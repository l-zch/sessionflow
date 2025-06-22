package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任務建立/更新請求")
public class TaskRequest {
    
    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    @Schema(description = "任務標題", example = "完成專案文件", required = true)
    private String title;
    
    @Schema(description = "標籤 ID 列表", example = "[1, 2]")
    private List<Long> tagIds;
    
    @Schema(description = "截止時間", example = "2024-01-15T18:00:00")
    private LocalDateTime dueAt;
    
    @Size(max = 2000, message = "Task note cannot exceed 2000 characters")
    @Schema(description = "任務備註", example = "需要包含技術規格和使用者手冊")
    private String note;
    
    // Custom constructor for title only
    public TaskRequest(String title) {
        this.title = title;
    }
} 