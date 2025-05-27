package com.sessionflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Session Model 測試")
class SessionTest {

    @Test
    @DisplayName("使用標題建立 Session")
    void shouldCreateSessionWithTitle() {
        // Given
        String title = "測試工作階段";

        // When
        Session session = new Session(title);

        // Then
        assertThat(session.getTitle()).isEqualTo(title);
        assertThat(session.getId()).isNull();
        assertThat(session.getNote()).isNull();
        assertThat(session.getTask()).isNull();
        assertThat(session.getEndReminder()).isNull();
    }

    @Test
    @DisplayName("使用預設建構子建立 Session")
    void shouldCreateSessionWithDefaultConstructor() {
        // When
        Session session = new Session();

        // Then
        assertThat(session.getId()).isNull();
        assertThat(session.getTitle()).isNull();
        assertThat(session.getNote()).isNull();
        assertThat(session.getTask()).isNull();
        assertThat(session.getEndReminder()).isNull();
    }

    @Test
    @DisplayName("設定和取得 Session 屬性")
    void shouldSetAndGetSessionProperties() {
        // Given
        Session session = new Session();
        String title = "工作階段標題";
        String note = "工作階段備註";
        LocalDateTime endReminder = LocalDateTime.now().plusHours(2);
        Task task = new Task("關聯任務");

        // When
        session.setTitle(title);
        session.setNote(note);
        session.setEndReminder(endReminder);
        session.setTask(task);

        // Then
        assertThat(session.getTitle()).isEqualTo(title);
        assertThat(session.getNote()).isEqualTo(note);
        assertThat(session.getEndReminder()).isEqualTo(endReminder);
        assertThat(session.getTask()).isEqualTo(task);
    }

    @Test
    @DisplayName("Session 的 equals 和 hashCode")
    void shouldImplementEqualsAndHashCode() {
        // Given
        Session session1 = new Session("測試工作階段");
        session1.setId(1L);
        
        Session session2 = new Session("測試工作階段");
        session2.setId(1L);
        
        Session session3 = new Session("不同工作階段");
        session3.setId(2L);

        // Then
        assertThat(session1).isEqualTo(session2);
        assertThat(session1).isNotEqualTo(session3);
        assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
    }

    @Test
    @DisplayName("Session 的 toString")
    void shouldImplementToString() {
        // Given
        Session session = new Session("測試工作階段");
        session.setId(1L);
        session.setNote("測試備註");

        // When
        String toString = session.toString();

        // Then
        assertThat(toString).contains("Session");
        assertThat(toString).contains("測試工作階段");
    }

    @Test
    @DisplayName("Session 可以設定空的備註")
    void shouldAllowNullNote() {
        // Given
        Session session = new Session("測試工作階段");

        // When
        session.setNote(null);

        // Then
        assertThat(session.getNote()).isNull();
    }

    @Test
    @DisplayName("Session 可以設定空的結束提醒")
    void shouldAllowNullEndReminder() {
        // Given
        Session session = new Session("測試工作階段");

        // When
        session.setEndReminder(null);

        // Then
        assertThat(session.getEndReminder()).isNull();
    }

    @Test
    @DisplayName("Session 可以不關聯任務")
    void shouldAllowNullTask() {
        // Given
        Session session = new Session("測試工作階段");

        // When
        session.setTask(null);

        // Then
        assertThat(session.getTask()).isNull();
    }

    @Test
    @DisplayName("Session 可以關聯和取消關聯任務")
    void shouldAssociateAndDisassociateTask() {
        // Given
        Session session = new Session("測試工作階段");
        Task task = new Task("測試任務");

        // When - 關聯任務
        session.setTask(task);

        // Then
        assertThat(session.getTask()).isEqualTo(task);

        // When - 取消關聯
        session.setTask(null);

        // Then
        assertThat(session.getTask()).isNull();
    }

    @Test
    @DisplayName("Session 可以設定未來的結束提醒時間")
    void shouldAllowFutureEndReminder() {
        // Given
        Session session = new Session("測試工作階段");
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        // When
        session.setEndReminder(futureTime);

        // Then
        assertThat(session.getEndReminder()).isEqualTo(futureTime);
        assertThat(session.getEndReminder()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Session 可以設定過去的結束提醒時間")
    void shouldAllowPastEndReminder() {
        // Given
        Session session = new Session("測試工作階段");
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        // When
        session.setEndReminder(pastTime);

        // Then
        assertThat(session.getEndReminder()).isEqualTo(pastTime);
        assertThat(session.getEndReminder()).isBefore(LocalDateTime.now());
    }
} 