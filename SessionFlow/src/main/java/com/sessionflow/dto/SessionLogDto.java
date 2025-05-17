package com.sessionflow.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SessionLogDto(
        Long id,

        @NotNull(message = "Session ID is required") Long sessionId,

        @NotNull(message = "Start time is required") LocalDateTime start,

        LocalDateTime end,

        Long duration, // in seconds

        String note) {
}