package com.sessionflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ScheduleEntry Model 測試")
class ScheduleEntryTest {

    private final LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    @DisplayName("使用標題和時間建立 ScheduleEntry")
    void shouldCreateScheduleEntryWithTitleAndTime() {
        // Given
        String title = "測試排程";
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(2);

        // When
        ScheduleEntry entry = new ScheduleEntry(title, startAt, endAt);

        // Then
        assertThat(entry.getTitle()).isEqualTo(title);
        assertThat(entry.getStartAt()).isEqualTo(startAt);
        assertThat(entry.getEndAt()).isEqualTo(endAt);
        assertThat(entry.getId()).isNull();
        assertThat(entry.getNote()).isNull();
        assertThat(entry.getTask()).isNull();
    }

    @Test
    @DisplayName("使用預設建構子建立 ScheduleEntry")
    void shouldCreateScheduleEntryWithDefaultConstructor() {
        // When
        ScheduleEntry entry = new ScheduleEntry();

        // Then
        assertThat(entry.getId()).isNull();
        assertThat(entry.getTitle()).isNull();
        assertThat(entry.getStartAt()).isNull();
        assertThat(entry.getEndAt()).isNull();
        assertThat(entry.getNote()).isNull();
        assertThat(entry.getTask()).isNull();
    }

    @Test
    @DisplayName("設定和取得 ScheduleEntry 屬性")
    void shouldSetAndGetScheduleEntryProperties() {
        // Given
        ScheduleEntry entry = new ScheduleEntry();
        String title = "排程標題";
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(1);
        String note = "排程備註";
        Task task = new Task("關聯任務");

        // When
        entry.setTitle(title);
        entry.setStartAt(startAt);
        entry.setEndAt(endAt);
        entry.setNote(note);
        entry.setTask(task);

        // Then
        assertThat(entry.getTitle()).isEqualTo(title);
        assertThat(entry.getStartAt()).isEqualTo(startAt);
        assertThat(entry.getEndAt()).isEqualTo(endAt);
        assertThat(entry.getNote()).isEqualTo(note);
        assertThat(entry.getTask()).isEqualTo(task);
    }

    @Test
    @DisplayName("計算排程時長 - 分鐘")
    void shouldCalculateDurationInMinutes() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusHours(2).plusMinutes(30);
        ScheduleEntry entry = new ScheduleEntry("時長測試", startAt, endAt);

        // When
        long durationMinutes = entry.getDurationMinutes();

