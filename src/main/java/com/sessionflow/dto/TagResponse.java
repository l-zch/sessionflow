package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "標籤回應")
public class TagResponse {
    
    @Schema(description = "標籤 ID", example = "1")
    private Long id;
    
    @Schema(description = "標籤名稱", example = "工作")
    private String name;
    
    @Schema(description = "標籤顏色（十六進位色碼）", example = "#FF5733")
    private String color;
} 