package com.sessionflow.mapper.impl;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.mapper.ScheduleEntryMapper;
import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleEntryMapperImpl implements ScheduleEntryMapper {
    
    private final TaskRepository taskRepository;
    
    @Override
    public ScheduleEntry toEntity(ScheduleEntryRequest request) {
        ScheduleEntry scheduleEntry = new ScheduleEntry(
                request.getTitle(),
                request.getStartAt(),
                request.getEndAt()
        );
        
        scheduleEntry.setNote(request.getNote());
        
        // 設定關聯的任務
        if (request.getTaskId() != null) {
            Task task = taskRepository.findById(request.getTaskId()).orElse(null);
            scheduleEntry.setTask(task);
        }
        
        return scheduleEntry;
    }
    
    @Override
    public ScheduleEntryResponse toResponse(ScheduleEntry scheduleEntry) {
        ScheduleEntryResponse response = new ScheduleEntryResponse();
        response.setId(scheduleEntry.getId());
        response.setTitle(scheduleEntry.getTitle());
        response.setStartAt(scheduleEntry.getStartAt());
        response.setEndAt(scheduleEntry.getEndAt());
        response.setNote(scheduleEntry.getNote());
        
        // 設定任務 ID
        if (scheduleEntry.getTask() != null) {
            response.setTaskId(scheduleEntry.getTask().getId());
        }
        
        return response;
    }
    
    @Override
    public List<ScheduleEntryResponse> toResponseList(List<ScheduleEntry> scheduleEntries) {
        return scheduleEntries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateEntityFromRequest(ScheduleEntryRequest request, ScheduleEntry scheduleEntry) {
        scheduleEntry.setTitle(request.getTitle());
        scheduleEntry.setStartAt(request.getStartAt());
        scheduleEntry.setEndAt(request.getEndAt());
        scheduleEntry.setNote(request.getNote());
        
        // 更新關聯的任務
        if (request.getTaskId() != null) {
            Task task = taskRepository.findById(request.getTaskId()).orElse(null);
            scheduleEntry.setTask(task);
        } else {
            scheduleEntry.setTask(null);
        }
    }
} 