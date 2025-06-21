package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.model.Session;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionMapper 單元測試")
class SessionMapperImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SessionMapperImpl sessionMapper;

    private SessionRequest sessionRequest;
    private Session session;
    private Task task;

    @BeforeEach
    void setUp() {
        // 建立測試用的 Task
        task = new Task("完成專案文件");
        task.setId(1L);

        // 建立測試用的 SessionRequest
        sessionRequest = new SessionRequest();
        sessionRequest.setTitle("專案開發時間");
        sessionRequest.setTaskId(1L);
        sessionRequest.setEndReminder(LocalDateTime.of(2024, 1, 15, 16, 0));
        sessionRequest.setNote("專注於核心功能開發");

        // 建立測試用的 Session
        session = new Session("專案開發時間");
        session.setId(1L);
        session.setTask(task);
        session.setStartTime(LocalDateTime.of(2024, 1, 15, 14, 0));
        session.setEndReminder(LocalDateTime.of(2024, 1, 15, 16, 0));
        session.setNote("專注於核心功能開發");
    }

    @Test
    @DisplayName("SessionRequest 轉換為 Session 實體成功")
    void toEntity_ValidSessionRequest_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        Session result = sessionMapper.toEntity(sessionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getTask()).isEqualTo(task);
        assertThat(result.getEndReminder()).isEqualTo(LocalDateTime.of(2024, 1, 15, 16, 0));
        assertThat(result.getNote()).isEqualTo("專注於核心功能開發");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("SessionRequest 轉換為 Session 實體 - 無任務關聯")
    void toEntity_NoTaskAssociation_Success() {
        // Given
        SessionRequest requestWithoutTask = new SessionRequest();
        requestWithoutTask.setTitle("簡單工作階段");
        requestWithoutTask.setNote("無任務關聯");

        // When
        Session result = sessionMapper.toEntity(requestWithoutTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("簡單工作階段");
        assertThat(result.getTask()).isNull();
        assertThat(result.getNote()).isEqualTo("無任務關聯");
        assertThat(result.getEndReminder()).isNull();

        verify(taskRepository, never()).findById(any());
    }

    @Test
    @DisplayName("SessionRequest 轉換為 Session 實體 - 任務不存在")
    void toEntity_TaskNotFound_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Session result = sessionMapper.toEntity(sessionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getTask()).isNull(); // 任務不存在時設為 null
        assertThat(result.getEndReminder()).isEqualTo(LocalDateTime.of(2024, 1, 15, 16, 0));
        assertThat(result.getNote()).isEqualTo("專注於核心功能開發");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("SessionRequest 為 null 時返回 null")
    void toEntity_NullRequest_ReturnsNull() {
        // When
        Session result = sessionMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
        verify(taskRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Session 實體轉換為 SessionResponse 成功")
    void toResponse_ValidSession_Success() {
        // When
        SessionResponse result = sessionMapper.toResponse(session);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 0));
        assertThat(result.getEndReminder()).isEqualTo(LocalDateTime.of(2024, 1, 15, 16, 0));
        assertThat(result.getNote()).isEqualTo("專注於核心功能開發");
    }

    @Test
    @DisplayName("Session 實體轉換為 SessionResponse - 無任務關聯")
    void toResponse_SessionWithoutTask_Success() {
        // Given
        Session sessionWithoutTask = new Session("簡單工作階段");
        sessionWithoutTask.setId(2L);
        sessionWithoutTask.setNote("無任務關聯");
        // startTime 會在建構時自動設定，這裡手動設定以便測試
        sessionWithoutTask.setStartTime(LocalDateTime.of(2024, 1, 15, 15, 0));

        // When
        SessionResponse result = sessionMapper.toResponse(sessionWithoutTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("簡單工作階段");
        assertThat(result.getTaskId()).isNull();
        assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 15, 0));
        assertThat(result.getNote()).isEqualTo("無任務關聯");
        assertThat(result.getEndReminder()).isNull();
    }

    @Test
    @DisplayName("Session 為 null 時返回 null")
    void toResponse_NullSession_ReturnsNull() {
        // When
        SessionResponse result = sessionMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Session 列表轉換為 SessionResponse 列表成功")
    void toResponseList_ValidSessionList_Success() {
        // Given
        Session session2 = new Session("另一個工作階段");
        session2.setId(2L);

        List<Session> sessions = List.of(session, session2);

        // When
        List<SessionResponse> result = sessionMapper.toResponseList(sessions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("專案開發時間");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("另一個工作階段");
    }

    @Test
    @DisplayName("空 Session 列表轉換為空 SessionResponse 列表")
    void toResponseList_EmptySessionList_ReturnsEmptyList() {
        // Given
        List<Session> emptySessions = List.of();

        // When
        List<SessionResponse> result = sessionMapper.toResponseList(emptySessions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
} 