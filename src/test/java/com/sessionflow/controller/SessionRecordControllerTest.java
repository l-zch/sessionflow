package com.sessionflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRecordUpdateRequest;
import com.sessionflow.exception.SessionRecordNotFoundException;
import com.sessionflow.service.SessionRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionRecordController.class)
@DisplayName("SessionRecordController 整合測試")
class SessionRecordControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private SessionRecordService sessionRecordService;
    
    private SessionRecordResponse sessionRecordResponse1;
    private SessionRecordResponse sessionRecordResponse2;
    private SessionRecordUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        sessionRecordResponse1 = new SessionRecordResponse();
        sessionRecordResponse1.setId(1L);
        sessionRecordResponse1.setTitle("專案開發時間");
        sessionRecordResponse1.setTaskId(1L);
        sessionRecordResponse1.setStartAt(LocalDateTime.of(2024, 1, 15, 14, 0));
        sessionRecordResponse1.setEndAt(LocalDateTime.of(2024, 1, 15, 16, 0));
        sessionRecordResponse1.setPlannedNote("專注於核心功能開發");
        sessionRecordResponse1.setCompletionNote("完成了主要功能的 80%");
        
        sessionRecordResponse2 = new SessionRecordResponse();
        sessionRecordResponse2.setId(2L);
        sessionRecordResponse2.setTitle("測試時間");
        sessionRecordResponse2.setTaskId(1L);
        sessionRecordResponse2.setStartAt(LocalDateTime.of(2024, 1, 16, 10, 0));
        sessionRecordResponse2.setEndAt(LocalDateTime.of(2024, 1, 16, 12, 0));
        sessionRecordResponse2.setPlannedNote("進行單元測試");
        sessionRecordResponse2.setCompletionNote("完成所有測試案例");
        
        updateRequest = new SessionRecordUpdateRequest();
        updateRequest.setPlannedNote("更新的計畫備註");
        updateRequest.setCompletionNote("更新的完成備註");
    }
    
    @Test
    @DisplayName("GET /api/session-records - 查詢所有工作階段紀錄，回傳 200")
    void getSessionRecords_NoParams_Returns200() throws Exception {
        // Given
        List<SessionRecordResponse> responses = List.of(sessionRecordResponse1, sessionRecordResponse2);
        
        when(sessionRecordService.getSessionRecords(null, null, null)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("專案開發時間"))
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[0].plannedNote").value("專注於核心功能開發"))
                .andExpect(jsonPath("$[0].completionNote").value("完成了主要功能的 80%"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("測試時間"));
        
        verify(sessionRecordService).getSessionRecords(null, null, null);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 使用 startDate 查詢，回傳 200")
    void getSessionRecords_WithStartDate_Returns200() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        List<SessionRecordResponse> responses = List.of(sessionRecordResponse1);
        
        when(sessionRecordService.getSessionRecords(startDate, null, null)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("startDate", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("專案開發時間"));
        
        verify(sessionRecordService).getSessionRecords(startDate, null, null);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 使用 endDate 查詢，回傳 200")
    void getSessionRecords_WithEndDate_Returns200() throws Exception {
        // Given
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        List<SessionRecordResponse> responses = List.of(sessionRecordResponse1, sessionRecordResponse2);
        
        when(sessionRecordService.getSessionRecords(null, endDate, null)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("endDate", "2024-01-16"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(sessionRecordService).getSessionRecords(null, endDate, null);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 使用 taskId 查詢，回傳 200")
    void getSessionRecords_WithTaskId_Returns200() throws Exception {
        // Given
        Long taskId = 1L;
        List<SessionRecordResponse> responses = List.of(sessionRecordResponse1, sessionRecordResponse2);
        
        when(sessionRecordService.getSessionRecords(null, null, taskId)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[1].taskId").value(1));
        
        verify(sessionRecordService).getSessionRecords(null, null, taskId);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 使用所有查詢參數，回傳 200")
    void getSessionRecords_WithAllParams_Returns200() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        Long taskId = 1L;
        List<SessionRecordResponse> responses = List.of(sessionRecordResponse1);
        
        when(sessionRecordService.getSessionRecords(startDate, endDate, taskId)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-16")
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].taskId").value(1));
        
        verify(sessionRecordService).getSessionRecords(startDate, endDate, taskId);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 查詢結果為空，回傳 200")
    void getSessionRecords_EmptyResult_Returns200() throws Exception {
        // Given
        List<SessionRecordResponse> emptyResponses = List.of();
        
        when(sessionRecordService.getSessionRecords(null, null, null)).thenReturn(emptyResponses);
        
        // When & Then
        mockMvc.perform(get("/api/session-records"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(sessionRecordService).getSessionRecords(null, null, null);
    }
    
    @Test
    @DisplayName("GET /api/session-records - 無效的日期格式，回傳 400")
    void getSessionRecords_InvalidDateFormat_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).getSessionRecords(any(), any(), any());
    }
    
    @Test
    @DisplayName("GET /api/session-records - 無效的 taskId 格式，回傳 400")
    void getSessionRecords_InvalidTaskIdFormat_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/session-records")
                        .param("taskId", "invalid-id"))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).getSessionRecords(any(), any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 更新成功，回傳 200")
    void updateSessionRecord_Success_Returns200() throws Exception {
        // Given
        Long recordId = 1L;
        SessionRecordResponse updatedResponse = new SessionRecordResponse();
        updatedResponse.setId(recordId);
        updatedResponse.setTitle("專案開發時間");
        updatedResponse.setPlannedNote("更新的計畫備註");
        updatedResponse.setCompletionNote("更新的完成備註");
        
        when(sessionRecordService.updateSessionRecord(recordId, updateRequest)).thenReturn(updatedResponse);
        
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", recordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(recordId))
                .andExpect(jsonPath("$.title").value("專案開發時間"))
                .andExpect(jsonPath("$.plannedNote").value("更新的計畫備註"))
                .andExpect(jsonPath("$.completionNote").value("更新的完成備註"));
        
        verify(sessionRecordService).updateSessionRecord(eq(recordId), any(SessionRecordUpdateRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 部分更新成功，回傳 200")
    void updateSessionRecord_PartialUpdate_Returns200() throws Exception {
        // Given
        Long recordId = 1L;
        SessionRecordUpdateRequest partialUpdateRequest = new SessionRecordUpdateRequest();
        partialUpdateRequest.setPlannedNote("僅更新計畫備註");
        // completionNote 保持 null
        
        SessionRecordResponse updatedResponse = new SessionRecordResponse();
        updatedResponse.setId(recordId);
        updatedResponse.setPlannedNote("僅更新計畫備註");
        updatedResponse.setCompletionNote("原本的完成備註");
        
        when(sessionRecordService.updateSessionRecord(recordId, partialUpdateRequest)).thenReturn(updatedResponse);
        
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", recordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(recordId))
                .andExpect(jsonPath("$.plannedNote").value("僅更新計畫備註"))
                .andExpect(jsonPath("$.completionNote").value("原本的完成備註"));
        
        verify(sessionRecordService).updateSessionRecord(eq(recordId), any(SessionRecordUpdateRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 工作階段紀錄不存在，回傳 404")
    void updateSessionRecord_NotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        
        when(sessionRecordService.updateSessionRecord(nonExistentId, updateRequest))
                .thenThrow(new SessionRecordNotFoundException(nonExistentId));
        
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SESSION_RECORD_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("工作階段紀錄不存在"));
        
        verify(sessionRecordService).updateSessionRecord(eq(nonExistentId), any(SessionRecordUpdateRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 請求體驗證失敗，回傳 400")
    void updateSessionRecord_ValidationError_Returns400() throws Exception {
        // Given
        Long recordId = 1L;
        SessionRecordUpdateRequest invalidRequest = new SessionRecordUpdateRequest();
        invalidRequest.setPlannedNote("a".repeat(501)); // 超過最大長度
        
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", recordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).updateSessionRecord(any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 無效的 ID 格式，回傳 400")
    void updateSessionRecord_InvalidIdFormat_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).updateSessionRecord(any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/session-records/{id} - 空的請求體，回傳 400")
    void updateSessionRecord_EmptyBody_Returns400() throws Exception {
        // Given
        Long recordId = 1L;
        
        // When & Then
        mockMvc.perform(put("/api/session-records/{id}", recordId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).updateSessionRecord(any(), any());
    }
    
    @Test
    @DisplayName("DELETE /api/session-records/{id} - 刪除成功，回傳 204")
    void deleteSessionRecord_Success_Returns204() throws Exception {
        // Given
        Long recordId = 1L;
        
        doNothing().when(sessionRecordService).deleteSessionRecord(recordId);
        
        // When & Then
        mockMvc.perform(delete("/api/session-records/{id}", recordId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        
        verify(sessionRecordService).deleteSessionRecord(recordId);
    }
    
    @Test
    @DisplayName("DELETE /api/session-records/{id} - 工作階段紀錄不存在，回傳 404")
    void deleteSessionRecord_NotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        
        doThrow(new SessionRecordNotFoundException(nonExistentId))
                .when(sessionRecordService).deleteSessionRecord(nonExistentId);
        
        // When & Then
        mockMvc.perform(delete("/api/session-records/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SESSION_RECORD_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("工作階段紀錄不存在"));
        
        verify(sessionRecordService).deleteSessionRecord(nonExistentId);
    }
    
    @Test
    @DisplayName("DELETE /api/session-records/{id} - 無效的 ID 格式，回傳 400")
    void deleteSessionRecord_InvalidIdFormat_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/session-records/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());
        
        verify(sessionRecordService, never()).deleteSessionRecord(any());
    }
} 