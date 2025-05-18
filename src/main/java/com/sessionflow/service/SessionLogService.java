package com.sessionflow.service;

import com.sessionflow.dto.SessionLogDto;

import java.util.List;

public interface SessionLogService {

    /**
     * Start a new session log (start timing)
     */
    SessionLogDto startSessionLog(Long sessionId);

    /**
     * Update a session log (stop/pause, add note)
     */
    SessionLogDto updateSessionLog(Long id, SessionLogDto sessionLogDto);

    /**
     * Get all logs for a session
     */
    List<SessionLogDto> getLogsBySessionId(Long sessionId);

    /**
     * Delete a session log
     */
    void deleteSessionLog(Long id);
}