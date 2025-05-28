package com.sessionflow.service.impl;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.exception.TaskNotFoundException;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import com.sessionflow.repository.TaskRepository;
import com.sessionflow.service.impl.TaskServiceImpl;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.repository.SessionRecordRepository;
import com.sessionflow.repository.ScheduleEntryRepository;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService 單元測試")
class TaskServiceImplTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @Mock
    private SessionRepository sessionRepository;
    
    @Mock
    private SessionRecordRepository sessionRecordRepository;
    
    @Mock
    private ScheduleEntryRepository scheduleEntryRepository;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    private TaskRequest taskRequest;
    private Task task;
    private TaskResponse taskResponse;
    
    @BeforeEach
    void setUp() {
        taskRequest = new TaskRequest("完成專案文件");
        task = new Task("完成專案文件");
        task.setId(1L);
        taskResponse = new TaskResponse(1L, "完成專案文件", "PENDING");
    }
    
    @Test
    @DisplayName("成功建立任務")
    void createTask_Success() {
        // Given
        when(taskMapper.toEntity(taskRequest)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        
        // When
        TaskResponse result = taskService.createTask(taskRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("完成專案文件");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        
        verify(taskMapper).toEntity(taskRequest);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(task);
    }
    
    @Test
    @DisplayName("查詢所有任務成功 - 無狀態篩選")
    void getAllTasks_NoStatusFilter_Success() {
        // Given
        List<Task> tasks = List.of(task);
        List<TaskResponse> expectedResponses = List.of(taskResponse);
        
        when(taskRepository.findAllOrderByCreatedAtDesc()).thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(expectedResponses);
        
        // When
        List<TaskResponse> result = taskService.getAllTasks(null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("完成專案文件");
        
        verify(taskRepository).findAllOrderByCreatedAtDesc();
        verify(taskMapper).toResponseList(tasks);
    }
    
    @Test
    @DisplayName("查詢所有任務成功 - 依狀態篩選")
    void getAllTasks_WithStatusFilter_Success() {
        // Given
        List<Task> tasks = List.of(task);
        List<TaskResponse> expectedResponses = List.of(taskResponse);
        
        when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PENDING)).thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(expectedResponses);
        
        // When
        List<TaskResponse> result = taskService.getAllTasks("PENDING");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(taskRepository).findByStatusOrderByCreatedAtDesc(TaskStatus.PENDING);
        verify(taskMapper).toResponseList(tasks);
    }
    
    @Test
    @DisplayName("查詢任務時使用無效狀態，應丟出 IllegalArgumentException")
    void getAllTasks_InvalidStatus_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> taskService.getAllTasks("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid task status: invalid");
        
        verify(taskRepository, never()).findByStatusOrderByCreatedAtDesc(any());
    }
    
    @Test
    @DisplayName("更新任務成功")
    void updateTask_Success() {
        // Given
        Long taskId = 1L;
        TaskRequest updateRequest = new TaskRequest("更新後的任務");
        Task updatedTask = new Task("更新後的任務");
        updatedTask.setId(taskId);
        TaskResponse updatedResponse = new TaskResponse(taskId, "更新後的任務", "PENDING");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(updatedResponse);
        
        // When
        TaskResponse result = taskService.updateTask(taskId, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getTitle()).isEqualTo("更新後的任務");
        
        verify(taskRepository).findById(taskId);
        verify(taskMapper).updateEntityFromRequest(task, updateRequest);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(updatedTask);
    }
    
    @Test
    @DisplayName("更新不存在的任務，應丟出 TaskNotFoundException")
    void updateTask_TaskNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        TaskRequest updateRequest = new TaskRequest("更新後的任務");
        
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(nonExistentId, updateRequest))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id 999 not found");
        
        verify(taskRepository).findById(nonExistentId);
        verify(taskMapper, never()).updateEntityFromRequest(any(), any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("刪除任務成功")
    void deleteTask_Success() {
        // Given
        Long taskId = 1L;
        
        when(taskRepository.existsById(taskId)).thenReturn(true);
        doNothing().when(sessionRecordRepository).deleteByTaskId(taskId);
        doNothing().when(sessionRepository).deleteByTaskId(taskId);
        doNothing().when(scheduleEntryRepository).deleteByTaskId(taskId);
        doNothing().when(taskRepository).deleteById(taskId);
        
        // When
        assertThatCode(() -> taskService.deleteTask(taskId))
                .doesNotThrowAnyException();
        
        // Then
        verify(taskRepository).existsById(taskId);
        verify(sessionRecordRepository).deleteByTaskId(taskId);
        verify(sessionRepository).deleteByTaskId(taskId);
        verify(scheduleEntryRepository).deleteByTaskId(taskId);
        verify(taskRepository).deleteById(taskId);
    }
    
    @Test
    @DisplayName("刪除不存在的任務，應丟出 TaskNotFoundException")
    void deleteTask_TaskNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        
        when(taskRepository.existsById(nonExistentId)).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(nonExistentId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id 999 not found");
        
        verify(taskRepository).existsById(nonExistentId);
        verify(taskRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("標記任務為完成成功")
    void completeTask_Success() {
        // Given
        Long taskId = 1L;
        Task completedTask = new Task("完成專案文件");
        completedTask.setId(taskId);
        completedTask.complete();
        TaskResponse completedResponse = new TaskResponse(taskId, "完成專案文件", "COMPLETE");
        completedResponse.setCompletedAt(LocalDateTime.now());
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(completedTask);
        when(taskMapper.toResponse(completedTask)).thenReturn(completedResponse);
        
        // When
        TaskResponse result = taskService.completeTask(taskId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getStatus()).isEqualTo("COMPLETE");
        assertThat(result.getCompletedAt()).isNotNull();
        
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        verify(taskMapper).toResponse(completedTask);
    }
    
    @Test
    @DisplayName("標記不存在的任務為完成，應丟出 TaskNotFoundException")
    void completeTask_TaskNotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;
        
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(nonExistentId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id 999 not found");
        
        verify(taskRepository).findById(nonExistentId);
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("建立任務時正確關聯標籤")
    void createTask_WithTags_Success() {
        // Given
        TaskRequest requestWithTags = new TaskRequest("有標籤的任務");
        requestWithTags.setTagIds(List.of(1L, 2L));
        
        Task taskWithTags = new Task("有標籤的任務");
        taskWithTags.setId(1L);
        
        TaskResponse responseWithTags = new TaskResponse(1L, "有標籤的任務", "PENDING");
        
        when(taskMapper.toEntity(requestWithTags)).thenReturn(taskWithTags);
        when(taskRepository.save(taskWithTags)).thenReturn(taskWithTags);
        when(taskMapper.toResponse(taskWithTags)).thenReturn(responseWithTags);
        
        // When
        TaskResponse result = taskService.createTask(requestWithTags);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("有標籤的任務");
        
        verify(taskMapper).toEntity(requestWithTags);
        verify(taskRepository).save(taskWithTags);
        verify(taskMapper).toResponse(taskWithTags);
    }
    
    @Test
    @DisplayName("查詢任務時使用空字串狀態，應視為無篩選")
    void getAllTasks_EmptyStringStatus_TreatedAsNoFilter() {
        // Given
        List<Task> tasks = List.of(task);
        List<TaskResponse> expectedResponses = List.of(taskResponse);
        
        when(taskRepository.findAllOrderByCreatedAtDesc()).thenReturn(tasks);
        when(taskMapper.toResponseList(tasks)).thenReturn(expectedResponses);
        
        // When
        List<TaskResponse> result = taskService.getAllTasks("   "); // 空白字串
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        verify(taskRepository).findAllOrderByCreatedAtDesc();
        verify(taskRepository, never()).findByStatusOrderByCreatedAtDesc(any());
        verify(taskMapper).toResponseList(tasks);
    }
} 