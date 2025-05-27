package com.sessionflow.repository;

import com.sessionflow.model.Session;
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
@DisplayName("SessionRepository 整合測試")
class SessionRepositoryIntegrationTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Task testTask;

    @BeforeEach
    void setUp() {
        // 創建測試用的任務
        testTask = new Task("測試任務");
        testTask = taskRepository.save(testTask);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("成功建立 Session")
    void shouldCreateSessionSuccessfully() {
        // Given
        Session session = new Session("專案開發時間");
        session.setTask(testTask);
        session.setEndReminder(LocalDateTime.now().plusHours(2));
        session.setNote("專注於核心功能開發");

        // When
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();

        // Then
        assertThat(savedSession.getId()).isNotNull();
        assertThat(savedSession.getTitle()).isEqualTo("專案開發時間");
        assertThat(savedSession.getTask()).isNotNull();
        assertThat(savedSession.getTask().getId()).isEqualTo(testTask.getId());
        assertThat(savedSession.getEndReminder()).isNotNull();
        assertThat(savedSession.getNote()).isEqualTo("專注於核心功能開發");
    }

    @Test
    @DisplayName("建立不關聯任務的 Session")
    void shouldCreateSessionWithoutTask() {
        // Given
        Session session = new Session("獨立工作階段");
        session.setNote("不關聯特定任務的工作時間");

        // When
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();

        // Then
        assertThat(savedSession.getId()).isNotNull();
        assertThat(savedSession.getTitle()).isEqualTo("獨立工作階段");
        assertThat(savedSession.getTask()).isNull();
        assertThat(savedSession.getNote()).isEqualTo("不關聯特定任務的工作時間");
    }

    @Test
    @DisplayName("查詢單筆 Session")
    void shouldFindSessionById() {
        // Given
        Session session = new Session("測試工作階段");
        session.setTask(testTask);
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Session> foundSession = sessionRepository.findById(savedSession.getId());

        // Then
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getTitle()).isEqualTo("測試工作階段");
        assertThat(foundSession.get().getTask()).isNotNull();
        assertThat(foundSession.get().getTask().getId()).isEqualTo(testTask.getId());
    }

    @Test
    @DisplayName("查詢全部 Session")
    void shouldFindAllSessions() {
        // Given
        Session session1 = new Session("工作階段1");
        session1.setTask(testTask);
        
        Session session2 = new Session("工作階段2");
        // session2 不關聯任務
        
        sessionRepository.saveAll(List.of(session1, session2));
        entityManager.flush();

        // When
        List<Session> allSessions = sessionRepository.findAll();

        // Then
        assertThat(allSessions).hasSize(2);
        assertThat(allSessions).extracting(Session::getTitle)
                .containsExactlyInAnyOrder("工作階段1", "工作階段2");
    }

    @Test
    @DisplayName("更新 Session 欄位")
    void shouldUpdateSessionFields() {
        // Given
        Session session = new Session("原始標題");
        session.setNote("原始備註");
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();

        // When
        savedSession.setTitle("更新後標題");
        savedSession.setNote("更新後備註");
        savedSession.setTask(testTask);
        savedSession.setEndReminder(LocalDateTime.now().plusHours(3));
        
        Session updatedSession = sessionRepository.save(savedSession);
        entityManager.flush();

        // Then
        assertThat(updatedSession.getTitle()).isEqualTo("更新後標題");
        assertThat(updatedSession.getNote()).isEqualTo("更新後備註");
        assertThat(updatedSession.getTask()).isNotNull();
        assertThat(updatedSession.getTask().getId()).isEqualTo(testTask.getId());
        assertThat(updatedSession.getEndReminder()).isNotNull();
    }

    @Test
    @DisplayName("移除 Session 的 Task 關聯")
    void shouldRemoveTaskAssociation() {
        // Given
        Session session = new Session("有任務關聯的工作階段");
        session.setTask(testTask);
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();

        // When
        savedSession.setTask(null);
        sessionRepository.save(savedSession);
        entityManager.flush();
        entityManager.clear();

        // Then
        Session foundSession = sessionRepository.findById(savedSession.getId()).orElseThrow();
        assertThat(foundSession.getTask()).isNull();
    }

    @Test
    @DisplayName("刪除 Session 後資料庫應為空")
    void shouldDeleteSessionAndDatabaseShouldBeEmpty() {
        // Given
        Session session1 = new Session("工作階段1");
        Session session2 = new Session("工作階段2");
        sessionRepository.saveAll(List.of(session1, session2));
        entityManager.flush();

        // When
        sessionRepository.deleteAll();
        entityManager.flush();

        // Then
        List<Session> allSessions = sessionRepository.findAll();
        assertThat(allSessions).isEmpty();
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 標題")
    void shouldValidateNotNullTitle() {
        // Given
        Session session = new Session();
        // title 為 null

        // When & Then
        assertThatThrownBy(() -> {
            sessionRepository.save(session);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證空白標題")
    void shouldValidateBlankTitle() {
        // Given
        Session session = new Session("");

        // When & Then
        assertThatThrownBy(() -> {
            sessionRepository.save(session);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("處理 endReminder 時間欄位")
    void shouldHandleEndReminderTimeField() {
        // Given
        LocalDateTime reminderTime = LocalDateTime.now().plusHours(2);
        Session session = new Session("時間提醒測試");
        session.setEndReminder(reminderTime);

        // When
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        // Then
        Session foundSession = sessionRepository.findById(savedSession.getId()).orElseThrow();
        assertThat(foundSession.getEndReminder()).isNotNull();
        // 由於資料庫精度問題，使用秒級比較
        assertThat(foundSession.getEndReminder().withNano(0))
                .isEqualTo(reminderTime.withNano(0));
    }

    @Test
    @DisplayName("刪除關聯的 Task 不應影響 Session")
    void shouldNotAffectSessionWhenTaskIsDeleted() {
        // Given
        Session session = new Session("關聯任務的工作階段");
        session.setTask(testTask);
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();

        // When - 先將關聯設為 null，再刪除任務
        sessionRepository.setTaskToNullByTaskId(testTask.getId());
        entityManager.flush();
        taskRepository.delete(testTask);
        entityManager.flush();
        entityManager.clear();

        // Then - Session 應該仍然存在，但 task 關聯為 null
        Session foundSession = sessionRepository.findById(savedSession.getId()).orElseThrow();
        assertThat(foundSession.getTitle()).isEqualTo("關聯任務的工作階段");
        assertThat(foundSession.getTask()).isNull();
    }

    @Test
    @DisplayName("處理長文本備註")
    void shouldHandleLongNoteText() {
        // Given
        String longNote = "這是一個很長的備註".repeat(100); // 創建長文本
        Session session = new Session("長備註測試");
        session.setNote(longNote);

        // When
        Session savedSession = sessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        // Then
        Session foundSession = sessionRepository.findById(savedSession.getId()).orElseThrow();
        assertThat(foundSession.getNote()).isEqualTo(longNote);
    }

    @Test
    @DisplayName("刪除單一 Session")
    void shouldDeleteSingleSession() {
        // Given
        Session session1 = new Session("保留的工作階段");
        Session session2 = new Session("要刪除的工作階段");
        sessionRepository.saveAll(List.of(session1, session2));
        entityManager.flush();

        // When
        sessionRepository.delete(session2);
        entityManager.flush();

        // Then
        List<Session> remainingSessions = sessionRepository.findAll();
        assertThat(remainingSessions).hasSize(1);
        assertThat(remainingSessions.get(0).getTitle()).isEqualTo("保留的工作階段");
    }

    @Test
    @DisplayName("根據 taskId 刪除所有關聯的 Session")
    void shouldDeleteSessionsByTaskId() {
        // Given
        Task anotherTask = new Task("另一個任務");
        anotherTask = taskRepository.save(anotherTask);
        
        Session session1 = new Session("關聯 testTask 的工作階段1");
        session1.setTask(testTask);
        
        Session session2 = new Session("關聯 testTask 的工作階段2");
        session2.setTask(testTask);
        
        Session session3 = new Session("關聯 anotherTask 的工作階段");
        session3.setTask(anotherTask);
        
        Session session4 = new Session("無關聯的工作階段");
        // 不設定 task
        
        sessionRepository.saveAll(List.of(session1, session2, session3, session4));
        entityManager.flush();

        // When
        sessionRepository.deleteByTaskId(testTask.getId());
        entityManager.flush();

        // Then
        List<Session> remainingSessions = sessionRepository.findAll();
        assertThat(remainingSessions).hasSize(2);
        assertThat(remainingSessions).extracting(Session::getTitle)
                .containsExactlyInAnyOrder("關聯 anotherTask 的工作階段", "無關聯的工作階段");
    }

    @Test
    @DisplayName("根據不存在的 taskId 刪除 Session - 應無影響")
    void shouldNotDeleteSessionsWhenTaskIdNotExists() {
        // Given
        Session session1 = new Session("工作階段1");
        session1.setTask(testTask);
        
        Session session2 = new Session("工作階段2");
        // 不設定 task
        
        sessionRepository.saveAll(List.of(session1, session2));
        entityManager.flush();

        // When
        sessionRepository.deleteByTaskId(999L); // 不存在的 taskId
        entityManager.flush();

        // Then
        List<Session> allSessions = sessionRepository.findAll();
        assertThat(allSessions).hasSize(2);
        assertThat(allSessions).extracting(Session::getTitle)
                .containsExactlyInAnyOrder("工作階段1", "工作階段2");
    }
} 