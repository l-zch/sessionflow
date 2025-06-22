package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "標籤回應")
public class TagResponse {
    
    @NotNull
    @Schema(description = "標籤 ID", example = "1", nullable = false)
    private Long id;
    
    @NotNull
    @Schema(description = "標籤名稱", example = "工作", nullable = false)
    private String name;
    
    @NotNull
    @Schema(description = "標籤顏色（十六進位色碼）", example = "#FF5733", nullable = false)
    private String color;
} 