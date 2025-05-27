package com.sessionflow.mapper;

import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.model.Session;

import java.util.List;

public interface SessionMapper {
    
    /**
     * 將 SessionRequest 轉換為 Session 實體
     */
    Session toEntity(SessionRequest request);
    
    /**
     * 將 Session 實體轉換為 SessionResponse
     */
    SessionResponse toResponse(Session session);
    
    /**
     * 將 Session 實體列表轉換為 SessionResponse 列表
     */
    List<SessionResponse> toResponseList(List<Session> sessions);
    
    /**
     * 更新 Session 實體的欄位（從 SessionRequest）
     */
    void updateEntityFromRequest(Session session, SessionRequest request);
} 