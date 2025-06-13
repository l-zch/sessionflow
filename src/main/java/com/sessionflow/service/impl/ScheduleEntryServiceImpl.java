package com.sessionflow.service.impl;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.exception.InvalidTimeRangeException;
import com.sessionflow.exception.ScheduleEntryNotFoundException;
import com.sessionflow.mapper.ScheduleEntryMapper;
import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.repository.ScheduleEntryRepository;
import com.sessionflow.service.ScheduleEntryService;
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
public class ScheduleEntryServiceImpl implements ScheduleEntryService {
    
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ScheduleEntryResponse createScheduleEntry(ScheduleEntryRequest request) {
        log.info("建立排程 - title: {}", request.getTitle());
        
        // 驗證時間區間
        validateTimeRange(request.getStartAt(), request.getEndAt());
        
        ScheduleEntry scheduleEntry = scheduleEntryMapper.toEntity(request);
        ScheduleEntry savedEntry = scheduleEntryRepository.save(scheduleEntry);
        ScheduleEntryResponse response = scheduleEntryMapper.toResponse(savedEntry);
        
        // 發布 ScheduleEntry 建立事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.SCHEDULE_ENTRY_CREATE,
            savedEntry.getId(),
            null,
            response,
            null
        ));
        
        log.info("成功建立排程 - ID: {}", savedEntry.getId());
        return response;
    }
    
    @Override
    @Transactional
    public ScheduleEntryResponse updateScheduleEntry(Long id, ScheduleEntryRequest request) {
        log.info("更新排程 - ID: {}", id);
        
        // 驗證時間區間
        validateTimeRange(request.getStartAt(), request.getEndAt());
        
        ScheduleEntry scheduleEntry = scheduleEntryRepository.findById(id)
                .orElseThrow(() -> new ScheduleEntryNotFoundException(id));
        
        scheduleEntryMapper.updateEntityFromRequest(request, scheduleEntry);
        ScheduleEntry savedEntry = scheduleEntryRepository.save(scheduleEntry);
        ScheduleEntryResponse response = scheduleEntryMapper.toResponse(savedEntry);
        
        // 發布 ScheduleEntry 更新事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.SCHEDULE_ENTRY_UPDATE,
            savedEntry.getId(),
            null,
            response,
            null
        ));
        
        log.info("成功更新排程 - ID: {}", savedEntry.getId());
        return response;
    }
    
    @Override
    @Transactional
    public void deleteScheduleEntry(Long id) {
        log.info("刪除排程 - ID: {}", id);
        
        if (!scheduleEntryRepository.existsById(id)) {
            throw new ScheduleEntryNotFoundException(id);
        }
        
        scheduleEntryRepository.deleteById(id);
        
        // 發布 ScheduleEntry 刪除事件
        eventPublisher.publishEvent(new ResourceChangedEvent<ScheduleEntryResponse>(
            NotificationType.SCHEDULE_ENTRY_DELETE,
            id,
            null,
            null,
            null
        ));
        
        log.info("成功刪除排程 - ID: {}", id);
    }
    
    @Override
    public List<ScheduleEntryResponse> getScheduleEntries(LocalDate startDate, LocalDate endDate) {
        log.info("查詢排程 - startDate: {}, endDate: {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // 包含結束日期的整天
        
        List<ScheduleEntry> scheduleEntries = scheduleEntryRepository.findByDateRange(startDateTime, endDateTime);
        log.debug("找到 {} 筆排程", scheduleEntries.size());
        
        return scheduleEntryMapper.toResponseList(scheduleEntries);
    }
    
    @Override
    public List<Long> findIdsByTaskId(Long taskId) {
        log.debug("Finding schedule entry IDs by task ID: {}", taskId);
        
        List<ScheduleEntry> scheduleEntries = scheduleEntryRepository.findByTaskId(taskId);
        List<Long> scheduleEntryIds = scheduleEntries.stream()
                .map(ScheduleEntry::getId)
                .toList();
        
        log.debug("Found {} schedule entries for task ID: {}", scheduleEntryIds.size(), taskId);
        return scheduleEntryIds;
    }
    
    @Override
    @Transactional
    public void deleteByTaskId(Long taskId) {
        log.info("Deleting schedule entries by task ID: {}", taskId);
        
        scheduleEntryRepository.deleteByTaskId(taskId);
        
        log.info("Successfully deleted schedule entries for task ID: {}", taskId);
    }
    
    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new InvalidTimeRangeException("結束時間必須晚於開始時間");
        }
    }
} 