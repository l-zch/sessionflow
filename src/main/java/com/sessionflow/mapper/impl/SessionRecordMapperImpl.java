package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.mapper.SessionRecordMapper;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.model.Task;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionRecordMapperImpl implements SessionRecordMapper {

    @Override
    public SessionRecordResponse toResponse(SessionRecord sessionRecord) {
        if (sessionRecord == null) {
            return null;
        }

        SessionRecordResponse response = new SessionRecordResponse();
        response.setId(sessionRecord.getId());
        response.setTitle(sessionRecord.getTitle());
        response.setStartAt(sessionRecord.getStartAt());
        response.setEndAt(sessionRecord.getEndAt());
        response.setPlannedNote(sessionRecord.getPlannedNote());
        response.setCompletionNote(sessionRecord.getCompletionNote());

        // handle task
        if (sessionRecord.getTask() != null) {
            response.setTaskId(sessionRecord.getTask().getId());
        }
        return response;
    }

    @Override
    public List<SessionRecordResponse> toResponseList(List<SessionRecord> sessionRecords) {
        if (sessionRecords == null) {
            return Collections.emptyList();
        }

        return sessionRecords.stream()
            .map(this::toResponse)
            .toList();
    }
} 