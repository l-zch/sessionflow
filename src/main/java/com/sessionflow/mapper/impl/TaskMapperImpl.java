package com.sessionflow.mapper.impl;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.mapper.TagMapper;
import com.sessionflow.model.Tag;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskMapperImpl implements TaskMapper {
    
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    
    @Override
    public Task toEntity(TaskRequest taskRequest) {
        if (taskRequest == null) {
            return null;
        }
        
        Task task = new Task(taskRequest.getTitle());
        task.setDueAt(taskRequest.getDueAt());
        task.setNote(taskRequest.getNote());
        
        // 處理標籤關聯
        if (taskRequest.getTagIds() != null && !taskRequest.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : taskRequest.getTagIds()) {
                tagRepository.findById(tagId).ifPresent(tags::add);
            }
            task.setTags(tags);
        }
        
        return task;
    }
    
    @Override
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDueAt(task.getDueAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setNote(task.getNote());
        response.setStatus(task.getStatus().getValue());
        
        // 轉換標籤
        response.setTags(task.getTags() != null && !task.getTags().isEmpty()
                ? task.getTags().stream()
                        .map(tagMapper::toResponse)
                        .collect(Collectors.toList())
                : List.of());
        
        return response;
    }
    
    @Override
    public List<TaskResponse> toResponseList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }
        
        return tasks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
} 