package com.sessionflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "標籤建立/更新請求")
public class TagRequest {
    
    @NotBlank(message = "Tag name cannot be blank")
    @Schema(description = "標籤名稱", example = "工作", required = true)
    private String name;
    
    @NotBlank(message = "Tag color cannot be blank")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    @Schema(description = "標籤顏色（十六進位色碼）", example = "#FF5733", required = true)
    private String color;
} 