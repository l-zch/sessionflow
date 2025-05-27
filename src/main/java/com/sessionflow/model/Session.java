package com.sessionflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "task")
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @NotBlank(message = "Session title cannot be blank")
    @Column(nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
    
    @Column(name = "end_reminder")
    private LocalDateTime endReminder;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    // Custom constructor
    public Session(String title) {
        this.title = title;
    }
} 