package com.sessionflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_entries")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "task")
public class ScheduleEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @NotBlank(message = "排程標題不能為空")
    @Column(nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    @NotNull(message = "開始時間不能為空")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull(message = "結束時間不能為空")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    // Custom constructor
    public ScheduleEntry(String title, LocalDateTime startAt, LocalDateTime endAt) {
        validateTimeRange(startAt, endAt);
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
    }
    
    // Override setters with validation
    public void setStartAt(LocalDateTime startAt) {
        validateTimeRange(startAt, endAt);
        this.startAt = startAt;
    }
    
    public void setEndAt(LocalDateTime endAt) {
        validateTimeRange(startAt, endAt);
        this.endAt = endAt;
    }
    
    // Lifecycle methods
    @PrePersist
    @PreUpdate
    protected void validateTimeRange() {
        validateTimeRange(startAt, endAt);
    }
    
    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
    }
    
    public Duration getDuration() {
        if (startAt == null || endAt == null) return Duration.ZERO;
        return Duration.between(startAt, endAt);
    }
    
    public long getDurationMinutes() {
        return getDuration().toMinutes();
    }
    
    public boolean isOverlapping(ScheduleEntry other) {
        if (other == null || startAt == null || endAt == null || 
            other.startAt == null || other.endAt == null) {
            return false;
        }
        return startAt.isBefore(other.endAt) && endAt.isAfter(other.startAt);
    }
} 