package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionDto;
import com.sessionflow.dto.SessionLogDto;
import com.sessionflow.mapper.SessionLogMapper;
import com.sessionflow.mapper.SessionMapper;
import com.sessionflow.model.Session;
import com.sessionflow.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionMapperImpl implements SessionMapper {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SessionLogMapper sessionLogMapper;

    @Override
    public SessionDto toDto(Session session) {
        if (session == null) {
            return null;
        }

        var logs = session.getLogs() != null && !session.getLogs().isEmpty()
                ? sessionLogMapper.toDtoList(session.getLogs())
                : null;

        return new SessionDto(
                session.getId(),
                session.getTask().getId(),
                session.getDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.isCanPlay(),
                logs,
                session.getCreatedAt(),
                session.getUpdatedAt());
    }

    @Override
    public Session toEntity(SessionDto sessionDto) {
        if (sessionDto == null) {
            return null;
        }

        Session.SessionBuilder builder = Session.builder()
                .date(sessionDto.date())
                .startTime(sessionDto.startTime())
                .endTime(sessionDto.endTime())
                .canPlay(sessionDto.canPlay());

        // Set the task relationship
        if (sessionDto.taskId() != null) {
            builder.task(taskRepository.findById(sessionDto.taskId())
                    .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + sessionDto.taskId())));
        }

        // Logs will be handled separately when session is saved

        return builder.build();
    }

    @Override
    public List<SessionDto> toDtoList(List<Session> sessions) {
        if (sessions == null) {
            return null;
        }

        return sessions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> toEntityList(List<SessionDto> sessionDtos) {
        if (sessionDtos == null) {
            return null;
        }

        return sessionDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}