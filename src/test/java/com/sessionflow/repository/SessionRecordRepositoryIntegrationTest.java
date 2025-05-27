package com.sessionflow.repository;

import com.sessionflow.model.SessionRecord;
import com.sessionflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SessionRecordRepository 整合測試")
class SessionRecordRepositoryIntegrationTest {

    @Autowired
    private SessionRecordRepository sessionRecordRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Task testTask;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        // 創建測試用的任務
        testTask = new Task("測試任務");
        testTask = taskRepository.save(testTask);
        
        // 設定基準時間
        baseTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("成功建立 SessionRecord")
    void shouldCreateSessionRecordSuccessfully() {
        // Given
        LocalDateTime startTime = baseTime;
        LocalDateTime endTime = baseTime.plusHours(2);
        
        SessionRecord record = new SessionRecord("專案開發紀錄", startTime, endTime);
        record.setTask(testTask);
        record.setPlannedNote("計畫完成核心功能");
        record.setCompletionNote("成功完成 80% 的功能");

        // When
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();

        // Then
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getTitle()).isEqualTo("專案開發紀錄");
        assertThat(savedRecord.getStartAt()).isEqualTo(startTime);
        assertThat(savedRecord.getEndAt()).isEqualTo(endTime);
        assertThat(savedRecord.getTask()).isNotNull();
        assertThat(savedRecord.getTask().getId()).isEqualTo(testTask.getId());
        assertThat(savedRecord.getPlannedNote()).isEqualTo("計畫完成核心功能");
        assertThat(savedRecord.getCompletionNote()).isEqualTo("成功完成 80% 的功能");
        assertThat(savedRecord.getCreatedAt()).isNotNull();
        assertThat(savedRecord.getUpdatedAt()).isNotNull();
        assertThat(savedRecord.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("建立不關聯任務的 SessionRecord")
    void shouldCreateSessionRecordWithoutTask() {
        // Given
        LocalDateTime startTime = baseTime;
        LocalDateTime endTime = baseTime.plusMinutes(30);
        
        SessionRecord record = new SessionRecord("獨立工作紀錄", startTime, endTime);
        record.setCompletionNote("完成了一些雜項工作");

        // When
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();

        // Then
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getTitle()).isEqualTo("獨立工作紀錄");
        assertThat(savedRecord.getTask()).isNull();
        assertThat(savedRecord.getCompletionNote()).isEqualTo("完成了一些雜項工作");
    }

    @Test
    @DisplayName("查詢單筆 SessionRecord")
    void shouldFindSessionRecordById() {
        // Given
        SessionRecord record = new SessionRecord("測試紀錄", baseTime, baseTime.plusHours(1));
        record.setTask(testTask);
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<SessionRecord> foundRecord = sessionRecordRepository.findById(savedRecord.getId());

        // Then
        assertThat(foundRecord).isPresent();
        assertThat(foundRecord.get().getTitle()).isEqualTo("測試紀錄");
        assertThat(foundRecord.get().getTask()).isNotNull();
        assertThat(foundRecord.get().getTask().getId()).isEqualTo(testTask.getId());
    }

    @Test
    @DisplayName("查詢全部 SessionRecord")
    void shouldFindAllSessionRecords() {
        // Given
        SessionRecord record1 = new SessionRecord("紀錄1", baseTime, baseTime.plusHours(1));
        SessionRecord record2 = new SessionRecord("紀錄2", baseTime.plusHours(2), baseTime.plusHours(3));
        
        sessionRecordRepository.saveAll(List.of(record1, record2));
        entityManager.flush();

        // When
        List<SessionRecord> allRecords = sessionRecordRepository.findAll();

        // Then
        assertThat(allRecords).hasSize(2);
        assertThat(allRecords).extracting(SessionRecord::getTitle)
                .containsExactlyInAnyOrder("紀錄1", "紀錄2");
    }

