package com.sessionflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.exception.InvalidTimeRangeException;
import com.sessionflow.exception.ScheduleEntryNotFoundException;
import com.sessionflow.service.ScheduleEntryService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleEntryController.class)
@DisplayName("ScheduleEntryController 整合測試")
class ScheduleEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleEntryService scheduleEntryService;

    @Autowired
    private ObjectMapper objectMapper;

    private ScheduleEntryRequest validRequest;
    private ScheduleEntryResponse scheduleEntryResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);

        validRequest = new ScheduleEntryRequest();
        validRequest.setTitle("團隊會議");
        validRequest.setTaskId(1L);
        validRequest.setStartAt(startAt);
        validRequest.setEndAt(endAt);
        validRequest.setNote("討論專案進度");

        scheduleEntryResponse = new ScheduleEntryResponse();
        scheduleEntryResponse.setId(1L);
        scheduleEntryResponse.setTitle("團隊會議");
        scheduleEntryResponse.setTaskId(1L);
        scheduleEntryResponse.setStartAt(startAt);
        scheduleEntryResponse.setEndAt(endAt);
        scheduleEntryResponse.setNote("討論專案進度");
    }

    @Test
    @DisplayName("POST /api/schedule-entries - 成功建立排程")
    void createScheduleEntry_ValidRequest_ShouldReturn201() throws Exception {
        // Given
        when(scheduleEntryService.createScheduleEntry(any(ScheduleEntryRequest.class)))
                .thenReturn(scheduleEntryResponse);

        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("團隊會議"))
                .andExpect(jsonPath("$.taskId").value(1L))
                .andExpect(jsonPath("$.startAt").value("2024-01-15T10:00:00"))
                .andExpect(jsonPath("$.endAt").value("2024-01-15T11:00:00"))
                .andExpect(jsonPath("$.note").value("討論專案進度"));

        verify(scheduleEntryService).createScheduleEntry(any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("POST /api/schedule-entries - 建立排程成功（無任務關聯）")
    void createScheduleEntry_WithoutTask_ShouldReturn201() throws Exception {
        // Given
        validRequest.setTaskId(null);
        scheduleEntryResponse.setTaskId(null);

        when(scheduleEntryService.createScheduleEntry(any(ScheduleEntryRequest.class)))
                .thenReturn(scheduleEntryResponse);

        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").isEmpty());

        verify(scheduleEntryService).createScheduleEntry(any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("POST /api/schedule-entries - 時間區間錯誤應回傳422")
    void createScheduleEntry_InvalidTimeRange_ShouldReturn422() throws Exception {
        // Given
        when(scheduleEntryService.createScheduleEntry(any(ScheduleEntryRequest.class)))
                .thenThrow(new InvalidTimeRangeException("結束時間必須晚於開始時間"));

        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_TIME_RANGE"))
                .andExpect(jsonPath("$.message").value("時間區間錯誤"))
                .andExpect(jsonPath("$.details").value("結束時間必須晚於開始時間"));

        verify(scheduleEntryService).createScheduleEntry(any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("POST /api/schedule-entries - 請求參數驗證失敗應回傳400")
    void createScheduleEntry_ValidationError_ShouldReturn400() throws Exception {
        // Given - 空標題
        validRequest.setTitle("");

        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(scheduleEntryService, never()).createScheduleEntry(any());
    }

    @Test
    @DisplayName("POST /api/schedule-entries - 缺少必要欄位應回傳400")
    void createScheduleEntry_MissingRequiredFields_ShouldReturn400() throws Exception {
        // Given - 缺少開始時間
        validRequest.setStartAt(null);

        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(scheduleEntryService, never()).createScheduleEntry(any());
    }

    @Test
    @DisplayName("PUT /api/schedule-entries/{id} - 成功更新排程")
    void updateScheduleEntry_ValidRequest_ShouldReturn200() throws Exception {
        // Given
        Long scheduleId = 1L;
        ScheduleEntryRequest updateRequest = new ScheduleEntryRequest();
        updateRequest.setTitle("更新後的會議");
        updateRequest.setTaskId(2L);
        updateRequest.setStartAt(LocalDateTime.of(2024, 1, 15, 14, 0));
        updateRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 15, 0));
        updateRequest.setNote("更新後的備註");

        ScheduleEntryResponse updatedResponse = new ScheduleEntryResponse();
        updatedResponse.setId(scheduleId);
        updatedResponse.setTitle("更新後的會議");
        updatedResponse.setTaskId(2L);
        updatedResponse.setStartAt(updateRequest.getStartAt());
        updatedResponse.setEndAt(updateRequest.getEndAt());
        updatedResponse.setNote("更新後的備註");

        when(scheduleEntryService.updateScheduleEntry(eq(scheduleId), any(ScheduleEntryRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/schedule-entries/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduleId))
                .andExpect(jsonPath("$.title").value("更新後的會議"))
                .andExpect(jsonPath("$.taskId").value(2L))
                .andExpect(jsonPath("$.note").value("更新後的備註"));

        verify(scheduleEntryService).updateScheduleEntry(eq(scheduleId), any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("PUT /api/schedule-entries/{id} - 排程不存在應回傳404")
    void updateScheduleEntry_NotFound_ShouldReturn404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(scheduleEntryService.updateScheduleEntry(eq(nonExistentId), any(ScheduleEntryRequest.class)))
                .thenThrow(new ScheduleEntryNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(put("/api/schedule-entries/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_ENTRY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("排程不存在"));

        verify(scheduleEntryService).updateScheduleEntry(eq(nonExistentId), any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("PUT /api/schedule-entries/{id} - 時間區間錯誤應回傳422")
    void updateScheduleEntry_InvalidTimeRange_ShouldReturn422() throws Exception {
        // Given
        Long scheduleId = 1L;
        when(scheduleEntryService.updateScheduleEntry(eq(scheduleId), any(ScheduleEntryRequest.class)))
                .thenThrow(new InvalidTimeRangeException("結束時間必須晚於開始時間"));

        // When & Then
        mockMvc.perform(put("/api/schedule-entries/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_TIME_RANGE"));

        verify(scheduleEntryService).updateScheduleEntry(eq(scheduleId), any(ScheduleEntryRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/schedule-entries/{id} - 成功刪除排程")
    void deleteScheduleEntry_ExistingId_ShouldReturn204() throws Exception {
        // Given
        Long scheduleId = 1L;
        doNothing().when(scheduleEntryService).deleteScheduleEntry(scheduleId);

        // When & Then
        mockMvc.perform(delete("/api/schedule-entries/{id}", scheduleId))
                .andExpect(status().isNoContent());

        verify(scheduleEntryService).deleteScheduleEntry(scheduleId);
    }

    @Test
    @DisplayName("DELETE /api/schedule-entries/{id} - 排程不存在應回傳404")
    void deleteScheduleEntry_NotFound_ShouldReturn404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        doThrow(new ScheduleEntryNotFoundException(nonExistentId))
                .when(scheduleEntryService).deleteScheduleEntry(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/schedule-entries/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_ENTRY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("排程不存在"));

        verify(scheduleEntryService).deleteScheduleEntry(nonExistentId);
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 成功查詢排程")
    void getScheduleEntries_ValidDateRange_ShouldReturn200() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);

        ScheduleEntryResponse response1 = new ScheduleEntryResponse();
        response1.setId(1L);
        response1.setTitle("會議1");
        response1.setStartAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        response1.setEndAt(LocalDateTime.of(2024, 1, 15, 11, 0));

        ScheduleEntryResponse response2 = new ScheduleEntryResponse();
        response2.setId(2L);
        response2.setTitle("會議2");
        response2.setStartAt(LocalDateTime.of(2024, 1, 16, 14, 0));
        response2.setEndAt(LocalDateTime.of(2024, 1, 16, 15, 0));

        List<ScheduleEntryResponse> responses = Arrays.asList(response1, response2);

        when(scheduleEntryService.getScheduleEntries(startDate, endDate)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-16"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("會議1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("會議2"));

        verify(scheduleEntryService).getScheduleEntries(startDate, endDate);
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 查詢結果為空")
    void getScheduleEntries_NoResults_ShouldReturnEmptyList() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);

        when(scheduleEntryService.getScheduleEntries(startDate, endDate)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-16"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(scheduleEntryService).getScheduleEntries(startDate, endDate);
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 缺少 startDate 參數應回傳400")
    void getScheduleEntries_MissingStartDate_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("endDate", "2024-01-16"))
                .andExpect(status().isBadRequest());

        verify(scheduleEntryService, never()).getScheduleEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 缺少 endDate 參數應回傳400")
    void getScheduleEntries_MissingEndDate_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("startDate", "2024-01-15"))
                .andExpect(status().isBadRequest());

        verify(scheduleEntryService, never()).getScheduleEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 缺少所有參數應回傳400")
    void getScheduleEntries_MissingAllParams_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/schedule-entries"))
                .andExpect(status().isBadRequest());

        verify(scheduleEntryService, never()).getScheduleEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 日期格式錯誤應回傳400")
    void getScheduleEntries_InvalidDateFormat_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2024-01-16"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH_ERROR"));

        verify(scheduleEntryService, never()).getScheduleEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/schedule-entries - 單日查詢")
    void getScheduleEntries_SameDateRange_ShouldReturn200() throws Exception {
        // Given
        LocalDate sameDate = LocalDate.of(2024, 1, 15);
        when(scheduleEntryService.getScheduleEntries(sameDate, sameDate)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/schedule-entries")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(scheduleEntryService).getScheduleEntries(sameDate, sameDate);
    }

    @Test
    @DisplayName("POST /api/schedule-entries - JSON 格式錯誤應回傳400")
    void createScheduleEntry_InvalidJson_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/schedule-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JSON_PARSE_ERROR"));

        verify(scheduleEntryService, never()).createScheduleEntry(any());
    }

    @Test
    @DisplayName("PUT /api/schedule-entries/{id} - 路徑參數類型錯誤應回傳400")
    void updateScheduleEntry_InvalidPathVariable_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/schedule-entries/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH_ERROR"));

        verify(scheduleEntryService, never()).updateScheduleEntry(any(), any());
    }
} 