        // Then
        assertThat(durationMinutes).isEqualTo(150); // 2.5 hours = 150 minutes
    }

    @Test
    @DisplayName("檢測排程重疊 - 完全重疊")
    void shouldDetectCompleteOverlap() {
        // Given
        LocalDateTime start1 = baseTime;
        LocalDateTime end1 = baseTime.plusHours(2);
        ScheduleEntry entry1 = new ScheduleEntry("排程1", start1, end1);

        LocalDateTime start2 = baseTime.plusMinutes(30);
        LocalDateTime end2 = baseTime.plusHours(1).plusMinutes(30);
        ScheduleEntry entry2 = new ScheduleEntry("排程2", start2, end2);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue();
        assertThat(entry2.isOverlapping(entry1)).isTrue();
    }

    @Test
    @DisplayName("檢測排程重疊 - 部分重疊")
    void shouldDetectPartialOverlap() {
        // Given
        LocalDateTime start1 = baseTime;
        LocalDateTime end1 = baseTime.plusHours(2);
        ScheduleEntry entry1 = new ScheduleEntry("排程1", start1, end1);

        LocalDateTime start2 = baseTime.plusHours(1);
        LocalDateTime end2 = baseTime.plusHours(3);
        ScheduleEntry entry2 = new ScheduleEntry("排程2", start2, end2);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue();
        assertThat(entry2.isOverlapping(entry1)).isTrue();
    }

    @Test
    @DisplayName("檢測排程不重疊 - 相鄰但不重疊")
    void shouldDetectNoOverlapAdjacent() {
        // Given
        LocalDateTime start1 = baseTime;
        LocalDateTime end1 = baseTime.plusHours(2);
        ScheduleEntry entry1 = new ScheduleEntry("排程1", start1, end1);

        LocalDateTime start2 = baseTime.plusHours(2); // 緊接著開始
        LocalDateTime end2 = baseTime.plusHours(4);
        ScheduleEntry entry2 = new ScheduleEntry("排程2", start2, end2);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isFalse();
        assertThat(entry2.isOverlapping(entry1)).isFalse();
    }

    @Test
    @DisplayName("檢測排程不重疊 - 完全分離")
    void shouldDetectNoOverlapSeparate() {
        // Given
        LocalDateTime start1 = baseTime;
        LocalDateTime end1 = baseTime.plusHours(1);
        ScheduleEntry entry1 = new ScheduleEntry("排程1", start1, end1);

        LocalDateTime start2 = baseTime.plusHours(3);
        LocalDateTime end2 = baseTime.plusHours(4);
        ScheduleEntry entry2 = new ScheduleEntry("排程2", start2, end2);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isFalse();
        assertThat(entry2.isOverlapping(entry1)).isFalse();
    }

    @Test
    @DisplayName("檢測排程重疊 - 一個包含另一個")
    void shouldDetectOverlapContainment() {
        // Given
        LocalDateTime start1 = baseTime;
        LocalDateTime end1 = baseTime.plusHours(4);
        ScheduleEntry entry1 = new ScheduleEntry("外層排程", start1, end1);

        LocalDateTime start2 = baseTime.plusHours(1);
        LocalDateTime end2 = baseTime.plusHours(3);
        ScheduleEntry entry2 = new ScheduleEntry("內層排程", start2, end2);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue();
        assertThat(entry2.isOverlapping(entry1)).isTrue();
    }

    @Test
    @DisplayName("建立排程時驗證結束時間必須晚於開始時間")
    void shouldValidateEndTimeAfterStartTime() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.minusHours(1); // 結束時間早於開始時間

        // When & Then
        assertThatThrownBy(() -> new ScheduleEntry("無效排程", startAt, endAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("結束時間必須晚於開始時間");
    }

    @Test
    @DisplayName("建立排程時驗證結束時間不能等於開始時間")
    void shouldValidateEndTimeNotEqualStartTime() {
        // Given
        LocalDateTime time = baseTime;

        // When & Then
        assertThatThrownBy(() -> new ScheduleEntry("零時長排程", time, time))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("結束時間必須晚於開始時間");
    }

    @Test
    @DisplayName("ScheduleEntry 的 equals 和 hashCode")
    void shouldImplementEqualsAndHashCode() {
        // Given
        ScheduleEntry entry1 = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));
        entry1.setId(1L);
        
        ScheduleEntry entry2 = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));
        entry2.setId(1L);
        
        ScheduleEntry entry3 = new ScheduleEntry("不同排程", baseTime, baseTime.plusHours(1));
        entry3.setId(2L);

        // Then
        assertThat(entry1).isEqualTo(entry2);
        assertThat(entry1).isNotEqualTo(entry3);
        assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
    }

    @Test
    @DisplayName("ScheduleEntry 的 toString")
    void shouldImplementToString() {
        // Given
        ScheduleEntry entry = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));
        entry.setId(1L);
        entry.setNote("測試備註");

        // When
        String toString = entry.toString();

        // Then
        assertThat(toString).contains("ScheduleEntry");
        assertThat(toString).contains("測試排程");
    }

    @Test
    @DisplayName("ScheduleEntry 可以設定空的備註")
    void shouldAllowNullNote() {
        // Given
        ScheduleEntry entry = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));

        // When
        entry.setNote(null);

        // Then
        assertThat(entry.getNote()).isNull();
    }

    @Test
    @DisplayName("ScheduleEntry 可以不關聯任務")
    void shouldAllowNullTask() {
        // Given
        ScheduleEntry entry = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));

        // When
        entry.setTask(null);

        // Then
        assertThat(entry.getTask()).isNull();
    }

    @Test
    @DisplayName("ScheduleEntry 可以關聯和取消關聯任務")
    void shouldAssociateAndDisassociateTask() {
        // Given
        ScheduleEntry entry = new ScheduleEntry("測試排程", baseTime, baseTime.plusHours(1));
        Task task = new Task("測試任務");

        // When - 關聯任務
        entry.setTask(task);

        // Then
        assertThat(entry.getTask()).isEqualTo(task);

        // When - 取消關聯
        entry.setTask(null);

        // Then
        assertThat(entry.getTask()).isNull();
    }

    @Test
    @DisplayName("計算短時間排程的時長")
    void shouldCalculateShortDuration() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusMinutes(30);
        ScheduleEntry entry = new ScheduleEntry("短排程", startAt, endAt);

        // When
        long durationMinutes = entry.getDurationMinutes();

        // Then
        assertThat(durationMinutes).isEqualTo(30);
    }

    @Test
    @DisplayName("計算長時間排程的時長")
    void shouldCalculateLongDuration() {
        // Given
        LocalDateTime startAt = baseTime;
        LocalDateTime endAt = baseTime.plusDays(1).plusHours(2);
        ScheduleEntry entry = new ScheduleEntry("長排程", startAt, endAt);

        // When
        long durationMinutes = entry.getDurationMinutes();

        // Then
        long expectedMinutes = 24 * 60 + 2 * 60; // 1 day + 2 hours
        assertThat(durationMinutes).isEqualTo(expectedMinutes);
    }

    @Test
    @DisplayName("檢測與自己的重疊應該為 true")
    void shouldDetectOverlapWithSelf() {
        // Given
        ScheduleEntry entry = new ScheduleEntry("自我排程", baseTime, baseTime.plusHours(1));

        // When & Then
        assertThat(entry.isOverlapping(entry)).isTrue();
    }

    @Test
    @DisplayName("檢測排程重疊 - 邊界情況：開始時間相同")
    void shouldDetectOverlapSameStartTime() {
        // Given
        LocalDateTime start = baseTime;
        ScheduleEntry entry1 = new ScheduleEntry("排程1", start, start.plusHours(2));
        ScheduleEntry entry2 = new ScheduleEntry("排程2", start, start.plusHours(1));

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue();
        assertThat(entry2.isOverlapping(entry1)).isTrue();
    }

    @Test
    @DisplayName("檢測排程重疊 - 邊界情況：結束時間相同")
    void shouldDetectOverlapSameEndTime() {
        // Given
        LocalDateTime end = baseTime.plusHours(2);
        ScheduleEntry entry1 = new ScheduleEntry("排程1", baseTime, end);
        ScheduleEntry entry2 = new ScheduleEntry("排程2", baseTime.plusHours(1), end);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue();
        assertThat(entry2.isOverlapping(entry1)).isTrue();
    }
} 