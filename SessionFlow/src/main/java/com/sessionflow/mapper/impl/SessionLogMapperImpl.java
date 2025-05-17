package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionLogDto;
import com.sessionflow.mapper.SessionLogMapper;
import com.sessionflow.model.SessionLog;
import com.sessionflow.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionLogMapperImpl implements SessionLogMapper {

    @Autowired
    private SessionRepository sessionRepository;

    @Override
    public SessionLogDto toDto(SessionLog sessionLog) {
        if (sessionLog == null) {
            return null;
        }

        return new SessionLogDto(
                sessionLog.id,
                sessionLog.session.id,
                sessionLog.start,
                sessionLog.end,
                sessionLog.duration,
                sessionLog.note);
    }

    @Override
    public SessionLog toEntity(SessionLogDto sessionLogDto) {
        if (sessionLogDto == null) {
            return null;
        }

        SessionLog.SessionLogBuilder builder = SessionLog.builder()
                .start(sessionLogDto.start())
                .end(sessionLogDto.end())
                .duration(sessionLogDto.duration())
                .note(sessionLogDto.note());

        // Set the session relationship
        if (sessionLogDto.sessionId() != null) {
            builder.session(sessionRepository.findById(sessionLogDto.sessionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Session not found with ID: " + sessionLogDto.sessionId())));
        }

        return builder.build();
    }

    @Override
    public List<SessionLogDto> toDtoList(List<SessionLog> sessionLogs) {
        if (sessionLogs == null) {
            return null;
        }

        return sessionLogs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionLog> toEntityList(List<SessionLogDto> sessionLogDtos) {
        if (sessionLogDtos == null) {
            return null;
        }

        return sessionLogDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}