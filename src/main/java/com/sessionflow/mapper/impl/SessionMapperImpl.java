package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.mapper.SessionMapper;
import com.sessionflow.model.Session;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionMapperImpl implements SessionMapper {
    
    private final TaskRepository taskRepository;
    
    @Override
    public Session toEntity(SessionRequest request) {
        if (request == null) {
            return null;
        }
        
        Session session = new Session(request.getTitle());
        session.setEndReminder(request.getEndReminder());
        session.setNote(request.getNote());
        
        // 處理任務關聯
        if (request.getTaskId() != null) {
            Task task = taskRepository.findById(request.getTaskId())
                    .orElse(null);
            if (task != null) {
                session.setTask(task);
                log.debug("Associated session with task: {}", task.getId());
            } else {
                log.warn("Task with id {} not found, session will not be associated with any task", request.getTaskId());
            }
        }
        
        return session;
    }
    
    @Override
    public SessionResponse toResponse(Session session) {
        if (session == null) {
            return null;
        }
        
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setStartTime(session.getStartTime());
        response.setEndReminder(session.getEndReminder());
        response.setNote(session.getNote());
        
        // 設定任務 ID
        if (session.getTask() != null) {
            response.setTaskId(session.getTask().getId());
        }
        
        return response;
    }
    
    @Override
    public List<SessionResponse> toResponseList(List<Session> sessions) {
        if (sessions == null) {
            return Collections.emptyList();
        }
        return sessions.stream()
            .map(this::toResponse)
            .toList();
    }
} 