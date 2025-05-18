package com.sessionflow.service.impl;

import com.sessionflow.dto.SessionLogDto;
import com.sessionflow.exception.ResourceNotFoundException;
import com.sessionflow.mapper.SessionLogMapper;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionLog;
import com.sessionflow.repository.SessionLogRepository;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.service.SessionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionLogServiceImpl implements SessionLogService {

    @Autowired
    private SessionLogRepository sessionLogRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionLogMapper sessionLogMapper;

    @Override
    @Transactional
    public SessionLogDto startSessionLog(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        // Create a new session log with current time as start
        SessionLog sessionLog = SessionLog.builder()
                .session(session)
                .startTime(LocalDateTime.now())
                .build();

        SessionLog savedLog = sessionLogRepository.save(sessionLog);
        return sessionLogMapper.toDto(savedLog);
    }

    @Override
    @Transactional
    public SessionLogDto updateSessionLog(Long id, SessionLogDto sessionLogDto) {
        SessionLog existingLog = sessionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session log not found with ID: " + id));

        // Update the fields
        if (sessionLogDto.endTime() != null) {
            existingLog.setEndTime(sessionLogDto.endTime());
        }

        if (sessionLogDto.note() != null) {
            existingLog.setNote(sessionLogDto.note());
        }

        // If manually setting duration
        if (sessionLogDto.duration() != null) {
            existingLog.setDuration(sessionLogDto.duration());
        }

        SessionLog updatedLog = sessionLogRepository.save(existingLog);
        return sessionLogMapper.toDto(updatedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionLogDto> getLogsBySessionId(Long sessionId) {
        // Check if session exists
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        List<SessionLog> logs = sessionLogRepository.findBySessionIdOrderByStartAsc(sessionId);
        return sessionLogMapper.toDtoList(logs);
    }

    @Override
    @Transactional
    public void deleteSessionLog(Long id) {
        SessionLog log = sessionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session log not found with ID: " + id));

        sessionLogRepository.delete(log);
    }
}