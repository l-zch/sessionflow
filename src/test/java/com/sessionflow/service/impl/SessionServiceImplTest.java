package com.sessionflow.service.impl;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService 單元測試")
class SessionServiceImplTest {
    
    @Mock
    private SessionRepository sessionRepository;
    
    @Mock
    private SessionRecordRepository sessionRecordRepository;
    
    @Mock
    private SessionMapper sessionMapper;
    
    @Mock
    private SessionRecordMapper sessionRecordMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private SessionServiceImpl sessionService;
    
    private SessionRequest sessionRequest;
    private Session session;
    private SessionResponse sessionResponse;
    private SessionRecordCreateRequest recordCreateRequest;
    private SessionRecord sessionRecord;
    private SessionRecordResponse recordResponse;
    
    @BeforeEach
    void setUp() {
        sessionRequest = new SessionRequest("專案開發時間");
        sessionRequest.setNote("專注於核心功能開發");
        
        session = new Session("專案開發時間");
        session.setId(1L);
        session.setNote("專注於核心功能開發");
        
        sessionResponse = new SessionResponse();
        sessionResponse.setId(1L);
        sessionResponse.setTitle("專案開發時間");
        sessionResponse.setNote("專注於核心功能開發");
        
        recordCreateRequest = new SessionRecordCreateRequest();
        recordCreateRequest.setSessionId(1L);
        recordCreateRequest.setCompletionNote("完成了主要功能的 80%");
        
        sessionRecord = new SessionRecord("專案開發時間", LocalDateTime.now(), LocalDateTime.now());
        sessionRecord.setId(1L);
        sessionRecord.setPlannedNote("專注於核心功能開發");
        sessionRecord.setCompletionNote("完成了主要功能的 80%");
        
        recordResponse = new SessionRecordResponse();
        recordResponse.setId(1L);
        recordResponse.setTitle("專案開發時間");
        recordResponse.setPlannedNote("專注於核心功能開發");
        recordResponse.setCompletionNote("完成了主要功能的 80%");
    }
    
    @Test
    @DisplayName("成功建立工作階段")
    void createSession_Success() {
        // Given
        when(sessionMapper.toEntity(sessionRequest)).thenReturn(session);
        when(sessionRepository.save(session)).thenReturn(session);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);
        
        // When
        SessionResponse result = sessionService.createSession(sessionRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getNote()).isEqualTo("專注於核心功能開發");
        
        verify(sessionMapper).toEntity(sessionRequest);
        verify(sessionRepository).save(session);
        verify(sessionMapper).toResponse(session);
    }
    
    @Test
    @DisplayName("查詢所有工作階段成功")
    void getAllSessions_Success() {
        // Given
        List<Session> sessions = List.of(session);
        List<SessionResponse> expectedResponses = List.of(sessionResponse);
        
        when(sessionRepository.findAllByOrderByIdDesc()).thenReturn(sessions);
        when(sessionMapper.toResponseList(sessions)).thenReturn(expectedResponses);
        
        // When
        List<SessionResponse> result = sessionService.getAllSessions();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("專案開發時間");
        
        verify(sessionRepository).findAllByOrderByIdDesc();
        verify(sessionMapper).toResponseList(sessions);
    }
    
    @Test
    @DisplayName("結束工作階段成功")
    void endSession_Success() {
        // Given
        Long sessionId = 1L;
        
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRecordMapper.createFromSession(session, recordCreateRequest.getCompletionNote()))
                .thenReturn(sessionRecord);
        when(sessionRecordRepository.save(sessionRecord)).thenReturn(sessionRecord);
        when(sessionRecordMapper.toResponse(sessionRecord)).thenReturn(recordResponse);
        doNothing().when(sessionRepository).delete(session);
        
        // When
        SessionRecordResponse result = sessionService.endSession(sessionId, recordCreateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getPlannedNote()).isEqualTo("專注於核心功能開發");
        assertThat(result.getCompletionNote()).isEqualTo("完成了主要功能的 80%");
        
        verify(sessionRepository).findById(sessionId);
        verify(sessionRecordMapper).createFromSession(session, recordCreateRequest.getCompletionNote());
        verify(sessionRecordRepository).save(sessionRecord);
        verify(sessionRepository).delete(session);
        verify(sessionRecordMapper).toResponse(sessionRecord);
    }
    
    @Test
    @DisplayName("結束不存在的工作階段，應丟出 SessionNotFoundException")
    void endSession_SessionNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        
        when(sessionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> sessionService.endSession(nonExistentId, recordCreateRequest))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessage("Session with id 999 not found");
        
        verify(sessionRepository).findById(nonExistentId);
        verify(sessionRecordMapper, never()).createFromSession(any(), any());
        verify(sessionRecordRepository, never()).save(any());
        verify(sessionRepository, never()).delete(any());
    }
    
    @Test
    @DisplayName("成功建立包含任務關聯的工作階段")
    void createSession_WithTaskId_Success() {
        // Given
        SessionRequest requestWithTask = new SessionRequest("有任務的工作階段");
        requestWithTask.setTaskId(1L);
        requestWithTask.setNote("專注於任務完成");
        
        Session sessionWithTask = new Session("有任務的工作階段");
        sessionWithTask.setId(1L);
        sessionWithTask.setNote("專注於任務完成");
        
        SessionResponse responseWithTask = new SessionResponse();
        responseWithTask.setId(1L);
        responseWithTask.setTitle("有任務的工作階段");
        responseWithTask.setTaskId(1L);
        responseWithTask.setNote("專注於任務完成");
        
        when(sessionMapper.toEntity(requestWithTask)).thenReturn(sessionWithTask);
        when(sessionRepository.save(sessionWithTask)).thenReturn(sessionWithTask);
        when(sessionMapper.toResponse(sessionWithTask)).thenReturn(responseWithTask);
        
        // When
        SessionResponse result = sessionService.createSession(requestWithTask);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("有任務的工作階段");
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getNote()).isEqualTo("專注於任務完成");
        
        verify(sessionMapper).toEntity(requestWithTask);
        verify(sessionRepository).save(sessionWithTask);
        verify(sessionMapper).toResponse(sessionWithTask);
    }

    @Test
    @DisplayName("根據任務ID查詢工作階段ID列表成功")
    void findIdsByTaskId_Success() {
        // Given
        Long taskId = 1L;
        Session session2 = new Session("另一個工作階段");
        session2.setId(2L);
        List<Session> sessions = List.of(session, session2);

        when(sessionRepository.findByTaskId(taskId)).thenReturn(sessions);

        // When
        List<Long> result = sessionService.findIdsByTaskId(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(1L, 2L);

        verify(sessionRepository).findByTaskId(taskId);
    }

    @Test
    @DisplayName("根據任務ID查詢工作階段ID列表 - 無結果")
    void findIdsByTaskId_EmptyResult() {
        // Given
        Long taskId = 999L;
        List<Session> emptySessions = List.of();

        when(sessionRepository.findByTaskId(taskId)).thenReturn(emptySessions);

        // When
        List<Long> result = sessionService.findIdsByTaskId(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(sessionRepository).findByTaskId(taskId);
    }

    @Test
    @DisplayName("根據任務ID刪除工作階段成功")
    void deleteByTaskId_Success() {
        // Given
        Long taskId = 1L;

        doNothing().when(sessionRepository).deleteByTaskId(taskId);

        // When
        sessionService.deleteByTaskId(taskId);

        // Then
        verify(sessionRepository).deleteByTaskId(taskId);
    }
} 