package com.sessionflow.service.impl;

import com.sessionflow.dto.SessionDto;
import com.sessionflow.exception.ResourceNotFoundException;
import com.sessionflow.mapper.SessionMapper;
import com.sessionflow.model.Session;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionMapper sessionMapper;

    @Override
    @Transactional
    public SessionDto createSession(SessionDto sessionDto) {
        Session session = sessionMapper.toEntity(sessionDto);
        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByDate(LocalDate date) {
        List<Session> sessions = sessionRepository.findByDateOrderByStartTime(date);
        return sessionMapper.toDtoList(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDto getSessionById(Long id) {
        Session session = sessionRepository.findByIdWithLogs(id);
        if (session == null) {
            throw new ResourceNotFoundException("Session not found with ID: " + id);
        }
        return sessionMapper.toDto(session);
    }

    @Override
    @Transactional
    public SessionDto updateSession(Long id, SessionDto sessionDto) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));

        // Update the fields
        if (sessionDto.date() != null) {
            existingSession.date = sessionDto.date();
        }
        if (sessionDto.startTime() != null) {
            existingSession.startTime = sessionDto.startTime();
        }
        if (sessionDto.endTime() != null) {
            existingSession.endTime = sessionDto.endTime();
        }

        existingSession.canPlay = sessionDto.canPlay();

        Session updatedSession = sessionRepository.save(existingSession);
        return sessionMapper.toDto(updatedSession);
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id));

        sessionRepository.delete(session);
    }
}