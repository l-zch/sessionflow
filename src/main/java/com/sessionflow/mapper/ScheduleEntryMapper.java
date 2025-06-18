package com.sessionflow.mapper;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.model.ScheduleEntry;

import java.util.List;

public interface ScheduleEntryMapper {
    
    ScheduleEntry toEntity(ScheduleEntryRequest request);
    
    ScheduleEntryResponse toResponse(ScheduleEntry scheduleEntry);
    
    List<ScheduleEntryResponse> toResponseList(List<ScheduleEntry> scheduleEntries);
} 