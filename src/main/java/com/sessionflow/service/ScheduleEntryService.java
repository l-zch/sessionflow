package com.sessionflow.service;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleEntryService {
    
    /**
     * 建立排程
     */
    ScheduleEntryResponse createScheduleEntry(ScheduleEntryRequest request);
    
    /**
     * 更新排程
     */
    ScheduleEntryResponse updateScheduleEntry(Long id, ScheduleEntryRequest request);
    
    /**
     * 刪除排程
     */
    void deleteScheduleEntry(Long id);
    
    /**
     * 根據指定時間區間查詢所有排程
     */
    List<ScheduleEntryResponse> getScheduleEntries(LocalDate startDate, LocalDate endDate);
} 