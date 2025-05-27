package com.sessionflow.mapper;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;

import java.util.List;

public interface SessionRecordMapper {
    
    /**
     * 從 Session 建立 SessionRecord
     */
    SessionRecord createFromSession(Session session, String completionNote);
    
    /**
     * 將 SessionRecord 實體轉換為 SessionRecordResponse
     */
    SessionRecordResponse toResponse(SessionRecord sessionRecord);
    
    /**
     * 將 SessionRecord 列表轉換為 SessionRecordResponse 列表
     */
    List<SessionRecordResponse> toResponseList(List<SessionRecord> sessionRecords);
} 