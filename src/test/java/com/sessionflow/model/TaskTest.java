package com.sessionflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Model Tests")
class TaskTest {

    private Validator validator;
    private Task task;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        task = new Task();
    }

    @Test
    @DisplayName("Should create task with title")
    void shouldCreateTaskWithTitle() {
        // Given
        String title = "完成專案文件";
        
        // When
        Task task = new Task(title);
        
        // Then
        assertEquals(title, task.getTitle());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNull(task.getCompletedAt());
        assertFalse(task.isCompleted());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    @DisplayName("Should validate title is not blank")
    void shouldValidateTitleIsNotBlank() {
        // Given
        task.setTitle("");
        
        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Task title cannot be blank")));
    }

    @Test
    @DisplayName("Should validate title length")
    void shouldValidateTitleLength() {
        // Given
        String longTitle = "a".repeat(256);
        task.setTitle(longTitle);
        
        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Task title cannot exceed 255 characters")));
    }

    @Test
    @DisplayName("Should validate note length")
    void shouldValidateNoteLength() {
        // Given
        String longNote = "a".repeat(2001);
        task.setTitle("Valid Title");
        task.setNote(longNote);
        
        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Task note cannot exceed 2000 characters")));
    }

    @Test
    @DisplayName("Should complete task successfully")
    void shouldCompleteTaskSuccessfully() {
        // Given
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.PENDING);
        LocalDateTime before = LocalDateTime.now();
        
        // When
        task.markAsComplete();
        
        // Then
        assertEquals(TaskStatus.COMPLETE, task.getStatus());
        assertTrue(task.isCompleted());
        assertNotNull(task.getCompletedAt());
        assertTrue(task.getCompletedAt().isAfter(before));
    }

    @Test
    @DisplayName("Should mark task as pending")
    void shouldMarkTaskAsPending() {
        // Given
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.COMPLETE);
        LocalDateTime before = LocalDateTime.now();

        // When
        task.markAsPending();
        
        // Then
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNull(task.getCompletedAt());
        assertFalse(task.isCompleted());
    }

        
    @Test
    @DisplayName("Should handle tags correctly")
    void shouldHandleTagsCorrectly() {
        // Given
        task.setTitle("Test Task");
        Tag tag1 = new Tag("工作", "#FF5733");
        tag1.setId(1L); // Set ID for proper equals comparison
        Tag tag2 = new Tag("重要", "#33FF57");
        tag2.setId(2L); // Set ID for proper equals comparison
        
        // When
        task.getTags().add(tag1);
        task.getTags().add(tag2);
        
        // Then
        assertEquals(2, task.getTags().size());
        assertTrue(task.getTags().contains(tag1));
        assertTrue(task.getTags().contains(tag2));
    }

    @Test
    @DisplayName("Should set timestamps on create")
    void shouldSetTimestampsOnCreate() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        task.setTitle("Test Task");
        
        // When
        task.onCreate();
        
        // Then
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertTrue(task.getCreatedAt().isAfter(before));
        assertTrue(task.getUpdatedAt().isAfter(before));
        // Use a small tolerance for timestamp comparison
        assertTrue(Math.abs(Duration.between(task.getCreatedAt(), task.getUpdatedAt()).toNanos()) < 1_000_000); // 1ms tolerance
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        task.setTitle("Test Task");
        task.onCreate();
        LocalDateTime originalCreatedAt = task.getCreatedAt();
        LocalDateTime originalUpdatedAt = task.getUpdatedAt();
        
        // Wait a bit to ensure different timestamp
        Thread.sleep(10);
        
        // When
        task.onUpdate();
        
        // Then
        assertEquals(originalCreatedAt, task.getCreatedAt()); // createdAt should not change
        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should handle null note")
    void shouldHandleNullNote() {
        // Given
        task.setTitle("Test Task");
        task.setNote(null);
        
        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        
        // Then
        assertTrue(violations.isEmpty()); // null note should be allowed
        assertNull(task.getNote());
    }

    @Test
    @DisplayName("Should handle empty tags set")
    void shouldHandleEmptyTagsSet() {
        // Given
        task.setTitle("Test Task");
        
        // When & Then
        assertNotNull(task.getTags());
        assertTrue(task.getTags().isEmpty());
        assertEquals(0, task.getTags().size());
    }

    @Test
    @DisplayName("Should handle due time correctly")
    void shouldHandleDueTimeCorrectly() {
        // Given
        task.setTitle("Test Task");
        LocalDateTime dueTime = LocalDateTime.now().plusDays(1);
        
        // When
        task.setDueTime(dueTime);
        
        // Then
        assertEquals(dueTime, task.getDueTime());
    }

    @Test
    @DisplayName("Should handle null due time")
    void shouldHandleNullDueTime() {
        // Given
        task.setTitle("Test Task");
        task.setDueTime(null);
        
        // When
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        
        // Then
        assertTrue(violations.isEmpty()); // null due time should be allowed
        assertNull(task.getDueTime());
    }
} 