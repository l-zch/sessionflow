package com.sessionflow.mapper;

import com.sessionflow.dto.SessionDto;
import com.sessionflow.model.Session;

import java.util.List;

public interface SessionMapper {
    SessionDto toDto(Session session);

    Session toEntity(SessionDto sessionDto);

    List<SessionDto> toDtoList(List<Session> sessions);

    List<Session> toEntityList(List<SessionDto> sessionDtos);
}