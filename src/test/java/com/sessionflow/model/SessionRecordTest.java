package com.sessionflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionRecord Model 測試")
class SessionRecordTest {

    private final LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    @DisplayName("使用標題和時間建立 SessionRecord")
    void shouldCreateSessionRecordWithTitleAndTime() {
        // Given
        String title = "測試工作紀錄";
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(2);

        // When
        SessionRecord record = new SessionRecord(title, startAt, endAt);

        // Then
        assertThat(record.getTitle()).isEqualTo(title);
        assertThat(record.getStartAt()).isEqualTo(startAt);
        assertThat(record.getEndAt()).isEqualTo(endAt);
        assertThat(record.getId()).isNull();
        assertThat(record.getPlannedNote()).isNull();
        assertThat(record.getCompletionNote()).isNull();
        assertThat(record.getTask()).isNull();
        assertThat(record.getCreatedAt()).isNull();
        assertThat(record.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("使用預設建構子建立 SessionRecord")
    void shouldCreateSessionRecordWithDefaultConstructor() {
        // When
        SessionRecord record = new SessionRecord();

        // Then
        assertThat(record.getId()).isNull();
        assertThat(record.getTitle()).isNull();
        assertThat(record.getStartAt()).isNull();
        assertThat(record.getEndAt()).isNull();
        assertThat(record.getPlannedNote()).isNull();
        assertThat(record.getCompletionNote()).isNull();
        assertThat(record.getTask()).isNull();
        assertThat(record.getCreatedAt()).isNull();
        assertThat(record.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("設定和取得 SessionRecord 屬性")
    void shouldSetAndGetSessionRecordProperties() {
        // Given
        SessionRecord record = new SessionRecord();
        String title = "工作紀錄標題";
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(1);
        String plannedNote = "計畫備註";
        String completionNote = "完成備註";
        Task task = new Task("關聯任務");

        // When
        record.setTitle(title);
        record.setStartAt(startAt);
        record.setEndAt(endAt);
        record.setPlannedNote(plannedNote);
        record.setCompletionNote(completionNote);
        record.setTask(task);

        // Then
        assertThat(record.getTitle()).isEqualTo(title);
        assertThat(record.getStartAt()).isEqualTo(startAt);
        assertThat(record.getEndAt()).isEqualTo(endAt);
        assertThat(record.getPlannedNote()).isEqualTo(plannedNote);
        assertThat(record.getCompletionNote()).isEqualTo(completionNote);
        assertThat(record.getTask()).isEqualTo(task);
    }

    @Test
    @DisplayName("計算工作時長 - 分鐘")
    void shouldCalculateDurationInMinutes() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(2).plusMinutes(30);
        SessionRecord record = new SessionRecord("時長測試", startAt, endAt);

        // When
        long durationMinutes = record.getDurationMinutes();

        // Then
        assertThat(durationMinutes).isEqualTo(150); // 2.5 hours = 150 minutes
    }

    @Test
    @DisplayName("計算工作時長 - Duration 物件")
    void shouldCalculateDurationAsDurationObject() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(3).plusMinutes(45);
        SessionRecord record = new SessionRecord("時長測試", startAt, endAt);

        // When
        Duration duration = record.getDuration();

        // Then
        assertThat(duration.toHours()).isEqualTo(3);
        assertThat(duration.toMinutesPart()).isEqualTo(45);
        assertThat(duration.toMinutes()).isEqualTo(225); // 3 hours 45 minutes = 225 minutes
    }

    @Test
    @DisplayName("零時長的工作紀錄")
    void shouldHandleZeroDuration() {
        // Given
        LocalDateTime time = baseTime;
        SessionRecord record = new SessionRecord("零時長測試", time, time);

        // When
        long durationMinutes = record.getDurationMinutes();
        Duration duration = record.getDuration();

        // Then
        assertThat(durationMinutes).isEqualTo(0);
        assertThat(duration.isZero()).isTrue();
    }

    @Test
    @DisplayName("SessionRecord 的 equals 和 hashCode")
    void shouldImplementEqualsAndHashCode() {
        // Given
        SessionRecord record1 = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        record1.setId(1L);
        
        SessionRecord record2 = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        record2.setId(1L);
        
        SessionRecord record3 = new SessionRecord("不同紀錄", baseTime, baseTime.plusHours(1));
        record3.setId(2L);

        // Then
        assertThat(record1).isEqualTo(record2);
        assertThat(record1).isNotEqualTo(record3);
        assertThat(record1.hashCode()).isEqualTo(record2.hashCode());
    }

    @Test
    @DisplayName("SessionRecord 的 toString")
    void shouldImplementToString() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        record.setId(1L);
        record.setPlannedNote("計畫備註");

        // When
        String toString = record.toString();

        // Then
        assertThat(toString).contains("SessionRecord");
        assertThat(toString).contains("測試紀錄");
    }

    @Test
    @DisplayName("SessionRecord 可以設定空的備註")
    void shouldAllowNullNotes() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));

        // When
        record.setPlannedNote(null);
        record.setCompletionNote(null);

        // Then
        assertThat(record.getPlannedNote()).isNull();
        assertThat(record.getCompletionNote()).isNull();
    }

    @Test
    @DisplayName("SessionRecord 可以不關聯任務")
    void shouldAllowNullTask() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));

        // When
        record.setTask(null);

        // Then
        assertThat(record.getTask()).isNull();
    }

    @Test
    @DisplayName("SessionRecord 可以關聯和取消關聯任務")
    void shouldAssociateAndDisassociateTask() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        Task task = new Task("測試任務");

        // When - 關聯任務
        record.setTask(task);

        // Then
        assertThat(record.getTask()).isEqualTo(task);

        // When - 取消關聯
        record.setTask(null);

        // Then
        assertThat(record.getTask()).isNull();
    }

    @Test
    @DisplayName("長時間工作紀錄的時長計算")
    void shouldCalculateLongDuration() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusDays(1).plusHours(5).plusMinutes(30);
        SessionRecord record = new SessionRecord("長時間工作", startAt, endAt);

        // When
        long durationMinutes = record.getDurationMinutes();
        Duration duration = record.getDuration();

        // Then
        long expectedMinutes = 24 * 60 + 5 * 60 + 30; // 1 day + 5 hours + 30 minutes
        assertThat(durationMinutes).isEqualTo(expectedMinutes);
        assertThat(duration.toDays()).isEqualTo(1);
        assertThat(duration.toHoursPart()).isEqualTo(5);
        assertThat(duration.toMinutesPart()).isEqualTo(30);
    }

    @Test
    @DisplayName("短時間工作紀錄的時長計算")
    void shouldCalculateShortDuration() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusMinutes(15);
        SessionRecord record = new SessionRecord("短時間工作", startAt, endAt);

        // When
        long durationMinutes = record.getDurationMinutes();
        Duration duration = record.getDuration();

        // Then
        assertThat(durationMinutes).isEqualTo(15);
        assertThat(duration.toMinutes()).isEqualTo(15);
        assertThat(duration.getSeconds()).isEqualTo(15 * 60);
    }

    @Test
    @DisplayName("設定時間戳記")
    void shouldSetTimestamps() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusMinutes(5);

        // When
        record.setCreatedAt(createdAt);
        record.setUpdatedAt(updatedAt);

        // Then
        assertThat(record.getCreatedAt()).isEqualTo(createdAt);
        assertThat(record.getUpdatedAt()).isEqualTo(updatedAt);
    }
} 