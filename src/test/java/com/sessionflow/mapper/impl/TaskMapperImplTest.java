package com.sessionflow.mapper.impl;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.mapper.TagMapper;
import com.sessionflow.model.Tag;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import com.sessionflow.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskMapper 單元測試")
class TaskMapperImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TaskMapperImpl taskMapper;

    private TaskRequest taskRequest;
    private Task task;
    private Tag tag1;
    private Tag tag2;
    private TagResponse tagResponse1;
    private TagResponse tagResponse2;

    @BeforeEach
    void setUp() {
        // 建立測試用的 Tag
        tag1 = new Tag("工作", "#FF0000");
        tag1.setId(1L);

        tag2 = new Tag("學習", "#00FF00");
        tag2.setId(2L);

        // 建立測試用的 TagResponse
        tagResponse1 = new TagResponse(1L, "工作", "#FF0000");
        tagResponse2 = new TagResponse(2L, "學習", "#00FF00");

        // 建立測試用的 TaskRequest
        taskRequest = new TaskRequest();
        taskRequest.setTitle("完成專案文件");
        taskRequest.setTagIds(List.of(1L, 2L));
        taskRequest.setDueTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        taskRequest.setNote("需要包含技術規格和使用者手冊");

        // 建立測試用的 Task
        task = new Task("完成專案文件");
        task.setId(1L);
        task.setDueTime(LocalDateTime.of(2024, 1, 15, 18, 0));
        task.setNote("需要包含技術規格和使用者手冊");
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 0));
        task.setUpdatedAt(LocalDateTime.of(2024, 1, 10, 10, 0));
        task.setTags(Set.of(tag1, tag2));
    }

    @Test
    @DisplayName("TaskRequest 轉換為 Task 實體成功")
    void toEntity_ValidTaskRequest_Success() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag2));

        // When
        Task result = taskMapper.toEntity(taskRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("完成專案文件");
        assertThat(result.getDueTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));
        assertThat(result.getNote()).isEqualTo("需要包含技術規格和使用者手冊");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING); // 預設狀態
        assertThat(result.getTags()).hasSize(2);
        assertThat(result.getTags()).containsExactlyInAnyOrder(tag1, tag2);

        verify(tagRepository).findById(1L);
        verify(tagRepository).findById(2L);
    }

    @Test
    @DisplayName("TaskRequest 轉換為 Task 實體 - 無標籤")
    void toEntity_NoTags_Success() {
        // Given
        TaskRequest requestWithoutTags = new TaskRequest();
        requestWithoutTags.setTitle("簡單任務");
        requestWithoutTags.setNote("無標籤的任務");

        // When
        Task result = taskMapper.toEntity(requestWithoutTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("簡單任務");
        assertThat(result.getNote()).isEqualTo("無標籤的任務");
        assertThat(result.getDueTime()).isNull();
        assertThat(result.getTags()).isEmpty();

        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("TaskRequest 轉換為 Task 實體 - 空標籤列表")
    void toEntity_EmptyTagList_Success() {
        // Given
        TaskRequest requestWithEmptyTags = new TaskRequest();
        requestWithEmptyTags.setTitle("簡單任務");
        requestWithEmptyTags.setTagIds(List.of());

        // When
        Task result = taskMapper.toEntity(requestWithEmptyTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("簡單任務");
        assertThat(result.getTags()).isEmpty();

        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("TaskRequest 轉換為 Task 實體 - 部分標籤不存在")
    void toEntity_SomeTagsNotFound_Success() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.findById(2L)).thenReturn(Optional.empty()); // 標籤不存在

        // When
        Task result = taskMapper.toEntity(taskRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTags()).hasSize(1);
        assertThat(result.getTags()).contains(tag1);

        verify(tagRepository).findById(1L);
        verify(tagRepository).findById(2L);
    }

    @Test
    @DisplayName("TaskRequest 為 null 時返回 null")
    void toEntity_NullRequest_ReturnsNull() {
        // When
        Task result = taskMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Task 實體轉換為 TaskResponse 成功")
    void toResponse_ValidTask_Success() {
        // Given
        when(tagMapper.toResponse(tag1)).thenReturn(tagResponse1);
        when(tagMapper.toResponse(tag2)).thenReturn(tagResponse2);

        // When
        TaskResponse result = taskMapper.toResponse(task);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("完成專案文件");
        assertThat(result.getDueTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 0));
        assertThat(result.getNote()).isEqualTo("需要包含技術規格和使用者手冊");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTags()).hasSize(2);
        assertThat(result.getTags()).containsExactlyInAnyOrder(tagResponse1, tagResponse2);

        verify(tagMapper).toResponse(tag1);
        verify(tagMapper).toResponse(tag2);
    }

    @Test
    @DisplayName("Task 實體轉換為 TaskResponse - 無標籤")
    void toResponse_TaskWithoutTags_Success() {
        // Given
        Task taskWithoutTags = new Task("簡單任務");
        taskWithoutTags.setId(2L);
        taskWithoutTags.setStatus(TaskStatus.COMPLETE);
        taskWithoutTags.setCompletedAt(LocalDateTime.of(2024, 1, 16, 12, 0));

        // When
        TaskResponse result = taskMapper.toResponse(taskWithoutTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("簡單任務");
        assertThat(result.getStatus()).isEqualTo("COMPLETE");
        assertThat(result.getCompletedAt()).isEqualTo(LocalDateTime.of(2024, 1, 16, 12, 0));
        assertThat(result.getTags()).isNull();

        verify(tagMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Task 實體轉換為 TaskResponse - 空標籤集合")
    void toResponse_TaskWithEmptyTags_Success() {
        // Given
        Task taskWithEmptyTags = new Task("簡單任務");
        taskWithEmptyTags.setId(3L);
        taskWithEmptyTags.setTags(new HashSet<>());

        // When
        TaskResponse result = taskMapper.toResponse(taskWithEmptyTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTags()).isNull();

        verify(tagMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Task 為 null 時返回 null")
    void toResponse_NullTask_ReturnsNull() {
        // When
        TaskResponse result = taskMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
        verify(tagMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Task 列表轉換為 TaskResponse 列表成功")
    void toResponseList_ValidTaskList_Success() {
        // Given
        Task task2 = new Task("另一個任務");
        task2.setId(2L);
        task2.setStatus(TaskStatus.COMPLETE);

        List<Task> tasks = List.of(task, task2);

        when(tagMapper.toResponse(tag1)).thenReturn(tagResponse1);
        when(tagMapper.toResponse(tag2)).thenReturn(tagResponse2);

        // When
        List<TaskResponse> result = taskMapper.toResponseList(tasks);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("完成專案文件");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("另一個任務");
    }

    @Test
    @DisplayName("空 Task 列表轉換為空 TaskResponse 列表")
    void toResponseList_EmptyTaskList_ReturnsEmptyList() {
        // Given
        List<Task> emptyTasks = List.of();

        // When
        List<TaskResponse> result = taskMapper.toResponseList(emptyTasks);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Task 列表為 null 時返回 null")
    void toResponseList_NullTaskList_ReturnsNull() {
        // When
        List<TaskResponse> result = taskMapper.toResponseList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("使用 TaskRequest 更新 Task 實體成功")
    void updateEntityFromRequest_ValidInputs_Success() {
        // Given
        Task existingTask = new Task("舊標題");
        existingTask.setId(1L);
        existingTask.setNote("舊備註");
        existingTask.setTags(Set.of(tag1)); // 原本只有一個標籤

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("新標題");
        updateRequest.setDueTime(LocalDateTime.of(2024, 2, 1, 12, 0));
        updateRequest.setNote("新備註");
        updateRequest.setTagIds(List.of(2L)); // 更新為不同的標籤

        when(tagRepository.findById(2L)).thenReturn(Optional.of(tag2));

        // When
        taskMapper.updateEntityFromRequest(existingTask, updateRequest);

        // Then
        assertThat(existingTask.getTitle()).isEqualTo("新標題");
        assertThat(existingTask.getDueTime()).isEqualTo(LocalDateTime.of(2024, 2, 1, 12, 0));
        assertThat(existingTask.getNote()).isEqualTo("新備註");
        assertThat(existingTask.getTags()).hasSize(1);
        assertThat(existingTask.getTags()).contains(tag2);

        verify(tagRepository).findById(2L);
    }

    @Test
    @DisplayName("使用 TaskRequest 更新 Task 實體 - 清空標籤")
    void updateEntityFromRequest_ClearTags_Success() {
        // Given
        Task existingTask = new Task("任務標題");
        existingTask.setTags(new HashSet<>(Set.of(tag1, tag2))); // 原本有標籤

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("更新標題");
        updateRequest.setTagIds(null); // 設為 null 表示清空標籤

        // When
        taskMapper.updateEntityFromRequest(existingTask, updateRequest);

        // Then
        assertThat(existingTask.getTitle()).isEqualTo("更新標題");
        assertThat(existingTask.getTags()).isEmpty();

        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("使用 TaskRequest 更新 Task 實體 - 空標籤列表")
    void updateEntityFromRequest_EmptyTagList_Success() {
        // Given
        Task existingTask = new Task("任務標題");
        existingTask.setTags(new HashSet<>(Set.of(tag1, tag2))); // 原本有標籤

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("更新標題");
        updateRequest.setTagIds(List.of()); // 空列表

        // When
        taskMapper.updateEntityFromRequest(existingTask, updateRequest);

        // Then
        assertThat(existingTask.getTitle()).isEqualTo("更新標題");
        assertThat(existingTask.getTags()).isEmpty();

        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Task 為 null 時不執行任何操作")
    void updateEntityFromRequest_NullTask_NoOperation() {
        // When
        taskMapper.updateEntityFromRequest(null, taskRequest);

        // Then
        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("TaskRequest 為 null 時不執行任何操作")
    void updateEntityFromRequest_NullRequest_NoOperation() {
        // Given
        Task existingTask = new Task("原標題");
        String originalTitle = existingTask.getTitle();

        // When
        taskMapper.updateEntityFromRequest(existingTask, null);

        // Then
        assertThat(existingTask.getTitle()).isEqualTo(originalTitle);
        verify(tagRepository, never()).findById(any());
    }

    @Test
    @DisplayName("更新時部分標籤不存在")
    void updateEntityFromRequest_SomeTagsNotFound_Success() {
        // Given
        Task existingTask = new Task("任務標題");
        existingTask.setTags(new HashSet<>());

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("更新標題");
        updateRequest.setTagIds(List.of(1L, 999L)); // 999L 不存在

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag1));
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        taskMapper.updateEntityFromRequest(existingTask, updateRequest);

        // Then
        assertThat(existingTask.getTags()).hasSize(1);
        assertThat(existingTask.getTags()).contains(tag1);

        verify(tagRepository).findById(1L);
        verify(tagRepository).findById(999L);
    }
} 