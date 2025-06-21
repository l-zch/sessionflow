package com.sessionflow.service.impl;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRecordUpdateRequest;
import com.sessionflow.exception.SessionRecordNotFoundException;
import com.sessionflow.mapper.SessionRecordMapper;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.model.Task;
import com.sessionflow.repository.SessionRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionRecordService 單元測試")
class SessionRecordServiceImplTest {

    @Mock
    private SessionRecordRepository sessionRecordRepository;

    @Mock
    private SessionRecordMapper sessionRecordMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SessionRecordServiceImpl sessionRecordService;

    private SessionRecord sessionRecord1;
    private SessionRecord sessionRecord2;
    private SessionRecordResponse sessionRecordResponse1;
    private SessionRecordResponse sessionRecordResponse2;
    private SessionRecordUpdateRequest updateRequest;
    private Task task;

    @BeforeEach
    void setUp() {
        // 建立測試用的 Task
        task = new Task("測試任務");
        task.setId(1L);
        task.setNote("任務描述");

        // 建立測試用的 SessionRecord
        sessionRecord1 = new SessionRecord("專案開發時間",
                LocalDateTime.of(2024, 1, 15, 14, 0),
                LocalDateTime.of(2024, 1, 15, 16, 0));
        sessionRecord1.setId(1L);
        sessionRecord1.setTask(task);
        sessionRecord1.setPlannedNote("專注於核心功能開發");
        sessionRecord1.setCompletionNote("完成了主要功能的 80%");

        sessionRecord2 = new SessionRecord("測試時間",
                LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 16, 12, 0));
        sessionRecord2.setId(2L);
        sessionRecord2.setTask(task);
        sessionRecord2.setPlannedNote("進行單元測試");
        sessionRecord2.setCompletionNote("完成所有測試案例");

        // 建立測試用的 Response
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
    @DisplayName("查詢所有工作階段紀錄成功")
    void getSessionRecords_NoFilters_Success() {
        // Given
        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findAllByOrderByIdDesc()).thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("專案開發時間");
        assertThat(result.get(1).getTitle()).isEqualTo("測試時間");

        verify(sessionRecordRepository).findAllByOrderByIdDesc();
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("僅使用 startDate 查詢工作階段紀錄")
    void getSessionRecords_OnlyStartDate_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDate.of(2024, 12, 31).plusDays(1).atStartOfDay(); // 預設到年底

