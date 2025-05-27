package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作階段紀錄更新請求")
public class SessionRecordUpdateRequest {
    
    @Schema(description = "計畫備註", example = "專注於核心功能開發")
    @Size(max = 500, message = "計畫備註不能超過 500 個字元")
    private String plannedNote;
    
    @Schema(description = "完成備註", example = "完成了主要功能的 80%")
    @Size(max = 500, message = "完成備註不能超過 500 個字元")
    private String completionNote;
} 