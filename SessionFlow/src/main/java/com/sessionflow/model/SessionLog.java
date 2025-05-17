package com.sessionflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public Session session;

    @Column(nullable = false)
    public LocalDateTime start;

    public LocalDateTime end;

    public Long duration; // in seconds

    @Column(columnDefinition = "TEXT")
    public String note;

    @PrePersist
    protected void onCreate() {
        // If start is not provided, set it to current time
        if (this.start == null) {
            this.start = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update duration if end is set
        if (this.end != null && this.start != null) {
            this.duration = Duration.between(this.start, this.end).getSeconds();
        }
    }
}