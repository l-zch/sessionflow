package com.sessionflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.SessionRecordCreateRequest;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.exception.SessionNotFoundException;
import com.sessionflow.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@DisplayName("SessionController 整合測試")
class SessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SessionService sessionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/sessions - 建立工作階段成功，回傳 201")
    void createSession_Success_Returns201() throws Exception {
        // Given
        SessionRequest request = new SessionRequest("專案開發時間");
        request.setNote("專注於核心功能開發");
        request.setEndReminder(LocalDateTime.of(2024, 1, 15, 16, 0));
        
        SessionResponse response = new SessionResponse();
        response.setId(1L);
        response.setTitle("專案開發時間");
        response.setNote("專注於核心功能開發");
        response.setEndReminder(LocalDateTime.of(2024, 1, 15, 16, 0));
        
        when(sessionService.createSession(any(SessionRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("專案開發時間"))
                .andExpect(jsonPath("$.note").value("專注於核心功能開發"))
                .andExpect(jsonPath("$.endReminder").value("2024-01-15T16:00:00"));
        
        verify(sessionService).createSession(any(SessionRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/sessions - 缺少必要欄位，回傳 400")
    void createSession_MissingRequiredFields_Returns400() throws Exception {
        // Given
        SessionRequest request = new SessionRequest(); // 空的請求
        
        // When & Then
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        
        verify(sessionService, never()).createSession(any());
    }
    
    @Test
    @DisplayName("GET /api/sessions - 查詢所有工作階段成功，回傳 200")
    void getAllSessions_Success_Returns200() throws Exception {
        // Given
        List<SessionResponse> responses = List.of(
                createSessionResponse(1L, "專案開發時間"),
                createSessionResponse(2L, "會議時間")
        );
        
        when(sessionService.getAllSessions()).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("專案開發時間"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("會議時間"));
        
        verify(sessionService).getAllSessions();
    }
    
    @Test
    @DisplayName("POST /api/sessions/{id}/end - 結束工作階段成功，回傳 201")
    void endSession_Success_Returns201() throws Exception {
        // Given
        Long sessionId = 1L;
        SessionRecordCreateRequest request = new SessionRecordCreateRequest();
        request.setSessionId(sessionId);
        request.setCompletionNote("完成了主要功能的 80%");
        
        SessionRecordResponse response = new SessionRecordResponse();
        response.setId(1L);
        response.setTitle("專案開發時間");
        response.setStartAt(LocalDateTime.of(2024, 1, 15, 14, 0));
        response.setEndAt(LocalDateTime.of(2024, 1, 15, 16, 0));
        response.setPlannedNote("專注於核心功能開發");
        response.setCompletionNote("完成了主要功能的 80%");
        
        when(sessionService.endSession(eq(sessionId), any(SessionRecordCreateRequest.class)))
                .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/sessions/{id}/end", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("專案開發時間"))
                .andExpect(jsonPath("$.startAt").value("2024-01-15T14:00:00"))
                .andExpect(jsonPath("$.endAt").value("2024-01-15T16:00:00"))
                .andExpect(jsonPath("$.plannedNote").value("專注於核心功能開發"))
                .andExpect(jsonPath("$.completionNote").value("完成了主要功能的 80%"));
        
        verify(sessionService).endSession(eq(sessionId), any(SessionRecordCreateRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/sessions/{id}/end - 工作階段不存在，回傳 404")
    void endSession_SessionNotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        SessionRecordCreateRequest request = new SessionRecordCreateRequest();
        request.setSessionId(nonExistentId);
        request.setCompletionNote("完成備註");
        
        when(sessionService.endSession(eq(nonExistentId), any(SessionRecordCreateRequest.class)))
                .thenThrow(new SessionNotFoundException(nonExistentId));
        
        // When & Then
        mockMvc.perform(post("/api/sessions/{id}/end", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
        
        verify(sessionService).endSession(eq(nonExistentId), any(SessionRecordCreateRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/sessions/{id}/end - 無效的路徑參數，回傳 400")
    void endSession_InvalidPathVariable_Returns400() throws Exception {
        // Given
        SessionRecordCreateRequest request = new SessionRecordCreateRequest();
        request.setCompletionNote("完成備註");
        
        // When & Then
        mockMvc.perform(post("/api/sessions/invalid/end")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH_ERROR"));
        
        verify(sessionService, never()).endSession(any(), any());
    }
    
    @Test
    @DisplayName("POST /api/sessions - 建立工作階段時包含任務關聯，回傳 201")
    void createSession_WithTask_Returns201() throws Exception {
        // Given
        SessionRequest request = new SessionRequest("有任務的工作階段");
        request.setTaskId(1L);
        request.setNote("專注於任務完成");
        
        SessionResponse response = new SessionResponse();
        response.setId(1L);
        response.setTitle("有任務的工作階段");
        response.setTaskId(1L);
        response.setNote("專注於任務完成");
        
        when(sessionService.createSession(any(SessionRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("有任務的工作階段"))
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.note").value("專注於任務完成"));
        
        verify(sessionService).createSession(any(SessionRequest.class));
    }
    
    @Test
    @DisplayName("GET /api/sessions - 查詢空的工作階段列表，回傳 200")
    void getAllSessions_EmptyList_Returns200() throws Exception {
        // Given
        List<SessionResponse> emptyResponses = List.of();
        
        when(sessionService.getAllSessions()).thenReturn(emptyResponses);
        
        // When & Then
        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(sessionService).getAllSessions();
    }
    
    private SessionResponse createSessionResponse(Long id, String title) {
        SessionResponse response = new SessionResponse();
        response.setId(id);
        response.setTitle(title);
        return response;
    }
} 