    @Test
    @DisplayName("根據開始時間區間查詢 SessionRecord")
    void shouldFindSessionRecordsByStartAtBetween() {
        // Given
        LocalDateTime day1Start = baseTime;
        LocalDateTime day2Start = baseTime.plusDays(1);
        LocalDateTime day3Start = baseTime.plusDays(2);
        
        SessionRecord record1 = new SessionRecord("第一天紀錄", day1Start, day1Start.plusHours(1));
        SessionRecord record2 = new SessionRecord("第二天紀錄", day2Start, day2Start.plusHours(1));
        SessionRecord record3 = new SessionRecord("第三天紀錄", day3Start, day3Start.plusHours(1));
        
        sessionRecordRepository.saveAll(List.of(record1, record2, record3));
        entityManager.flush();

        // When - 查詢第一天到第二天的紀錄
        List<SessionRecord> records = sessionRecordRepository.findByStartAtBetween(
                day1Start, day3Start);

        // Then
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SessionRecord::getTitle)
                .containsExactlyInAnyOrder("第一天紀錄", "第二天紀錄");
    }

    @Test
    @DisplayName("根據 taskId 查詢 SessionRecord")
    void shouldFindSessionRecordsByTaskId() {
        // Given
        Task anotherTask = new Task("另一個任務");
        anotherTask = taskRepository.save(anotherTask);
        
        SessionRecord record1 = new SessionRecord("任務1紀錄1", baseTime, baseTime.plusHours(1));
        record1.setTask(testTask);
        
        SessionRecord record2 = new SessionRecord("任務1紀錄2", baseTime.plusHours(2), baseTime.plusHours(3));
        record2.setTask(testTask);
        
        SessionRecord record3 = new SessionRecord("任務2紀錄", baseTime.plusHours(4), baseTime.plusHours(5));
        record3.setTask(anotherTask);
        
        sessionRecordRepository.saveAll(List.of(record1, record2, record3));
        entityManager.flush();

        // When
        List<SessionRecord> task1Records = sessionRecordRepository.findByTaskId(testTask.getId());

        // Then
        assertThat(task1Records).hasSize(2);
        assertThat(task1Records).extracting(SessionRecord::getTitle)
                .containsExactlyInAnyOrder("任務1紀錄1", "任務1紀錄2");
    }

    @Test
    @DisplayName("根據時間區間和 taskId 查詢 SessionRecord")
    void shouldFindSessionRecordsByStartAtBetweenAndTaskId() {
        // Given
        Task anotherTask = new Task("另一個任務");
        anotherTask = taskRepository.save(anotherTask);
        
        LocalDateTime day1 = baseTime;
        LocalDateTime day2 = baseTime.plusDays(1);
        LocalDateTime day3 = baseTime.plusDays(2);
        
        SessionRecord record1 = new SessionRecord("任務1第一天", day1, day1.plusHours(1));
        record1.setTask(testTask);
        
        SessionRecord record2 = new SessionRecord("任務1第二天", day2, day2.plusHours(1));
        record2.setTask(testTask);
        
        SessionRecord record3 = new SessionRecord("任務2第二天", day2, day2.plusHours(1));
        record3.setTask(anotherTask);
        
        sessionRecordRepository.saveAll(List.of(record1, record2, record3));
        entityManager.flush();

        // When - 查詢第一天到第三天，且屬於 testTask 的紀錄
        List<SessionRecord> records = sessionRecordRepository.findByStartAtBetweenAndTaskId(
                day1, day3, testTask.getId());

        // Then
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SessionRecord::getTitle)
                .containsExactlyInAnyOrder("任務1第一天", "任務1第二天");
    }

    @Test
    @DisplayName("按 ID 降序查詢所有 SessionRecord")
    void shouldFindAllSessionRecordsByOrderByIdDesc() {
        // Given
        SessionRecord record1 = new SessionRecord("第一個紀錄", baseTime, baseTime.plusHours(1));
        SessionRecord record2 = new SessionRecord("第二個紀錄", baseTime.plusHours(2), baseTime.plusHours(3));
        SessionRecord record3 = new SessionRecord("第三個紀錄", baseTime.plusHours(4), baseTime.plusHours(5));
        
        sessionRecordRepository.saveAll(List.of(record1, record2, record3));
        entityManager.flush();

        // When
        List<SessionRecord> records = sessionRecordRepository.findAllByOrderByIdDesc();

        // Then
        assertThat(records).hasSize(3);
        // 應該按 ID 降序排列（最新的在前）
        assertThat(records.get(0).getTitle()).isEqualTo("第三個紀錄");
        assertThat(records.get(1).getTitle()).isEqualTo("第二個紀錄");
        assertThat(records.get(2).getTitle()).isEqualTo("第一個紀錄");
    }

    @Test
    @DisplayName("更新 SessionRecord 欄位")
    void shouldUpdateSessionRecordFields() {
        // Given
        SessionRecord record = new SessionRecord("原始標題", baseTime, baseTime.plusHours(1));
        record.setPlannedNote("原始計畫");
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();
        
        LocalDateTime originalUpdatedAt = savedRecord.getUpdatedAt();
        
        // 等待一毫秒確保時間戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        savedRecord.setTitle("更新後標題");
        savedRecord.setPlannedNote("更新後計畫");
        savedRecord.setCompletionNote("新增完成備註");
        savedRecord.setTask(testTask);
        
        SessionRecord updatedRecord = sessionRecordRepository.save(savedRecord);
        entityManager.flush();

        // Then
        assertThat(updatedRecord.getTitle()).isEqualTo("更新後標題");
        assertThat(updatedRecord.getPlannedNote()).isEqualTo("更新後計畫");
        assertThat(updatedRecord.getCompletionNote()).isEqualTo("新增完成備註");
        assertThat(updatedRecord.getTask()).isNotNull();
        assertThat(updatedRecord.getTask().getId()).isEqualTo(testTask.getId());
        assertThat(updatedRecord.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedRecord.getCreatedAt()).isEqualTo(savedRecord.getCreatedAt());
    }

    @Test
    @DisplayName("刪除 SessionRecord 後資料庫應為空")
    void shouldDeleteSessionRecordAndDatabaseShouldBeEmpty() {
        // Given
        SessionRecord record1 = new SessionRecord("紀錄1", baseTime, baseTime.plusHours(1));
        SessionRecord record2 = new SessionRecord("紀錄2", baseTime.plusHours(2), baseTime.plusHours(3));
        sessionRecordRepository.saveAll(List.of(record1, record2));
        entityManager.flush();

        // When
        sessionRecordRepository.deleteAll();
        entityManager.flush();

        // Then
        List<SessionRecord> allRecords = sessionRecordRepository.findAll();
        assertThat(allRecords).isEmpty();
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 標題")
    void shouldValidateNotNullTitle() {
        // Given
        SessionRecord record = new SessionRecord();
        record.setStartAt(baseTime);
        record.setEndAt(baseTime.plusHours(1));
        // title 為 null

        // When & Then
        assertThatThrownBy(() -> {
            sessionRecordRepository.save(record);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 開始時間")
    void shouldValidateNotNullStartAt() {
        // Given
        SessionRecord record = new SessionRecord();
        record.setTitle("測試紀錄");
        record.setEndAt(baseTime.plusHours(1));
        // startAt 為 null

        // When & Then
        assertThatThrownBy(() -> {
            sessionRecordRepository.save(record);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 結束時間")
    void shouldValidateNotNullEndAt() {
        // Given
        SessionRecord record = new SessionRecord();
        record.setTitle("測試紀錄");
        record.setStartAt(baseTime);
        // endAt 為 null

        // When & Then
        assertThatThrownBy(() -> {
            sessionRecordRepository.save(record);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("時間欄位檢查 - createdAt 和 updatedAt 自動設定")
    void shouldAutoSetTimestamps() {
        // Given
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        
        // When
        SessionRecord record = new SessionRecord("時間測試紀錄", baseTime, baseTime.plusHours(1));
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();
        
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(savedRecord.getCreatedAt()).isNotNull();
        assertThat(savedRecord.getUpdatedAt()).isNotNull();
        assertThat(savedRecord.getCreatedAt()).isAfter(beforeCreate);
        assertThat(savedRecord.getCreatedAt()).isBefore(afterCreate);
        assertThat(savedRecord.getUpdatedAt()).isAfter(beforeCreate);
        assertThat(savedRecord.getUpdatedAt()).isBefore(afterCreate);
    }

    @Test
    @DisplayName("計算工作時長")
    void shouldCalculateDuration() {
        // Given
        LocalDateTime startTime = baseTime;
        LocalDateTime endTime = baseTime.plusHours(2).plusMinutes(30);
        
        SessionRecord record = new SessionRecord("時長測試", startTime, endTime);
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();

        // When & Then
        assertThat(savedRecord.getDurationMinutes()).isEqualTo(150); // 2.5 hours = 150 minutes
        assertThat(savedRecord.getDuration().toHours()).isEqualTo(2);
        assertThat(savedRecord.getDuration().toMinutesPart()).isEqualTo(30);
    }

    @Test
    @DisplayName("處理長文本備註")
    void shouldHandleLongNoteText() {
        // Given
        String longPlannedNote = "這是一個很長的計畫備註".repeat(50);
        String longCompletionNote = "這是一個很長的完成備註".repeat(50);
        
        SessionRecord record = new SessionRecord("長備註測試", baseTime, baseTime.plusHours(1));
        record.setPlannedNote(longPlannedNote);
        record.setCompletionNote(longCompletionNote);

        // When
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();
        entityManager.clear();

        // Then
        SessionRecord foundRecord = sessionRecordRepository.findById(savedRecord.getId()).orElseThrow();
        assertThat(foundRecord.getPlannedNote()).isEqualTo(longPlannedNote);
        assertThat(foundRecord.getCompletionNote()).isEqualTo(longCompletionNote);
    }

    @Test
    @DisplayName("刪除關聯的 Task 不應影響 SessionRecord")
    void shouldNotAffectSessionRecordWhenTaskIsDeleted() {
        // Given
        SessionRecord record = new SessionRecord("關聯任務的紀錄", baseTime, baseTime.plusHours(1));
        record.setTask(testTask);
        SessionRecord savedRecord = sessionRecordRepository.save(record);
        entityManager.flush();

        // When - 先將關聯設為 null，再刪除任務
        sessionRecordRepository.setTaskToNullByTaskId(testTask.getId());
        entityManager.flush();
        taskRepository.delete(testTask);
        entityManager.flush();
        entityManager.clear();

        // Then - SessionRecord 應該仍然存在，但 task 關聯為 null
        SessionRecord foundRecord = sessionRecordRepository.findById(savedRecord.getId()).orElseThrow();
        assertThat(foundRecord.getTitle()).isEqualTo("關聯任務的紀錄");
        assertThat(foundRecord.getTask()).isNull();
    }
} 