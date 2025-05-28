package com.sessionflow.service.impl;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.exception.InvalidTimeRangeException;
import com.sessionflow.exception.ScheduleEntryNotFoundException;
import com.sessionflow.mapper.ScheduleEntryMapper;
import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.repository.ScheduleEntryRepository;
import com.sessionflow.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @Override
    @Transactional
    public ScheduleEntryResponse createScheduleEntry(ScheduleEntryRequest request) {
        log.info("建立排程 - title: {}", request.getTitle());
        
        // 驗證時間區間
        validateTimeRange(request.getStartAt(), request.getEndAt());
        
        ScheduleEntry scheduleEntry = scheduleEntryMapper.toEntity(request);
        ScheduleEntry savedEntry = scheduleEntryRepository.save(scheduleEntry);
        
        log.info("成功建立排程 - ID: {}", savedEntry.getId());
        return scheduleEntryMapper.toResponse(savedEntry);
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
        
        log.info("成功更新排程 - ID: {}", savedEntry.getId());
        return scheduleEntryMapper.toResponse(savedEntry);
    }
    
    @Override
    @Transactional
    public void deleteScheduleEntry(Long id) {
        log.info("刪除排程 - ID: {}", id);
        
        if (!scheduleEntryRepository.existsById(id)) {
            throw new ScheduleEntryNotFoundException(id);
        }
        
        scheduleEntryRepository.deleteById(id);
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
    
    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new InvalidTimeRangeException("結束時間必須晚於開始時間");
        }
    }
} 