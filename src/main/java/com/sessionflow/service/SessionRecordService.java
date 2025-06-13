package com.sessionflow.service;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRecordUpdateRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 工作階段紀錄服務介面
 */
public interface SessionRecordService {
    
    /**
     * 根據時間區間與 taskId 查詢 SessionRecord
     * 所有參數皆為可選，支援任意組合
     * 
     * @param startDate 開始日期（可選）
     * @param endDate 結束日期（可選）
     * @param taskId 任務 ID（可選）
     * @return 工作階段紀錄列表
     */
    List<SessionRecordResponse> getSessionRecords(LocalDate startDate, LocalDate endDate, Long taskId);
    
    /**
     * 更新指定 SessionRecord
     * 
     * @param id SessionRecord ID
     * @param updateRequest 更新請求
     * @return 更新後的工作階段紀錄
     */
    SessionRecordResponse updateSessionRecord(Long id, SessionRecordUpdateRequest updateRequest);
    
    /**
     * 刪除指定 SessionRecord
     * 
     * @param id SessionRecord ID
     */
    void deleteSessionRecord(Long id);
    
    /**
     * 根據任務 ID 查詢相關的會話記錄 ID 列表
     * 
     * @param taskId 任務 ID
     * @return 會話記錄 ID 列表
     */
    List<Long> findIdsByTaskId(Long taskId);
    
    /**
     * 根據任務 ID 刪除相關的會話記錄
     * 
     * @param taskId 任務 ID
     */
    void deleteByTaskId(Long taskId);
} 