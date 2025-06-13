package com.sessionflow.service;

import com.sessionflow.dto.SessionRecordCreateRequest;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;

import java.util.List;

public interface SessionService {
    
    /**
     * 建立工作階段
     */
    SessionResponse createSession(SessionRequest request);
    
    /**
     * 查詢所有目前存在的工作階段
     */
    List<SessionResponse> getAllSessions();
    
    /**
     * 結束工作階段並建立 SessionRecord
     * - 根據 ID 找出 Session
     * - 建立對應 SessionRecord（從 Session 帶入 title、task、note 為 plannedNote）
     * - 自動刪除該 Session
     * - 回傳 SessionRecordResponse
     */
    SessionRecordResponse endSession(Long sessionId, SessionRecordCreateRequest request);
    
    /**
     * 根據任務 ID 查詢相關的會話 ID 列表
     * 
     * @param taskId 任務 ID
     * @return 會話 ID 列表
     */
    List<Long> findIdsByTaskId(Long taskId);
    
    /**
     * 根據任務 ID 刪除相關的會話
     * 
     * @param taskId 任務 ID
     */
    void deleteByTaskId(Long taskId);
} 