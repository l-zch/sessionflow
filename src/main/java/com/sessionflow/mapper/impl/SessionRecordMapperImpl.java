package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.mapper.SessionRecordMapper;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SessionRecordMapperImpl implements SessionRecordMapper {
    
    @Override
    public SessionRecord createFromSession(Session session, String completionNote) {
        if (session == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        SessionRecord record = new SessionRecord(session.getTitle(), now, now);
        
        // 設定任務關聯
        if (session.getTask() != null) {
            record.setTask(session.getTask());
        }
        
        // 設定計畫備註（來自 Session 的 note）
        record.setPlannedNote(session.getNote());
        
        // 設定完成備註
        record.setCompletionNote(completionNote);
        
        log.debug("Created SessionRecord from Session: {}", session.getId());
        
        return record;
    }
    
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
        
        // 設定任務 ID
        if (sessionRecord.getTask() != null) {
            response.setTaskId(sessionRecord.getTask().getId());
        }
        
        return response;
    }
    
    @Override
    public List<SessionRecordResponse> toResponseList(List<SessionRecord> sessionRecords) {
        if (sessionRecords == null) {
            return List.of();
        }
        
        return sessionRecords.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
} 