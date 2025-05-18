package com.sessionflow.dto;

import com.sessionflow.model.Task;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record TaskDto(
        Long id,

        @NotBlank(message = "Title is required") String title,

        String description,

        Integer estimatedDuration,

        Task.TaskStatus status,

        Long parentId,

        List<TaskDto> children,

        Set<TagDto> tags,

        LocalDateTime createdAt,

        LocalDateTime updatedAt) {
    // Constructor with defaults for collections
    public TaskDto {
        if (children == null) {
            children = new ArrayList<>();
        }

        if (tags == null) {
            tags = new HashSet<>();
        }
    }
}