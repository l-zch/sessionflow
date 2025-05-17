package com.sessionflow.dto;

import javax.validation.constraints.NotBlank;

public record TagDto(
        Long id,

        @NotBlank(message = "Tag name is required") String name,

        String color) {
}