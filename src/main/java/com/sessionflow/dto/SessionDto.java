package com.sessionflow.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record SessionDto(
        Long id,

        @NotNull(message = "Task ID is required") Long taskId,

        @NotNull(message = "Date is required") LocalDate date,

        @NotNull(message = "Start time is required") LocalTime startTime,

        @NotNull(message = "End time is required") LocalTime endTime,

        boolean canPlay,

        List<SessionLogDto> logs,

        LocalDateTime createdAt,

        LocalDateTime updatedAt) {
    // Constructor with defaults for collections
    public SessionDto {
        if (logs == null) {
            logs = new ArrayList<>();
        }
    }
}