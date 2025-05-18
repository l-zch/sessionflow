package com.sessionflow.mapper.impl;

import com.sessionflow.dto.TaskDto;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapperImpl implements TaskMapper {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        Long parentId = null;
        if (task.getParent() != null) {
            parentId = task.getParent().getId();
        }

        List<TaskDto> children = null;
        if (task.getChildren() != null && !task.getChildren().isEmpty()) {
            children = toDtoList(task.getChildren());
        }

        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getEstimatedDuration(),
                task.getStatus(),
                parentId,
                children,
                null, // tags would be mapped by a TagMapper
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    @Override
    public Task toEntity(TaskDto taskDto) {
        if (taskDto == null) {
            return null;
        }

        Task.TaskBuilder builder = Task.builder()
                .title(taskDto.title())
                .description(taskDto.description())
                .estimatedDuration(taskDto.estimatedDuration())
                .status(taskDto.status() != null ? taskDto.status() : Task.TaskStatus.ACTIVE);

        // Handle parent relation if parentId is provided
        if (taskDto.parentId() != null) {
            builder.parent(taskRepository.findById(taskDto.parentId()).orElse(null));
        }

        // Children and tags would be handled separately
        // after the main entity is saved

        return builder.build();
    }

    @Override
    public List<TaskDto> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> toEntityList(List<TaskDto> taskDtos) {
        if (taskDtos == null) {
            return null;
        }

        return taskDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}