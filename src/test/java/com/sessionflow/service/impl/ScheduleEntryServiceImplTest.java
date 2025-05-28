package com.sessionflow.service.impl;

import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.exception.InvalidTimeRangeException;
import com.sessionflow.exception.ScheduleEntryNotFoundException;
import com.sessionflow.mapper.ScheduleEntryMapper;
import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.model.Task;
import com.sessionflow.repository.ScheduleEntryRepository;
import com.sessionflow.service.impl.ScheduleEntryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleEntryService 單元測試")
class ScheduleEntryServiceImplTest {

    @Mock
    private ScheduleEntryRepository scheduleEntryRepository;

    @Mock
    private ScheduleEntryMapper scheduleEntryMapper;

    @InjectMocks
    private ScheduleEntryServiceImpl scheduleEntryService;

    private ScheduleEntryRequest validRequest;
    private ScheduleEntry scheduleEntry;
    private ScheduleEntryResponse scheduleEntryResponse;
    private Task task;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);

        task = new Task();
        task.setId(1L);
        task.setTitle("測試任務");

        validRequest = new ScheduleEntryRequest();
        validRequest.setTitle("團隊會議");
        validRequest.setTaskId(1L);
        validRequest.setStartAt(startAt);
        validRequest.setEndAt(endAt);
        validRequest.setNote("討論專案進度");

        scheduleEntry = new ScheduleEntry();
        scheduleEntry.setId(1L);
        scheduleEntry.setTitle("團隊會議");
        scheduleEntry.setTask(task);
        scheduleEntry.setStartAt(startAt);
        scheduleEntry.setEndAt(endAt);
        scheduleEntry.setNote("討論專案進度");

        scheduleEntryResponse = new ScheduleEntryResponse();
        scheduleEntryResponse.setId(1L);
        scheduleEntryResponse.setTitle("團隊會議");
        scheduleEntryResponse.setTaskId(1L);
        scheduleEntryResponse.setStartAt(startAt);
        scheduleEntryResponse.setEndAt(endAt);
        scheduleEntryResponse.setNote("討論專案進度");
    }

    @Test
    @DisplayName("成功建立排程 - 有關聯任務")
    void createScheduleEntry_WithTask_ShouldReturnResponse() {
        // Given
        when(scheduleEntryMapper.toEntity(validRequest)).thenReturn(scheduleEntry);
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When
        ScheduleEntryResponse result = scheduleEntryService.createScheduleEntry(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("團隊會議");
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getStartAt()).isEqualTo(validRequest.getStartAt());
        assertThat(result.getEndAt()).isEqualTo(validRequest.getEndAt());

        verify(scheduleEntryMapper).toEntity(validRequest);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("成功建立排程 - 無關聯任務")
    void createScheduleEntry_WithoutTask_ShouldReturnResponse() {
        // Given
        validRequest.setTaskId(null);
        scheduleEntry.setTask(null);
        scheduleEntryResponse.setTaskId(null);

        when(scheduleEntryMapper.toEntity(validRequest)).thenReturn(scheduleEntry);
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When
        ScheduleEntryResponse result = scheduleEntryService.createScheduleEntry(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTaskId()).isNull();
        assertThat(result.getTitle()).isEqualTo("團隊會議");

        verify(scheduleEntryMapper).toEntity(validRequest);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("建立排程成功 - startAt 為 null")
    void createScheduleEntry_NullStartAt_ShouldNotThrowException() {
        // Given
        validRequest.setStartAt(null);
        validRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 11, 0));

        when(scheduleEntryMapper.toEntity(validRequest)).thenReturn(scheduleEntry);
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When & Then - 應該不會拋出異常
        assertThatCode(() -> scheduleEntryService.createScheduleEntry(validRequest))
                .doesNotThrowAnyException();

        verify(scheduleEntryMapper).toEntity(validRequest);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("建立排程成功 - endAt 為 null")
    void createScheduleEntry_NullEndAt_ShouldNotThrowException() {
        // Given
        validRequest.setStartAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        validRequest.setEndAt(null);

        when(scheduleEntryMapper.toEntity(validRequest)).thenReturn(scheduleEntry);
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When & Then - 應該不會拋出異常
        assertThatCode(() -> scheduleEntryService.createScheduleEntry(validRequest))
                .doesNotThrowAnyException();

        verify(scheduleEntryMapper).toEntity(validRequest);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("建立排程成功 - startAt 和 endAt 都為 null")
    void createScheduleEntry_BothTimesNull_ShouldNotThrowException() {
        // Given
        validRequest.setStartAt(null);
        validRequest.setEndAt(null);

        when(scheduleEntryMapper.toEntity(validRequest)).thenReturn(scheduleEntry);
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When & Then - 應該不會拋出異常
        assertThatCode(() -> scheduleEntryService.createScheduleEntry(validRequest))
                .doesNotThrowAnyException();

        verify(scheduleEntryMapper).toEntity(validRequest);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("建立排程失敗 - 結束時間早於開始時間")
    void createScheduleEntry_EndTimeBeforeStartTime_ShouldThrowException() {
        // Given
        validRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 9, 0)); // 早於開始時間

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.createScheduleEntry(validRequest))
                .isInstanceOf(InvalidTimeRangeException.class)
                .hasMessage("結束時間必須晚於開始時間");

        verify(scheduleEntryMapper, never()).toEntity(any());
        verify(scheduleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("建立排程失敗 - 結束時間等於開始時間")
    void createScheduleEntry_EndTimeEqualsStartTime_ShouldThrowException() {
        // Given
        LocalDateTime sameTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        validRequest.setStartAt(sameTime);
        validRequest.setEndAt(sameTime);

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.createScheduleEntry(validRequest))
                .isInstanceOf(InvalidTimeRangeException.class)
                .hasMessage("結束時間必須晚於開始時間");

        verify(scheduleEntryMapper, never()).toEntity(any());
        verify(scheduleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("成功更新排程")
    void updateScheduleEntry_ValidRequest_ShouldReturnUpdatedResponse() {
        // Given
        Long scheduleId = 1L;
        ScheduleEntryRequest updateRequest = new ScheduleEntryRequest();
        updateRequest.setTitle("更新後的會議");
        updateRequest.setTaskId(1L);
        updateRequest.setStartAt(LocalDateTime.of(2024, 1, 15, 14, 0));
        updateRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 15, 0));
        updateRequest.setNote("更新後的備註");

        ScheduleEntry updatedEntry = new ScheduleEntry();
        updatedEntry.setId(scheduleId);
        updatedEntry.setTitle("更新後的會議");

        ScheduleEntryResponse updatedResponse = new ScheduleEntryResponse();
        updatedResponse.setId(scheduleId);
        updatedResponse.setTitle("更新後的會議");

        when(scheduleEntryRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleEntry));
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(updatedEntry);
        when(scheduleEntryMapper.toResponse(updatedEntry)).thenReturn(updatedResponse);

        // When
        ScheduleEntryResponse result = scheduleEntryService.updateScheduleEntry(scheduleId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(scheduleId);
        assertThat(result.getTitle()).isEqualTo("更新後的會議");

        verify(scheduleEntryRepository).findById(scheduleId);
        verify(scheduleEntryMapper).updateEntityFromRequest(updateRequest, scheduleEntry);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(updatedEntry);
    }

    @Test
    @DisplayName("更新排程成功 - startAt 為 null")
    void updateScheduleEntry_NullStartAt_ShouldNotThrowException() {
        // Given
        Long scheduleId = 1L;
        validRequest.setStartAt(null);
        validRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 11, 0));

        when(scheduleEntryRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleEntry));
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When & Then - 應該不會拋出異常
        assertThatCode(() -> scheduleEntryService.updateScheduleEntry(scheduleId, validRequest))
                .doesNotThrowAnyException();

        verify(scheduleEntryRepository).findById(scheduleId);
        verify(scheduleEntryMapper).updateEntityFromRequest(validRequest, scheduleEntry);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("更新排程成功 - endAt 為 null")
    void updateScheduleEntry_NullEndAt_ShouldNotThrowException() {
        // Given
        Long scheduleId = 1L;
        validRequest.setStartAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        validRequest.setEndAt(null);

        when(scheduleEntryRepository.findById(scheduleId)).thenReturn(Optional.of(scheduleEntry));
        when(scheduleEntryRepository.save(scheduleEntry)).thenReturn(scheduleEntry);
        when(scheduleEntryMapper.toResponse(scheduleEntry)).thenReturn(scheduleEntryResponse);

        // When & Then - 應該不會拋出異常
        assertThatCode(() -> scheduleEntryService.updateScheduleEntry(scheduleId, validRequest))
                .doesNotThrowAnyException();

        verify(scheduleEntryRepository).findById(scheduleId);
        verify(scheduleEntryMapper).updateEntityFromRequest(validRequest, scheduleEntry);
        verify(scheduleEntryRepository).save(scheduleEntry);
        verify(scheduleEntryMapper).toResponse(scheduleEntry);
    }

    @Test
    @DisplayName("更新排程失敗 - 排程不存在")
    void updateScheduleEntry_NotFound_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(scheduleEntryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.updateScheduleEntry(nonExistentId, validRequest))
                .isInstanceOf(ScheduleEntryNotFoundException.class)
                .hasMessage("ScheduleEntry with id " + nonExistentId + " not found");

        verify(scheduleEntryRepository).findById(nonExistentId);
        verify(scheduleEntryMapper, never()).updateEntityFromRequest(any(), any());
        verify(scheduleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新排程失敗 - 時間區間不合法")
    void updateScheduleEntry_InvalidTimeRange_ShouldThrowException() {
        // Given
        Long scheduleId = 1L;
        validRequest.setEndAt(LocalDateTime.of(2024, 1, 15, 9, 0)); // 早於開始時間

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.updateScheduleEntry(scheduleId, validRequest))
                .isInstanceOf(InvalidTimeRangeException.class)
                .hasMessage("結束時間必須晚於開始時間");

        // 時間驗證在 repository 調用之前就會失敗，所以不會調用 repository
        verify(scheduleEntryRepository, never()).findById(any());
        verify(scheduleEntryMapper, never()).updateEntityFromRequest(any(), any());
        verify(scheduleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新排程失敗 - 結束時間等於開始時間")
    void updateScheduleEntry_EndTimeEqualsStartTime_ShouldThrowException() {
        // Given
        Long scheduleId = 1L;
        LocalDateTime sameTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        validRequest.setStartAt(sameTime);
        validRequest.setEndAt(sameTime);

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.updateScheduleEntry(scheduleId, validRequest))
                .isInstanceOf(InvalidTimeRangeException.class)
                .hasMessage("結束時間必須晚於開始時間");

        // 時間驗證在 repository 調用之前就會失敗，所以不會調用 repository
        verify(scheduleEntryRepository, never()).findById(any());
        verify(scheduleEntryMapper, never()).updateEntityFromRequest(any(), any());
        verify(scheduleEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("成功刪除排程")
    void deleteScheduleEntry_ExistingId_ShouldDeleteSuccessfully() {
        // Given
        Long scheduleId = 1L;
        when(scheduleEntryRepository.existsById(scheduleId)).thenReturn(true);

        // When
        scheduleEntryService.deleteScheduleEntry(scheduleId);

        // Then
        verify(scheduleEntryRepository).existsById(scheduleId);
        verify(scheduleEntryRepository).deleteById(scheduleId);
    }

    @Test
    @DisplayName("刪除排程失敗 - 排程不存在")
    void deleteScheduleEntry_NotFound_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(scheduleEntryRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> scheduleEntryService.deleteScheduleEntry(nonExistentId))
                .isInstanceOf(ScheduleEntryNotFoundException.class)
                .hasMessage("ScheduleEntry with id " + nonExistentId + " not found");

        verify(scheduleEntryRepository).existsById(nonExistentId);
        verify(scheduleEntryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("成功查詢指定時間區間內的排程")
    void getScheduleEntries_ValidDateRange_ShouldReturnScheduleList() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        ScheduleEntry entry1 = new ScheduleEntry();
        entry1.setId(1L);
        entry1.setTitle("會議1");

        ScheduleEntry entry2 = new ScheduleEntry();
        entry2.setId(2L);
        entry2.setTitle("會議2");

        List<ScheduleEntry> scheduleEntries = Arrays.asList(entry1, entry2);

        ScheduleEntryResponse response1 = new ScheduleEntryResponse();
        response1.setId(1L);
        response1.setTitle("會議1");

        ScheduleEntryResponse response2 = new ScheduleEntryResponse();
        response2.setId(2L);
        response2.setTitle("會議2");

        List<ScheduleEntryResponse> expectedResponses = Arrays.asList(response1, response2);

        when(scheduleEntryRepository.findByDateRange(startDateTime, endDateTime))
                .thenReturn(scheduleEntries);
        when(scheduleEntryMapper.toResponseList(scheduleEntries)).thenReturn(expectedResponses);

        // When
        List<ScheduleEntryResponse> result = scheduleEntryService.getScheduleEntries(startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("會議1");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("會議2");

        verify(scheduleEntryRepository).findByDateRange(startDateTime, endDateTime);
        verify(scheduleEntryMapper).toResponseList(scheduleEntries);
    }

    @Test
    @DisplayName("查詢排程 - 空結果")
    void getScheduleEntries_NoResults_ShouldReturnEmptyList() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        LocalDate endDate = LocalDate.of(2024, 1, 16);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        when(scheduleEntryRepository.findByDateRange(startDateTime, endDateTime))
                .thenReturn(Arrays.asList());
        when(scheduleEntryMapper.toResponseList(Arrays.asList())).thenReturn(Arrays.asList());

        // When
        List<ScheduleEntryResponse> result = scheduleEntryService.getScheduleEntries(startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(scheduleEntryRepository).findByDateRange(startDateTime, endDateTime);
        verify(scheduleEntryMapper).toResponseList(Arrays.asList());
    }

    @Test
    @DisplayName("查詢排程 - 單日查詢")
    void getScheduleEntries_SameStartAndEndDate_ShouldQueryCorrectRange() {
        // Given
        LocalDate sameDate = LocalDate.of(2024, 1, 15);
        LocalDateTime startDateTime = sameDate.atStartOfDay(); // 2024-01-15 00:00:00
        LocalDateTime endDateTime = sameDate.plusDays(1).atStartOfDay(); // 2024-01-16 00:00:00

        when(scheduleEntryRepository.findByDateRange(startDateTime, endDateTime))
                .thenReturn(Arrays.asList());
        when(scheduleEntryMapper.toResponseList(Arrays.asList())).thenReturn(Arrays.asList());

        // When
        scheduleEntryService.getScheduleEntries(sameDate, sameDate);

        // Then
        verify(scheduleEntryRepository).findByDateRange(startDateTime, endDateTime);
    }
}