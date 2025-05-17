package com.sessionflow.mapper;

import com.sessionflow.dto.SessionLogDto;
import com.sessionflow.model.SessionLog;

import java.util.List;

public interface SessionLogMapper {
    SessionLogDto toDto(SessionLog sessionLog);

    SessionLog toEntity(SessionLogDto sessionLogDto);

    List<SessionLogDto> toDtoList(List<SessionLog> sessionLogs);

    List<SessionLog> toEntityList(List<SessionLogDto> sessionLogDtos);
}