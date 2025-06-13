package com.sessionflow.repository;

import com.sessionflow.model.ScheduleEntry;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ScheduleEntryRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Test
    void shouldCreateScheduleEntrySuccessfully() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 30);
        ScheduleEntry scheduleEntry = new ScheduleEntry("會議討論", startAt, endAt);
        scheduleEntry.setNote("重要會議備註");

        // When
        ScheduleEntry saved = scheduleEntryRepository.save(scheduleEntry);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("會議討論");
        assertThat(saved.getStartAt()).isEqualTo(startAt);
        assertThat(saved.getEndAt()).isEqualTo(endAt);
        assertThat(saved.getNote()).isEqualTo("重要會議備註");
        assertThat(saved.getDurationMinutes()).isEqualTo(90);
    }

    @Test
    void shouldCreateScheduleEntryWithTask() {
        // Given
        Task task = new Task("相關任務");
        task.setNote("任務描述");
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = entityManager.persistAndFlush(task);

        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 15, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("任務相關會議", startAt, endAt);
        scheduleEntry.setTask(savedTask);

        // When
        ScheduleEntry saved = scheduleEntryRepository.save(scheduleEntry);
        entityManager.flush();

        // Then
        assertThat(saved.getTask()).isNotNull();
        assertThat(saved.getTask().getId()).isEqualTo(savedTask.getId());
        assertThat(saved.getTask().getTitle()).isEqualTo("相關任務");
    }

    @Test
    void shouldFindScheduleEntryById() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("晨會", startAt, endAt);
        ScheduleEntry saved = entityManager.persistAndFlush(scheduleEntry);

        // When
        Optional<ScheduleEntry> found = scheduleEntryRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("晨會");
        assertThat(found.get().getStartAt()).isEqualTo(startAt);
        assertThat(found.get().getEndAt()).isEqualTo(endAt);
    }

    @Test
    void shouldFindAllScheduleEntries() {
        // Given
        LocalDateTime startAt1 = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime endAt1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        ScheduleEntry entry1 = new ScheduleEntry("晨會", startAt1, endAt1);

        LocalDateTime startAt2 = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime endAt2 = LocalDateTime.of(2024, 1, 15, 15, 30);
        ScheduleEntry entry2 = new ScheduleEntry("下午會議", startAt2, endAt2);

        entityManager.persistAndFlush(entry1);
        entityManager.persistAndFlush(entry2);

        // When
        List<ScheduleEntry> entries = scheduleEntryRepository.findAll();

        // Then
        assertThat(entries).hasSize(2);
        assertThat(entries).extracting(ScheduleEntry::getTitle)
                .containsExactlyInAnyOrder("晨會", "下午會議");
    }

    @Test
    void shouldUpdateScheduleEntry() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("原始會議", startAt, endAt);
        ScheduleEntry saved = entityManager.persistAndFlush(scheduleEntry);

        // When
        saved.setTitle("更新後的會議");
        saved.setNote("新增備註");
        LocalDateTime newEndAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        saved.setEndAt(newEndAt);
        
        ScheduleEntry updated = scheduleEntryRepository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getTitle()).isEqualTo("更新後的會議");
        assertThat(updated.getNote()).isEqualTo("新增備註");
        assertThat(updated.getEndAt()).isEqualTo(newEndAt);
        assertThat(updated.getDurationMinutes()).isEqualTo(120);
    }

    @Test
    void shouldDeleteScheduleEntry() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("待刪除會議", startAt, endAt);
        ScheduleEntry saved = entityManager.persistAndFlush(scheduleEntry);

        // When
        scheduleEntryRepository.deleteById(saved.getId());
        entityManager.flush();

        // Then
        Optional<ScheduleEntry> found = scheduleEntryRepository.findById(saved.getId());
        assertThat(found).isEmpty();
        assertThat(scheduleEntryRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFindByDateRange() {
        // Given
        // 會議1: 2024-01-15 09:00-10:00 (在範圍內)
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        ScheduleEntry entry1 = new ScheduleEntry("晨會", start1, end1);

        // 會議2: 2024-01-15 14:00-15:00 (在範圍內)
        LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 15, 0);
        ScheduleEntry entry2 = new ScheduleEntry("下午會議", start2, end2);

        // 會議3: 2024-01-16 10:00-11:00 (範圍外)
        LocalDateTime start3 = LocalDateTime.of(2024, 1, 16, 10, 0);
        LocalDateTime end3 = LocalDateTime.of(2024, 1, 16, 11, 0);
        ScheduleEntry entry3 = new ScheduleEntry("隔日會議", start3, end3);

        // 會議4: 2024-01-15 08:00-09:30 (部分重疊)
        LocalDateTime start4 = LocalDateTime.of(2024, 1, 15, 8, 0);
        LocalDateTime end4 = LocalDateTime.of(2024, 1, 15, 9, 30);
        ScheduleEntry entry4 = new ScheduleEntry("早期會議", start4, end4);

        entityManager.persistAndFlush(entry1);
        entityManager.persistAndFlush(entry2);
        entityManager.persistAndFlush(entry3);
        entityManager.persistAndFlush(entry4);

        // When - 查詢 2024-01-15 09:00-15:00 範圍
        LocalDateTime rangeStart = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2024, 1, 15, 15, 0);
        List<ScheduleEntry> entries = scheduleEntryRepository.findByDateRange(rangeStart, rangeEnd);

        // Then
        assertThat(entries).hasSize(3); // entry1, entry2, entry4 (部分重疊)
        assertThat(entries).extracting(ScheduleEntry::getTitle)
                .containsExactly("早期會議", "晨會", "下午會議"); // 按開始時間排序
    }

    @Test
    void shouldFindByStartAtBetween() {
        // Given
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        ScheduleEntry entry1 = new ScheduleEntry("會議A", start1, end1);

        LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 11, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 12, 0);
        ScheduleEntry entry2 = new ScheduleEntry("會議B", start2, end2);

        LocalDateTime start3 = LocalDateTime.of(2024, 1, 15, 16, 0);
        LocalDateTime end3 = LocalDateTime.of(2024, 1, 15, 17, 0);
        ScheduleEntry entry3 = new ScheduleEntry("會議C", start3, end3);

        entityManager.persistAndFlush(entry1);
        entityManager.persistAndFlush(entry2);
        entityManager.persistAndFlush(entry3);

        // When - 查詢開始時間在 09:00-12:00 之間的會議
        LocalDateTime rangeStart = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2024, 1, 15, 12, 0);
        List<ScheduleEntry> entries = scheduleEntryRepository.findByStartAtBetweenOrderByStartAtAsc(rangeStart, rangeEnd);

        // Then
        assertThat(entries).hasSize(2);
        assertThat(entries).extracting(ScheduleEntry::getTitle)
                .containsExactly("會議A", "會議B"); // 按開始時間排序
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry();
        scheduleEntry.setTitle(null);
        scheduleEntry.setStartAt(startAt);
        scheduleEntry.setEndAt(endAt);

        // When & Then
        assertThatThrownBy(() -> {
            scheduleEntryRepository.save(scheduleEntry);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry();
        scheduleEntry.setTitle("   ");
        scheduleEntry.setStartAt(startAt);
        scheduleEntry.setEndAt(endAt);

        // When & Then
        assertThatThrownBy(() -> {
            scheduleEntryRepository.save(scheduleEntry);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenStartAtIsNull() {
        // Given
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry();
        scheduleEntry.setTitle("測試會議");
        scheduleEntry.setStartAt(null);
        scheduleEntry.setEndAt(endAt);

        // When & Then
        assertThatThrownBy(() -> {
            scheduleEntryRepository.save(scheduleEntry);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenEndAtIsNull() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry();
        scheduleEntry.setTitle("測試會議");
        scheduleEntry.setStartAt(startAt);
        scheduleEntry.setEndAt(null);

        // When & Then
        assertThatThrownBy(() -> {
            scheduleEntryRepository.save(scheduleEntry);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 10, 0); // 結束時間早於開始時間

        // When & Then
        assertThatThrownBy(() -> {
            new ScheduleEntry("無效時間會議", startAt, endAt);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("結束時間必須晚於開始時間");
    }

    @Test
    void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 10, 0); // 結束時間等於開始時間

        // When & Then
        assertThatThrownBy(() -> {
            new ScheduleEntry("零時長會議", startAt, endAt);
        }).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("結束時間必須晚於開始時間");
    }

    @Test
    void shouldCalculateDurationCorrectly() {
        // Given
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 12, 30);
        ScheduleEntry scheduleEntry = new ScheduleEntry("時長測試會議", startAt, endAt);

        // When
        long durationMinutes = scheduleEntry.getDurationMinutes();

        // Then
        assertThat(durationMinutes).isEqualTo(150); // 2.5 小時 = 150 分鐘
    }

    @Test
    void shouldDetectOverlappingSchedules() {
        // Given
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 12, 0);
        ScheduleEntry entry1 = new ScheduleEntry("會議1", start1, end1);

        LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 11, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 13, 0);
        ScheduleEntry entry2 = new ScheduleEntry("會議2", start2, end2);

        LocalDateTime start3 = LocalDateTime.of(2024, 1, 15, 13, 0);
        LocalDateTime end3 = LocalDateTime.of(2024, 1, 15, 14, 0);
        ScheduleEntry entry3 = new ScheduleEntry("會議3", start3, end3);

        // When & Then
        assertThat(entry1.isOverlapping(entry2)).isTrue(); // 重疊
        assertThat(entry1.isOverlapping(entry3)).isFalse(); // 不重疊
        assertThat(entry2.isOverlapping(entry3)).isFalse(); // 相鄰但不重疊
    }

    @Test
    void shouldHandleLongNoteText() {
        // Given
        String longNote = "這是一個非常長的備註".repeat(100);
        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("長備註會議", startAt, endAt);
        scheduleEntry.setNote(longNote);

        // When
        ScheduleEntry saved = scheduleEntryRepository.save(scheduleEntry);
        entityManager.flush();

        // Then
        assertThat(saved.getNote()).isEqualTo(longNote);
        assertThat(saved.getNote().length()).isGreaterThan(500);
    }

    @Test
    void shouldCreateScheduleEntryWithTaskAssociation() {
        // Given
        Task task = new Task("待刪除任務");
        task.setNote("任務描述");
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = entityManager.persistAndFlush(task);

        LocalDateTime startAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        ScheduleEntry scheduleEntry = new ScheduleEntry("關聯會議", startAt, endAt);
        scheduleEntry.setTask(savedTask);
        
        // When
        ScheduleEntry savedEntry = entityManager.persistAndFlush(scheduleEntry);

        // Then - 排程應該正確關聯任務
        assertThat(savedEntry.getTask()).isNotNull();
        assertThat(savedEntry.getTask().getId()).isEqualTo(savedTask.getId());
        assertThat(savedEntry.getTask().getTitle()).isEqualTo("待刪除任務");
        assertThat(savedEntry.getTitle()).isEqualTo("關聯會議");
    }

    @Test
    void shouldDeleteScheduleEntriesByTaskId() {
        // Given
        Task task1 = new Task("任務1");
        task1 = entityManager.persistAndFlush(task1);
        
        Task task2 = new Task("任務2");
        task2 = entityManager.persistAndFlush(task2);

        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        ScheduleEntry entry1 = new ScheduleEntry("關聯任務1的排程1", baseTime, baseTime.plusHours(1));
        entry1.setTask(task1);
        
        ScheduleEntry entry2 = new ScheduleEntry("關聯任務1的排程2", baseTime.plusHours(2), baseTime.plusHours(3));
        entry2.setTask(task1);
        
        ScheduleEntry entry3 = new ScheduleEntry("關聯任務2的排程", baseTime.plusHours(4), baseTime.plusHours(5));
        entry3.setTask(task2);
        
        ScheduleEntry entry4 = new ScheduleEntry("無關聯的排程", baseTime.plusHours(6), baseTime.plusHours(7));
        // 不設定 task
        
        entityManager.persistAndFlush(entry1);
        entityManager.persistAndFlush(entry2);
        entityManager.persistAndFlush(entry3);
        entityManager.persistAndFlush(entry4);

        // When
        scheduleEntryRepository.deleteByTaskId(task1.getId());
        entityManager.flush();

        // Then
        List<ScheduleEntry> remainingEntries = scheduleEntryRepository.findAll();
        assertThat(remainingEntries).hasSize(2);
        assertThat(remainingEntries).extracting(ScheduleEntry::getTitle)
                .containsExactlyInAnyOrder("關聯任務2的排程", "無關聯的排程");
    }

    @Test
    void shouldNotDeleteScheduleEntriesWhenTaskIdNotExists() {
        // Given
        Task task = new Task("測試任務");
        task = entityManager.persistAndFlush(task);

        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        ScheduleEntry entry1 = new ScheduleEntry("排程1", baseTime, baseTime.plusHours(1));
        entry1.setTask(task);
        
        ScheduleEntry entry2 = new ScheduleEntry("排程2", baseTime.plusHours(2), baseTime.plusHours(3));
        // 不設定 task
        
        entityManager.persistAndFlush(entry1);
        entityManager.persistAndFlush(entry2);

        // When
        scheduleEntryRepository.deleteByTaskId(999L); // 不存在的 taskId
        entityManager.flush();

        // Then
        List<ScheduleEntry> allEntries = scheduleEntryRepository.findAll();
        assertThat(allEntries).hasSize(2);
        assertThat(allEntries).extracting(ScheduleEntry::getTitle)
                .containsExactlyInAnyOrder("排程1", "排程2");
    }
} 