        List<SessionRecord> records = List.of(sessionRecord1);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1);

        when(sessionRecordRepository.findByStartAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(startDate, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(sessionRecordRepository).findByStartAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("僅使用 endDate 查詢工作階段紀錄")
    void getSessionRecords_OnlyEndDate_Success() {
        // Given
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        LocalDateTime startDateTime = LocalDate.of(2024, 1, 1).atStartOfDay(); // 預設從年初
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findByStartAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(null, endDate, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(sessionRecordRepository).findByStartAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("根據日期區間查詢工作階段紀錄成功")
    void getSessionRecords_WithDateRange_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findByStartAtBetween(startDateTime, endDateTime)).thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(startDate, endDate, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(sessionRecordRepository).findByStartAtBetween(startDateTime, endDateTime);
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("根據任務ID查詢工作階段紀錄成功")
    void getSessionRecords_WithTaskId_Success() {
        // Given
        Long taskId = 1L;
        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findByTaskId(taskId)).thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(null, null, taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTaskId()).isEqualTo(taskId);
        assertThat(result.get(1).getTaskId()).isEqualTo(taskId);

        verify(sessionRecordRepository).findByTaskId(taskId);
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("根據日期區間和任務ID查詢工作階段紀錄成功")
    void getSessionRecords_WithDateRangeAndTaskId_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        Long taskId = 1L;
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<SessionRecord> records = List.of(sessionRecord1);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1);

        when(sessionRecordRepository.findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId))
                .thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(startDate, endDate, taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskId()).isEqualTo(taskId);

        verify(sessionRecordRepository).findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId);
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("根據開始日期和任務ID查詢工作階段紀錄成功")
    void getSessionRecords_WithStartDateAndTaskId_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        Long taskId = 1L;
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDate.of(2099, 12, 31).atStartOfDay(); // 預設到遠未來

        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId))
                .thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(startDate, null, taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTaskId()).isEqualTo(taskId);
        assertThat(result.get(1).getTaskId()).isEqualTo(taskId);

        verify(sessionRecordRepository).findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId);
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("根據結束日期和任務ID查詢工作階段紀錄成功")
    void getSessionRecords_WithEndDateAndTaskId_Success() {
        // Given
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        Long taskId = 1L;
        LocalDateTime startDateTime = LocalDate.of(2000, 1, 1).atStartOfDay(); // 預設從遠過去
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<SessionRecord> records = List.of(sessionRecord1, sessionRecord2);
        List<SessionRecordResponse> expectedResponses = List.of(sessionRecordResponse1, sessionRecordResponse2);

        when(sessionRecordRepository.findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId))
                .thenReturn(records);
        when(sessionRecordMapper.toResponseList(records)).thenReturn(expectedResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(null, endDate, taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTaskId()).isEqualTo(taskId);
        assertThat(result.get(1).getTaskId()).isEqualTo(taskId);

        verify(sessionRecordRepository).findByStartAtBetweenAndTaskId(startDateTime, endDateTime, taskId);
        verify(sessionRecordMapper).toResponseList(records);
    }

    @Test
    @DisplayName("查詢結果為空列表")
    void getSessionRecords_EmptyResult_Success() {
        // Given
        List<SessionRecord> emptyRecords = List.of();
        List<SessionRecordResponse> emptyResponses = List.of();

        when(sessionRecordRepository.findAllByOrderByIdDesc()).thenReturn(emptyRecords);
        when(sessionRecordMapper.toResponseList(emptyRecords)).thenReturn(emptyResponses);

        // When
        List<SessionRecordResponse> result = sessionRecordService.getSessionRecords(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(sessionRecordRepository).findAllByOrderByIdDesc();
        verify(sessionRecordMapper).toResponseList(emptyRecords);
    }

    @Test
    @DisplayName("更新工作階段紀錄成功 - 更新所有欄位")
    void updateSessionRecord_UpdateAllFields_Success() {
        // Given
        Long recordId = 1L;

        when(sessionRecordRepository.findById(recordId)).thenReturn(Optional.of(sessionRecord1));
        when(sessionRecordRepository.save(sessionRecord1)).thenReturn(sessionRecord1);
        when(sessionRecordMapper.toResponse(sessionRecord1)).thenReturn(sessionRecordResponse1);

        // When
        SessionRecordResponse result = sessionRecordService.updateSessionRecord(recordId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        // 驗證欄位有被更新
        assertThat(sessionRecord1.getPlannedNote()).isEqualTo("更新的計畫備註");
        assertThat(sessionRecord1.getCompletionNote()).isEqualTo("更新的完成備註");

        verify(sessionRecordRepository).findById(recordId);
        verify(sessionRecordRepository).save(sessionRecord1);
        verify(sessionRecordMapper).toResponse(sessionRecord1);
    }

    @Test
    @DisplayName("更新工作階段紀錄成功 - 僅更新計畫備註")
    void updateSessionRecord_UpdatePlannedNoteOnly_Success() {
        // Given
        Long recordId = 1L;
        SessionRecordUpdateRequest partialUpdateRequest = new SessionRecordUpdateRequest();
        partialUpdateRequest.setPlannedNote("僅更新計畫備註");
        // completionNote 保持 null

        String originalCompletionNote = sessionRecord1.getCompletionNote();

        when(sessionRecordRepository.findById(recordId)).thenReturn(Optional.of(sessionRecord1));
        when(sessionRecordRepository.save(sessionRecord1)).thenReturn(sessionRecord1);
        when(sessionRecordMapper.toResponse(sessionRecord1)).thenReturn(sessionRecordResponse1);

        // When
        SessionRecordResponse result = sessionRecordService.updateSessionRecord(recordId, partialUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(sessionRecord1.getPlannedNote()).isEqualTo("僅更新計畫備註");
        assertThat(sessionRecord1.getCompletionNote()).isEqualTo(originalCompletionNote); // 保持原值

        verify(sessionRecordRepository).findById(recordId);
        verify(sessionRecordRepository).save(sessionRecord1);
        verify(sessionRecordMapper).toResponse(sessionRecord1);
    }

    @Test
    @DisplayName("更新工作階段紀錄成功 - 僅更新完成備註")
    void updateSessionRecord_UpdateCompletionNoteOnly_Success() {
        // Given
        Long recordId = 1L;
        SessionRecordUpdateRequest partialUpdateRequest = new SessionRecordUpdateRequest();
        partialUpdateRequest.setCompletionNote("僅更新完成備註");
        // plannedNote 保持 null

        String originalPlannedNote = sessionRecord1.getPlannedNote();

        when(sessionRecordRepository.findById(recordId)).thenReturn(Optional.of(sessionRecord1));
        when(sessionRecordRepository.save(sessionRecord1)).thenReturn(sessionRecord1);
        when(sessionRecordMapper.toResponse(sessionRecord1)).thenReturn(sessionRecordResponse1);

        // When
        SessionRecordResponse result = sessionRecordService.updateSessionRecord(recordId, partialUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(sessionRecord1.getPlannedNote()).isEqualTo(originalPlannedNote); // 保持原值
        assertThat(sessionRecord1.getCompletionNote()).isEqualTo("僅更新完成備註");

        verify(sessionRecordRepository).findById(recordId);
        verify(sessionRecordRepository).save(sessionRecord1);
        verify(sessionRecordMapper).toResponse(sessionRecord1);
    }

    @Test
    @DisplayName("更新工作階段紀錄 - 空的更新請求")
    void updateSessionRecord_EmptyUpdateRequest_Success() {
        // Given
        Long recordId = 1L;
        SessionRecordUpdateRequest emptyUpdateRequest = new SessionRecordUpdateRequest();
        // 所有欄位都是 null

        String originalPlannedNote = sessionRecord1.getPlannedNote();
        String originalCompletionNote = sessionRecord1.getCompletionNote();

        when(sessionRecordRepository.findById(recordId)).thenReturn(Optional.of(sessionRecord1));
        when(sessionRecordRepository.save(sessionRecord1)).thenReturn(sessionRecord1);
        when(sessionRecordMapper.toResponse(sessionRecord1)).thenReturn(sessionRecordResponse1);

        // When
        SessionRecordResponse result = sessionRecordService.updateSessionRecord(recordId, emptyUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(sessionRecord1.getPlannedNote()).isEqualTo(originalPlannedNote); // 保持原值
        assertThat(sessionRecord1.getCompletionNote()).isEqualTo(originalCompletionNote); // 保持原值

        verify(sessionRecordRepository).findById(recordId);
        verify(sessionRecordRepository).save(sessionRecord1);
        verify(sessionRecordMapper).toResponse(sessionRecord1);
    }

    @Test
    @DisplayName("更新不存在的工作階段紀錄，應丟出例外")
    void updateSessionRecord_NotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;

        when(sessionRecordRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessionRecordService.updateSessionRecord(nonExistentId, updateRequest))
                .isInstanceOf(SessionRecordNotFoundException.class)
                .hasMessage("SessionRecord with id 999 not found");

        verify(sessionRecordRepository).findById(nonExistentId);
        verify(sessionRecordRepository, never()).save(any());
        verify(sessionRecordMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("刪除工作階段紀錄成功")
    void deleteSessionRecord_Success() {
        // Given
        Long recordId = 1L;

        when(sessionRecordRepository.existsById(recordId)).thenReturn(true);
        doNothing().when(sessionRecordRepository).deleteById(recordId);

        // When
        sessionRecordService.deleteSessionRecord(recordId);

        // Then
        verify(sessionRecordRepository).existsById(recordId);
        verify(sessionRecordRepository).deleteById(recordId);
    }

    @Test
    @DisplayName("刪除不存在的工作階段紀錄，應丟出例外")
    void deleteSessionRecord_NotFound_ThrowsException() {
        // Given
        Long nonExistentId = 999L;

        when(sessionRecordRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> sessionRecordService.deleteSessionRecord(nonExistentId))
                .isInstanceOf(SessionRecordNotFoundException.class)
                .hasMessage("SessionRecord with id 999 not found");

        verify(sessionRecordRepository).existsById(nonExistentId);
        verify(sessionRecordRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("根據任務ID查詢工作階段紀錄ID列表成功")
    void findIdsByTaskId_Success() {
        // Given
        Long taskId = 1L;
        List<SessionRecord> sessionRecords = List.of(sessionRecord1, sessionRecord2);

        when(sessionRecordRepository.findByTaskId(taskId)).thenReturn(sessionRecords);

        // When
        List<Long> result = sessionRecordService.findIdsByTaskId(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(1L, 2L);

        verify(sessionRecordRepository).findByTaskId(taskId);
    }

    @Test
    @DisplayName("根據任務ID查詢工作階段紀錄ID列表 - 無結果")
    void findIdsByTaskId_EmptyResult() {
        // Given
        Long taskId = 999L;
        List<SessionRecord> emptyRecords = List.of();

        when(sessionRecordRepository.findByTaskId(taskId)).thenReturn(emptyRecords);

        // When
        List<Long> result = sessionRecordService.findIdsByTaskId(taskId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(sessionRecordRepository).findByTaskId(taskId);
    }

    @Test
    @DisplayName("根據任務ID刪除工作階段紀錄成功")
    void deleteByTaskId_Success() {
        // Given
        Long taskId = 1L;

        doNothing().when(sessionRecordRepository).deleteByTaskId(taskId);

        // When
        sessionRecordService.deleteByTaskId(taskId);

        // Then
        verify(sessionRecordRepository).deleteByTaskId(taskId);
    }
}