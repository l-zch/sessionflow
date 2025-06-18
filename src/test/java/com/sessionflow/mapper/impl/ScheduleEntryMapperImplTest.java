package com.sessionflow.mapper.impl;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.model.ScheduleEntry;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleEntryMapper 單元測試")
class ScheduleEntryMapperImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ScheduleEntryMapperImpl scheduleEntryMapper;

    private ScheduleEntryRequest scheduleEntryRequest;
    private ScheduleEntry scheduleEntry;
    private Task task;

    @BeforeEach
    void setUp() {
        // 建立測試用的 Task
        task = new Task("完成專案文件");
        task.setId(1L);

        // 建立測試用的 ScheduleEntryRequest
        scheduleEntryRequest = new ScheduleEntryRequest();
        scheduleEntryRequest.setTitle("專案會議");
        scheduleEntryRequest.setStartAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        scheduleEntryRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 11, 0));
        scheduleEntryRequest.setTaskId(1L);
        scheduleEntryRequest.setNote("討論專案進度");

        // 建立測試用的 ScheduleEntry
        scheduleEntry = new ScheduleEntry(
            "專案會議",
            LocalDateTime.of(2024, 1, 15, 10, 0),
            LocalDateTime.of(2024, 1, 15, 11, 0)
        );
        scheduleEntry.setId(1L);
        scheduleEntry.setTask(task);
        scheduleEntry.setNote("討論專案進度");
    }

    @Test
    @DisplayName("ScheduleEntryRequest 轉換為 ScheduleEntry 實體成功")
    void toEntity_ValidScheduleEntryRequest_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        ScheduleEntry result = scheduleEntryMapper.toEntity(scheduleEntryRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("專案會議");
        assertThat(result.getStartAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
        assertThat(result.getEndAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 11, 0));
        assertThat(result.getTask()).isEqualTo(task);
        assertThat(result.getNote()).isEqualTo("討論專案進度");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("ScheduleEntryRequest 轉換為 ScheduleEntry 實體 - 無任務關聯")
    void toEntity_NoTaskAssociation_Success() {
        // Given
        ScheduleEntryRequest requestWithoutTask = new ScheduleEntryRequest();
        requestWithoutTask.setTitle("個人時間");
        requestWithoutTask.setStartAt(LocalDateTime.of(2024, 1, 15, 14, 0));
        requestWithoutTask.setEndAt(LocalDateTime.of(2024, 1, 15, 15, 0));
        requestWithoutTask.setNote("休息時間");

        // When
        ScheduleEntry result = scheduleEntryMapper.toEntity(requestWithoutTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("個人時間");
        assertThat(result.getStartAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 0));
        assertThat(result.getEndAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 15, 0));
        assertThat(result.getTask()).isNull();
        assertThat(result.getNote()).isEqualTo("休息時間");

        verify(taskRepository, never()).findById(any());
    }

    @Test
    @DisplayName("ScheduleEntryRequest 轉換為 ScheduleEntry 實體 - 任務不存在")
    void toEntity_TaskNotFound_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ScheduleEntry result = scheduleEntryMapper.toEntity(scheduleEntryRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("專案會議");
        assertThat(result.getTask()).isNull(); // 任務不存在時設為 null
        assertThat(result.getNote()).isEqualTo("討論專案進度");

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("ScheduleEntryRequest 為 null 時返回 null")
    void toEntity_NullRequest_ReturnsNull() {
        // When
        ScheduleEntry result = scheduleEntryMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
        verify(taskRepository, never()).findById(any());
    }

    @Test
    @DisplayName("ScheduleEntry 實體轉換為 ScheduleEntryResponse 成功")
    void toResponse_ValidScheduleEntry_Success() {
        // When
        ScheduleEntryResponse result = scheduleEntryMapper.toResponse(scheduleEntry);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("專案會議");
        assertThat(result.getStartAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
        assertThat(result.getEndAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 11, 0));
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getNote()).isEqualTo("討論專案進度");
    }

    @Test
    @DisplayName("ScheduleEntry 實體轉換為 ScheduleEntryResponse - 無任務關聯")
    void toResponse_ScheduleEntryWithoutTask_Success() {
        // Given
        ScheduleEntry entryWithoutTask = new ScheduleEntry(
            "個人時間",
            LocalDateTime.of(2024, 1, 15, 14, 0),
            LocalDateTime.of(2024, 1, 15, 15, 0)
        );
        entryWithoutTask.setId(2L);
        entryWithoutTask.setNote("休息時間");

        // When
        ScheduleEntryResponse result = scheduleEntryMapper.toResponse(entryWithoutTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("個人時間");
        assertThat(result.getTaskId()).isNull();
        assertThat(result.getNote()).isEqualTo("休息時間");
    }

    @Test
    @DisplayName("ScheduleEntry 為 null 時返回 null")
    void toResponse_NullScheduleEntry_ReturnsNull() {
        // When
        ScheduleEntryResponse result = scheduleEntryMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ScheduleEntry 列表轉換為 ScheduleEntryResponse 列表成功")
    void toResponseList_ValidScheduleEntryList_Success() {
        // Given
        ScheduleEntry entry2 = new ScheduleEntry(
            "另一個會議",
            LocalDateTime.of(2024, 1, 15, 16, 0),
            LocalDateTime.of(2024, 1, 15, 17, 0)
        );
        entry2.setId(2L);

        List<ScheduleEntry> entries = List.of(scheduleEntry, entry2);

        // When
        List<ScheduleEntryResponse> result = scheduleEntryMapper.toResponseList(entries);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("專案會議");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("另一個會議");
    }

    @Test
    @DisplayName("空 ScheduleEntry 列表轉換為空 ScheduleEntryResponse 列表")
    void toResponseList_EmptyScheduleEntryList_ReturnsEmptyList() {
        // Given
        List<ScheduleEntry> emptyEntries = List.of();

        // When
        List<ScheduleEntryResponse> result = scheduleEntryMapper.toResponseList(emptyEntries);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ScheduleEntry 列表為 null 時返回 null")
    void toResponseList_NullScheduleEntryList_ReturnsNull() {
        // When
        List<ScheduleEntryResponse> result = scheduleEntryMapper.toResponseList(null);

        // Then
        assertThat(result).isNull();
    }

} 