package com.sessionflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.sessionflow.event.ResourceChangedEvent;
import com.sessionflow.common.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServiceImpl implements SessionService {
    
    private final SessionRepository sessionRepository;
    private final SessionRecordRepository sessionRecordRepository;
    private final SessionMapper sessionMapper;
    private final SessionRecordMapper sessionRecordMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    public SessionResponse createSession(SessionRequest request) {
        log.info("Creating new session with title: {}", request.getTitle());
        
        // 轉換為實體並儲存
        Session session = sessionMapper.toEntity(request);
        Session savedSession = sessionRepository.save(session);
        SessionResponse response = sessionMapper.toResponse(savedSession);
        
        // 發布 Session 建立事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.SESSION_CREATE,
            savedSession.getId(),
            null,
            response,
            null
        ));
        
        log.info("Successfully created session with id: {}", savedSession.getId());
        
        return response;
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
        
        // 業務邏輯：直接在此創建 SessionRecord 實體
        SessionRecord sessionRecord = new SessionRecord();
        sessionRecord.setTitle(session.getTitle());
        sessionRecord.setStartAt(session.getStartTime());
        sessionRecord.setEndAt(LocalDateTime.now()); // 結束時間由業務邏輯定義
        sessionRecord.setPlannedNote(session.getNote());
        sessionRecord.setCompletionNote(request.getCompletionNote());
        sessionRecord.setTask(session.getTask());

        SessionRecord savedRecord = sessionRecordRepository.save(sessionRecord);
        SessionRecordResponse recordResponse = sessionRecordMapper.toResponse(savedRecord);
        
        // 發布 SessionRecord 建立事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.SESSION_RECORD_CREATE,
            savedRecord.getId(),
            null,
            recordResponse,
            null
        ));
        
        // 刪除原始 Session
        sessionRepository.delete(session);
        
        // 發布 Session 刪除事件
        eventPublisher.publishEvent(new ResourceChangedEvent<SessionResponse>(
            NotificationType.SESSION_DELETE,
            sessionId,
            null,
            null,
            null
        ));
        
        log.info("Successfully ended session {} and created record {}", 
                sessionId, savedRecord.getId());
        
        return recordResponse;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Long> findIdsByTaskId(Long taskId) {
        log.debug("Finding session IDs by task ID: {}", taskId);
        
        List<Session> sessions = sessionRepository.findByTaskId(taskId);
        List<Long> sessionIds = sessions.stream()
                .map(Session::getId)
                .toList();
        
        log.debug("Found {} sessions for task ID: {}", sessionIds.size(), taskId);
        return sessionIds;
    }
    
    @Override
    public void deleteByTaskId(Long taskId) {
        log.info("Deleting sessions by task ID: {}", taskId);
        
        sessionRepository.deleteByTaskId(taskId);
        
        log.info("Successfully deleted sessions for task ID: {}", taskId);
    }
} 