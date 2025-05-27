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
@Schema(description = "工作階段紀錄建立請求")
public class SessionRecordCreateRequest {
    
    @NotNull(message = "Session ID cannot be null")
    @Schema(description = "工作階段 ID", example = "1")
    private Long sessionId;
    
    @Schema(description = "完成備註", example = "完成了主要功能的 80%")
    private String completionNote;
    
    private LocalDateTime startTime;
    
    private String plannedNotes;
} 