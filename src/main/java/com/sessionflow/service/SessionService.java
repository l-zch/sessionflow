package com.sessionflow.service;

import com.sessionflow.dto.SessionDto;

import java.time.LocalDate;
import java.util.List;

public interface SessionService {

    /**
     * Create a new session
     */
    SessionDto createSession(SessionDto sessionDto);

    /**
     * Get sessions for a specific date
     */
    List<SessionDto> getSessionsByDate(LocalDate date);

    /**
     * Get a single session by ID with its logs
     */
    SessionDto getSessionById(Long id);

    /**
     * Update an existing session
     */
    SessionDto updateSession(Long id, SessionDto sessionDto);

    /**
     * Delete a session and its logs
     */
    void deleteSession(Long id);
}