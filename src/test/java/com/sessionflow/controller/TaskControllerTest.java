package com.sessionflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.exception.TaskNotFoundException;
import com.sessionflow.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController 整合測試")
class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private TaskService taskService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/tasks - 建立任務成功，回傳 201")
    void createTask_Success_Returns201() throws Exception {
        // Given
        TaskRequest request = new TaskRequest("完成專案文件");
        TaskResponse response = new TaskResponse(1L, "完成專案文件", "PENDING");
        
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("完成專案文件"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        verify(taskService).createTask(any(TaskRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/tasks - 缺少必要欄位，回傳 400")
    void createTask_MissingRequiredFields_Returns400() throws Exception {
        // Given
        TaskRequest request = new TaskRequest(); // 空的請求
        
        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        
        verify(taskService, never()).createTask(any());
    }
    
    @Test
    @DisplayName("GET /api/tasks - 查詢所有任務成功，回傳 200")
    void getAllTasks_Success_Returns200() throws Exception {
        // Given
        List<TaskResponse> responses = List.of(
                new TaskResponse(1L, "任務1", "PENDING"),
                new TaskResponse(2L, "任務2", "COMPLETE")
        );
        
        when(taskService.getAllTasks(null)).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("任務1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("任務2"))
                .andExpect(jsonPath("$[1].status").value("COMPLETE"));
        
        verify(taskService).getAllTasks(null);
    }
    
    @Test
    @DisplayName("GET /api/tasks?status=PENDING - 依狀態篩選任務成功，回傳 200")
    void getAllTasks_WithStatusFilter_Returns200() throws Exception {
        // Given
        List<TaskResponse> responses = List.of(
                new TaskResponse(1L, "待辦任務", "PENDING")
        );
        
        when(taskService.getAllTasks("PENDING")).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/tasks")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
        
        verify(taskService).getAllTasks("PENDING");
    }
    
    @Test
    @DisplayName("GET /api/tasks?status=invalid - 無效狀態參數，回傳 400")
    void getAllTasks_InvalidStatus_Returns400() throws Exception {
        // Given
        when(taskService.getAllTasks("invalid"))
                .thenThrow(new IllegalArgumentException("Invalid task status: invalid"));
        
        // When & Then
        mockMvc.perform(get("/api/tasks")
                .param("status", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
        
        verify(taskService).getAllTasks("invalid");
    }
    
    @Test
    @DisplayName("PUT /api/tasks/{id} - 更新任務成功，回傳 200")
    void updateTask_Success_Returns200() throws Exception {
        // Given
        Long taskId = 1L;
        TaskRequest request = new TaskRequest("更新後的任務");
        TaskResponse response = new TaskResponse(taskId, "更新後的任務", "PENDING");
        
        when(taskService.updateTask(eq(taskId), any(TaskRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("更新後的任務"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        verify(taskService).updateTask(eq(taskId), any(TaskRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/tasks/{id} - 任務不存在，回傳 404")
    void updateTask_TaskNotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        TaskRequest request = new TaskRequest("更新後的任務");
        
        when(taskService.updateTask(eq(nonExistentId), any(TaskRequest.class)))
                .thenThrow(new TaskNotFoundException(nonExistentId));
        
        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
        
        verify(taskService).updateTask(eq(nonExistentId), any(TaskRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/tasks/{id} - 缺少必要欄位，回傳 400")
    void updateTask_MissingRequiredFields_Returns400() throws Exception {
        // Given
        Long taskId = 1L;
        TaskRequest request = new TaskRequest(); // 空的請求
        
        // When & Then
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        
        verify(taskService, never()).updateTask(any(), any());
    }
    
    @Test
    @DisplayName("PATCH /api/tasks/{id}/complete - 標記任務完成成功，回傳 200")
    void completeTask_Success_Returns200() throws Exception {
        // Given
        Long taskId = 1L;
        TaskResponse response = new TaskResponse(taskId, "完成的任務", "COMPLETE");
        response.setCompletedAt(LocalDateTime.now());
        
        when(taskService.completeTask(taskId)).thenReturn(response);
        
        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/complete", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value("COMPLETE"))
                .andExpect(jsonPath("$.completedAt").exists());
        
        verify(taskService).completeTask(taskId);
    }
    
    @Test
    @DisplayName("PATCH /api/tasks/{id}/complete - 任務不存在，回傳 404")
    void completeTask_TaskNotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        
        when(taskService.completeTask(nonExistentId))
                .thenThrow(new TaskNotFoundException(nonExistentId));
        
        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/complete", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
        
        verify(taskService).completeTask(nonExistentId);
    }
    
    @Test
    @DisplayName("PATCH /api/tasks/{id}/reopen - 標記任務待辦成功，回傳 200")
    void reopenTask_Success_Returns200() throws Exception {
        // Given
        Long taskId = 1L;
        TaskResponse response = new TaskResponse(taskId, "待辦的任務", "PENDING");
        
        when(taskService.reopenTask(taskId)).thenReturn(response);
        
        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/reopen", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        verify(taskService).reopenTask(taskId);
    }
    
    @Test
    @DisplayName("PATCH /api/tasks/{id}/reopen - 任務不存在，回傳 404")
    void reopenTask_TaskNotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        
        when(taskService.reopenTask(nonExistentId))
                .thenThrow(new TaskNotFoundException(nonExistentId));
        
        // When & Then
        mockMvc.perform(patch("/api/tasks/{id}/reopen", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
        
        verify(taskService).reopenTask(nonExistentId);
    }
    @Test
    @DisplayName("DELETE /api/tasks/{id} - 刪除任務成功，回傳 204")
    void deleteTask_Success_Returns204() throws Exception {
        // Given
        Long taskId = 1L;
        
        doNothing().when(taskService).deleteTask(taskId);
        
        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
        
        verify(taskService).deleteTask(taskId);
    }
    
    @Test
    @DisplayName("DELETE /api/tasks/{id} - 任務不存在，回傳 404")
    void deleteTask_TaskNotFound_Returns404() throws Exception {
        // Given
        Long nonExistentId = 999L;
        
        doThrow(new TaskNotFoundException(nonExistentId))
                .when(taskService).deleteTask(nonExistentId);
        
        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
        
        verify(taskService).deleteTask(nonExistentId);
    }
    
    @Test
    @DisplayName("POST /api/tasks - 無效的 JSON 格式，回傳 400")
    void createTask_InvalidJson_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("JSON_PARSE_ERROR"));
        
        verify(taskService, never()).createTask(any());
    }
    
    @Test
    @DisplayName("PUT /api/tasks/{id} - 無效的路徑參數，回傳 400")
    void updateTask_InvalidPathVariable_Returns400() throws Exception {
        // Given
        TaskRequest request = new TaskRequest("測試任務");
        
        // When & Then
        mockMvc.perform(put("/api/tasks/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH_ERROR"));
        
        verify(taskService, never()).updateTask(any(), any());
    }
    
    @Test
    @DisplayName("POST /api/tasks - 建立任務時包含標籤關聯，回傳 201")
    void createTask_WithTags_Returns201() throws Exception {
        // Given
        TaskRequest request = new TaskRequest("有標籤的任務");
        request.setTagIds(List.of(1L, 2L));
        request.setDueTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        request.setNote("重要任務備註");
        
        TaskResponse response = new TaskResponse(1L, "有標籤的任務", "PENDING");
        response.setDueTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        response.setNote("重要任務備註");
        
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("有標籤的任務"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.dueTime").value("2024-01-15T18:00:00"))
                .andExpect(jsonPath("$.note").value("重要任務備註"));
        
        verify(taskService).createTask(any(TaskRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/tasks - 不支援的 Content-Type，回傳 415")
    void createTask_UnsupportedMediaType_Returns415() throws Exception {
        // Given
        TaskRequest request = new TaskRequest("測試任務");
        
        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
        
        verify(taskService, never()).createTask(any());
    }
    
    @Test
    @DisplayName("GET /api/tasks?status= - 空狀態參數，回傳 200")
    void getAllTasks_EmptyStatusParameter_Returns200() throws Exception {
        // Given
        List<TaskResponse> responses = List.of(
                new TaskResponse(1L, "任務1", "PENDING")
        );
        
        when(taskService.getAllTasks("")).thenReturn(responses);
        
        // When & Then
        mockMvc.perform(get("/api/tasks")
                .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        
        verify(taskService).getAllTasks("");
    }
} 