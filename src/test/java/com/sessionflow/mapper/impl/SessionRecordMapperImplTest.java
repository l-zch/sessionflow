package com.sessionflow.mapper.impl;

import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.model.Session;
import com.sessionflow.model.SessionRecord;
import com.sessionflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionRecordMapper 單元測試")
class SessionRecordMapperImplTest {

    @InjectMocks
    private SessionRecordMapperImpl sessionRecordMapper;

    private Session session;
    private SessionRecord sessionRecord;
    private Task task;

    @BeforeEach
    void setUp() {
        // 建立測試用的 Task
        task = new Task("完成專案文件");
        task.setId(1L);

        // 建立測試用的 Session
        session = new Session("專案開發時間");
        session.setId(1L);
        session.setTask(task);
        session.setNote("專注於核心功能開發");
        session.setStartTime(LocalDateTime.of(2024, 1, 15, 9, 0)); // 明確設定 startTime

        // 建立測試用的 SessionRecord
        sessionRecord = new SessionRecord(
            "專案開發時間",
            LocalDateTime.of(2024, 1, 15, 9, 0),
            LocalDateTime.of(2024, 1, 15, 11, 0)
        );
        sessionRecord.setId(1L);
        sessionRecord.setTask(task);
        sessionRecord.setPlannedNote("專注於核心功能開發");
        sessionRecord.setCompletionNote("成功完成核心功能");
    }

    @Test
    @DisplayName("SessionRecord 實體轉換為 SessionRecordResponse 成功")
    void toResponse_ValidSessionRecord_Success() {
        // When
        SessionRecordResponse result = sessionRecordMapper.toResponse(sessionRecord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("專案開發時間");
        assertThat(result.getStartAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 9, 0));
        assertThat(result.getEndAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 11, 0));
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getPlannedNote()).isEqualTo("專注於核心功能開發");
        assertThat(result.getCompletionNote()).isEqualTo("成功完成核心功能");
    }

    @Test
    @DisplayName("SessionRecord 實體轉換為 SessionRecordResponse - 無任務關聯")
    void toResponse_SessionRecordWithoutTask_Success() {
        // Given
        SessionRecord recordWithoutTask = new SessionRecord(
            "簡單工作階段",
            LocalDateTime.of(2024, 1, 15, 14, 0),
            LocalDateTime.of(2024, 1, 15, 15, 30)
        );
        recordWithoutTask.setId(2L);
        recordWithoutTask.setPlannedNote("簡單工作");
        recordWithoutTask.setCompletionNote("完成");

        // When
        SessionRecordResponse result = sessionRecordMapper.toResponse(recordWithoutTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("簡單工作階段");
        assertThat(result.getTaskId()).isNull();
        assertThat(result.getPlannedNote()).isEqualTo("簡單工作");
        assertThat(result.getCompletionNote()).isEqualTo("完成");
    }

    @Test
    @DisplayName("SessionRecord 為 null 時返回 null")
    void toResponse_NullSessionRecord_ReturnsNull() {
        // When
        SessionRecordResponse result = sessionRecordMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("SessionRecord 列表轉換為 SessionRecordResponse 列表成功")
    void toResponseList_ValidSessionRecordList_Success() {
        // Given
        SessionRecord record2 = new SessionRecord(
            "另一個工作階段",
            LocalDateTime.of(2024, 1, 15, 14, 0),
            LocalDateTime.of(2024, 1, 15, 16, 0)
        );
        record2.setId(2L);

        List<SessionRecord> records = List.of(sessionRecord, record2);

        // When
        List<SessionRecordResponse> result = sessionRecordMapper.toResponseList(records);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("專案開發時間");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("另一個工作階段");
    }

    @Test
    @DisplayName("空 SessionRecord 列表轉換為空 SessionRecordResponse 列表")
    void toResponseList_EmptySessionRecordList_ReturnsEmptyList() {
        // Given
        List<SessionRecord> emptyRecords = List.of();

        // When
        List<SessionRecordResponse> result = sessionRecordMapper.toResponseList(emptyRecords);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SessionRecord 列表為 null 時返回空列表")
    void toResponseList_NullSessionRecordList_ReturnsEmptyList() {
        // When
        List<SessionRecordResponse> result = sessionRecordMapper.toResponseList(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
} 