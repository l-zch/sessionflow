package com.sessionflow.service.impl;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRecordUpdateRequest;
import com.sessionflow.exception.SessionRecordNotFoundException;
import com.sessionflow.mapper.SessionRecordMapper;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.repository.SessionRecordRepository;
import com.sessionflow.service.SessionRecordService;
import com.sessionflow.event.ResourceChangedEvent;
import com.sessionflow.common.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionRecordServiceImpl implements SessionRecordService {
    
    private final SessionRecordRepository sessionRecordRepository;
    private final SessionRecordMapper sessionRecordMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public List<SessionRecordResponse> getSessionRecords(LocalDate startDate, LocalDate endDate, Long taskId) {
        log.info("查詢工作階段紀錄 - startDate: {}, endDate: {}, taskId: {}", startDate, endDate, taskId);
        
        List<SessionRecord> sessionRecords;
        
        // 根據不同的查詢條件組合選擇適當的查詢方法
        if (startDate != null && endDate != null && taskId != null) {
            // 同時有日期區間和 taskId
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // 包含結束日期的整天
            sessionRecords = sessionRecordRepository.findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId);
            log.debug("使用日期區間和任務ID查詢，找到 {} 筆紀錄", sessionRecords.size());
        } else if (startDate != null || endDate != null) {
            // 有日期條件（startDate 或 endDate 或兩者都有）
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
            LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.of(2099, 12, 31).atStartOfDay();
            
            if (taskId != null) {
                sessionRecords = sessionRecordRepository.findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId);
                log.debug("使用日期條件和任務ID查詢，找到 {} 筆紀錄", sessionRecords.size());
            } else {
                sessionRecords = sessionRecordRepository.findByStartAtBetween(startDateTime, endDateTime);
                log.debug("使用日期條件查詢，找到 {} 筆紀錄", sessionRecords.size());
            }
        } else if (taskId != null) {
            // 只有 taskId
            sessionRecords = sessionRecordRepository.findByTaskId(taskId);
            log.debug("使用任務ID查詢，找到 {} 筆紀錄", sessionRecords.size());
        } else {
            // 沒有任何條件，查詢所有
            sessionRecords = sessionRecordRepository.findAllByOrderByIdDesc();
            log.debug("查詢所有紀錄，找到 {} 筆紀錄", sessionRecords.size());
        }
        
        return sessionRecordMapper.toResponseList(sessionRecords);
    }
    
    @Override
    @Transactional
    public SessionRecordResponse updateSessionRecord(Long id, SessionRecordUpdateRequest updateRequest) {
        log.info("更新工作階段紀錄 - ID: {}", id);
        
        SessionRecord sessionRecord = sessionRecordRepository.findById(id)
                .orElseThrow(() -> new SessionRecordNotFoundException(id));
        
        // 更新欄位
        if (updateRequest.getPlannedNote() != null) {
            sessionRecord.setPlannedNote(updateRequest.getPlannedNote());
            log.debug("更新計畫備註: {}", updateRequest.getPlannedNote());
        }
        
        if (updateRequest.getCompletionNote() != null) {
            sessionRecord.setCompletionNote(updateRequest.getCompletionNote());
            log.debug("更新完成備註: {}", updateRequest.getCompletionNote());
        }
        
        SessionRecord savedRecord = sessionRecordRepository.save(sessionRecord);
        SessionRecordResponse response = sessionRecordMapper.toResponse(savedRecord);
        
        // 發布 SessionRecord 更新事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.SESSION_RECORD_UPDATE,
            savedRecord.getId(),
            null,
            response,
            null
        ));
        
        log.info("成功更新工作階段紀錄 - ID: {}", savedRecord.getId());
        
        return response;
    }
    
    @Override
    @Transactional
    public void deleteSessionRecord(Long id) {
        log.info("刪除工作階段紀錄 - ID: {}", id);
        
        if (!sessionRecordRepository.existsById(id)) {
            throw new SessionRecordNotFoundException(id);
        }
        
        sessionRecordRepository.deleteById(id);
        
        // 發布 SessionRecord 刪除事件
        eventPublisher.publishEvent(new ResourceChangedEvent<SessionRecordResponse>(
            NotificationType.SESSION_RECORD_DELETE,
            id,
            null,
            null,
            null
        ));
        
        log.info("成功刪除工作階段紀錄 - ID: {}", id);
    }
    
    @Override
    public List<Long> findIdsByTaskId(Long taskId) {
        log.debug("Finding session record IDs by task ID: {}", taskId);
        
        List<SessionRecord> sessionRecords = sessionRecordRepository.findByTaskId(taskId);
        List<Long> sessionRecordIds = sessionRecords.stream()
                .map(SessionRecord::getId)
                .toList();
        
        log.debug("Found {} session records for task ID: {}", sessionRecordIds.size(), taskId);
        return sessionRecordIds;
    }
    
    @Override
    @Transactional
    public void deleteByTaskId(Long taskId) {
        log.info("Deleting session records by task ID: {}", taskId);
        
        sessionRecordRepository.deleteByTaskId(taskId);
        
        log.info("Successfully deleted session records for task ID: {}", taskId);
    }
} 