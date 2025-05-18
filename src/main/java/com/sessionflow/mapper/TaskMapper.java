package com.sessionflow.mapper;

import com.sessionflow.dto.TaskDto;
import com.sessionflow.model.Task;

import java.util.List;

public interface TaskMapper {
    TaskDto toDto(Task task);

    Task toEntity(TaskDto taskDto);

    List<TaskDto> toDtoList(List<Task> tasks);

    List<Task> toEntityList(List<TaskDto> taskDtos);
}