package com.sessionflow.repository;

import com.sessionflow.model.Tag;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TaskRepository 整合測試")
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tag workTag;
    private Tag urgentTag;

    @BeforeEach
    void setUp() {
        // 創建測試用的標籤
        workTag = new Tag("工作", "#FF5733");
        urgentTag = new Tag("緊急", "#FF0000");
        
        tagRepository.save(workTag);
        tagRepository.save(urgentTag);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("成功建立 Task")
    void shouldCreateTaskSuccessfully() {
        // Given
        Task task = new Task("完成專案文件");
        task.setNote("需要包含技術規格");
        task.setDueTime(LocalDateTime.now().plusDays(7));
        
        // 重新查詢 Tag 以避免分離實體問題
        Tag workTagFromDb = tagRepository.findById(workTag.getId()).orElseThrow();
        task.getTags().add(workTagFromDb);

        // When
        Task savedTask = taskRepository.save(task);
        entityManager.flush();

        // Then
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("完成專案文件");
        assertThat(savedTask.getNote()).isEqualTo("需要包含技術規格");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();
        assertThat(savedTask.getCompletedAt()).isNull();
        assertThat(savedTask.getTags()).hasSize(1);
        assertThat(savedTask.getTags()).extracting(Tag::getName).contains("工作");
    }

    @Test
    @DisplayName("查詢單筆 Task")
    void shouldFindTaskById() {
        // Given
        Task task = new Task("測試任務");
        
        // 重新查詢 Tag 以避免分離實體問題
        Tag workTagFromDb = tagRepository.findById(workTag.getId()).orElseThrow();
        Tag urgentTagFromDb = tagRepository.findById(urgentTag.getId()).orElseThrow();
        task.getTags().addAll(Set.of(workTagFromDb, urgentTagFromDb));
        
        Task savedTask = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        // Then
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("測試任務");
        assertThat(foundTask.get().getTags()).hasSize(2);
    }

    @Test
    @DisplayName("查詢全部 Task")
    void shouldFindAllTasks() {
        // Given
        Task task1 = new Task("任務1");
        Task task2 = new Task("任務2");
        task2.setStatus(TaskStatus.COMPLETE);
        task2.setCompletedAt(LocalDateTime.now());
        
        taskRepository.saveAll(List.of(task1, task2));
        entityManager.flush();

        // When
        List<Task> allTasks = taskRepository.findAll();

        // Then
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("任務1", "任務2");
    }

    @Test
    @DisplayName("根據狀態查詢 Task")
    void shouldFindTasksByStatus() {
        // Given
        Task pendingTask = new Task("待辦任務");
        Task completedTask = new Task("已完成任務");
        completedTask.markAsComplete();
        
        taskRepository.saveAll(List.of(pendingTask, completedTask));
        entityManager.flush();

        // When
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        List<Task> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETE);

        // Then
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("待辦任務");
        
        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getTitle()).isEqualTo("已完成任務");
        assertThat(completedTasks.get(0).getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("更新 Task 欄位")
    void shouldUpdateTaskFields() {
        // Given
        Task task = new Task("原始標題");
        task.setNote("原始備註");
        Task savedTask = taskRepository.save(task);
        entityManager.flush();
        
        LocalDateTime originalUpdatedAt = savedTask.getUpdatedAt();
        
        // 等待一毫秒確保時間戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        savedTask.setTitle("更新後標題");
        savedTask.setNote("更新後備註");
        savedTask.setDueTime(LocalDateTime.now().plusDays(3));
        savedTask.getTags().add(urgentTag);
        
        Task updatedTask = taskRepository.save(savedTask);
        entityManager.flush();

        // Then
        assertThat(updatedTask.getTitle()).isEqualTo("更新後標題");
        assertThat(updatedTask.getNote()).isEqualTo("更新後備註");
        assertThat(updatedTask.getDueTime()).isNotNull();
        assertThat(updatedTask.getTags()).contains(urgentTag);
        assertThat(updatedTask.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedTask.getCreatedAt()).isEqualTo(savedTask.getCreatedAt());
    }

    @Test
    @DisplayName("完成 Task")
    void shouldCompleteTask() {
        // Given
        Task task = new Task("待完成任務");
        Task savedTask = taskRepository.save(task);
        entityManager.flush();

        // When
        savedTask.markAsComplete();
        Task completedTask = taskRepository.save(savedTask);
        entityManager.flush();

        // Then
        assertThat(completedTask.getStatus()).isEqualTo(TaskStatus.COMPLETE);
        assertThat(completedTask.getCompletedAt()).isNotNull();
        assertThat(completedTask.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("刪除 Task 後資料庫應為空")
    void shouldDeleteTaskAndDatabaseShouldBeEmpty() {
        // Given
        Task task1 = new Task("任務1");
        Task task2 = new Task("任務2");
        taskRepository.saveAll(List.of(task1, task2));
        entityManager.flush();

        // When
        taskRepository.deleteAll();
        entityManager.flush();

        // Then
        List<Task> allTasks = taskRepository.findAll();
        assertThat(allTasks).isEmpty();
    }

    @Test
    @DisplayName("處理關聯標籤")
    void shouldHandleTagAssociations() {
        // Given
        Task task = new Task("有標籤的任務");
        
        // 重新查詢 Tag 以避免分離實體問題
        Tag workTagFromDb = tagRepository.findById(workTag.getId()).orElseThrow();
        Tag urgentTagFromDb = tagRepository.findById(urgentTag.getId()).orElseThrow();
        task.getTags().addAll(Set.of(workTagFromDb, urgentTagFromDb));
        
        // When
        Task savedTask = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        // Then
        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(foundTask.getTags()).hasSize(2);
        assertThat(foundTask.getTags()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("工作", "緊急");
    }

    @Test
    @DisplayName("移除標籤關聯")
    void shouldRemoveTagAssociations() {
        // Given
        Task task = new Task("有標籤的任務");
        
        // 重新查詢 Tag 以避免分離實體問題
        Tag workTagFromDb = tagRepository.findById(workTag.getId()).orElseThrow();
        Tag urgentTagFromDb = tagRepository.findById(urgentTag.getId()).orElseThrow();
        task.getTags().addAll(Set.of(workTagFromDb, urgentTagFromDb));
        
        Task savedTask = taskRepository.save(task);
        entityManager.flush();

        // When
        savedTask.getTags().clear();
        taskRepository.save(savedTask);
        entityManager.flush();
        entityManager.clear();

        // Then
        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(foundTask.getTags()).isEmpty();
    }

    @Test
    @DisplayName("驗證 not-null 欄位")
    void shouldValidateNotNullFields() {
        // Given
        Task task = new Task();
        // title 為 null

        // When & Then
        assertThatThrownBy(() -> {
            taskRepository.save(task);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證標題長度限制")
    void shouldValidateTitleLength() {
        // Given
        String longTitle = "a".repeat(256); // 超過 255 字元限制
        Task task = new Task(longTitle);

        // When & Then
        assertThatThrownBy(() -> {
            taskRepository.save(task);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("時間欄位檢查 - createdAt 和 updatedAt 自動設定")
    void shouldAutoSetTimestamps() {
        // Given
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        
        // When
        Task task = new Task("時間測試任務");
        Task savedTask = taskRepository.save(task);
        entityManager.flush();
        
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();
        assertThat(savedTask.getCreatedAt()).isAfter(beforeCreate);
        assertThat(savedTask.getCreatedAt()).isBefore(afterCreate);
        assertThat(savedTask.getUpdatedAt()).isAfter(beforeCreate);
        assertThat(savedTask.getUpdatedAt()).isBefore(afterCreate);
    }

    @Test
    @DisplayName("按創建時間降序查詢")
    void shouldFindAllOrderByCreatedAtDesc() {
        // Given
        Task task1 = new Task("第一個任務");
        taskRepository.save(task1);
        entityManager.flush();
        
        // 等待確保時間差異
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Task task2 = new Task("第二個任務");
        taskRepository.save(task2);
        entityManager.flush();

        // When
        List<Task> tasks = taskRepository.findAllOrderByCreatedAtDesc();

        // Then
        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).getTitle()).isEqualTo("第二個任務");
        assertThat(tasks.get(1).getTitle()).isEqualTo("第一個任務");
    }

    @Test
    @DisplayName("根據狀態按創建時間降序查詢")
    void shouldFindByStatusOrderByCreatedAtDesc() {
        // Given
        Task pendingTask1 = new Task("待辦任務1");
        Task pendingTask2 = new Task("待辦任務2");
        Task completedTask = new Task("已完成任務");
        completedTask.markAsComplete();
        
        taskRepository.saveAll(List.of(pendingTask1, completedTask, pendingTask2));
        entityManager.flush();

        // When
        List<Task> pendingTasks = taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PENDING);

        // Then
        assertThat(pendingTasks).hasSize(2);
        assertThat(pendingTasks).extracting(Task::getTitle)
                .containsExactly("待辦任務2", "待辦任務1");
    }
} 