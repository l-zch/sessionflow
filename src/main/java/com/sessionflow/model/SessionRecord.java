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
@Table(name = "session_records")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "task")
public class SessionRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @NotBlank(message = "Session record title cannot be blank")
    @Column(nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    @NotNull(message = "Start time cannot be null")
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @NotNull(message = "End time cannot be null")
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
    @Column(name = "planned_note", columnDefinition = "TEXT")
    private String plannedNote;
    
    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Custom constructor
    public SessionRecord(String title, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
    }
    
    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean isCompleted() {
        return endAt != null;
    }
    
    public Duration getDuration() {
        if (startAt == null) return Duration.ZERO;
        LocalDateTime end = endAt != null ? endAt : LocalDateTime.now();
        return Duration.between(startAt, end);
    }
    
    public long getDurationMinutes() {
        return getDuration().toMinutes();
    }
} 