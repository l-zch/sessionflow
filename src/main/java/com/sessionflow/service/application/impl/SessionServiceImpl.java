package com.sessionflow.service.application.impl;

import com.sessionflow.dto.SessionRecordCreateRequest;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.exception.SessionNotFoundException;
import com.sessionflow.mapper.SessionMapper;
import com.sessionflow.mapper.SessionRecordMapper;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.repository.SessionRecordRepository;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {
    
    private final SessionRepository sessionRepository;
    private final SessionRecordRepository sessionRecordRepository;
    private final SessionMapper sessionMapper;
    private final SessionRecordMapper sessionRecordMapper;
    
    @Override
    public SessionResponse createSession(SessionRequest request) {
        log.info("Creating new session with title: {}", request.getTitle());
        
        // 轉換為實體並儲存
        Session session = sessionMapper.toEntity(request);
        Session savedSession = sessionRepository.save(session);
        
        log.info("Successfully created session with id: {}", savedSession.getId());
        
        // 轉換為回應 DTO
        return sessionMapper.toResponse(savedSession);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getAllSessions() {
        log.info("Retrieving all sessions");
        
        List<Session> sessions = sessionRepository.findAllByOrderByIdDesc();
        
        log.info("Found {} sessions", sessions.size());
        
        return sessionMapper.toResponseList(sessions);
    }
    
    @Override
    public SessionRecordResponse endSession(Long sessionId, SessionRecordCreateRequest request) {
        log.info("Ending session with id: {}", sessionId);
        
        // 查找工作階段
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        // 建立 SessionRecord
        SessionRecord sessionRecord = sessionRecordMapper.createFromSession(
                session, request.getCompletionNote());
        SessionRecord savedRecord = sessionRecordRepository.save(sessionRecord);
        
        // 刪除原始 Session
        sessionRepository.delete(session);
        
        log.info("Successfully ended session {} and created record {}", 
                sessionId, savedRecord.getId());
        
        // 回傳 SessionRecordResponse
        return sessionRecordMapper.toResponse(savedRecord);
    }
} 