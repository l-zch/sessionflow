package com.sessionflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration; // in seconds

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    protected void onCreate() {
        // If start is not provided, set it to current time
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update duration if end is set
        if (this.endTime != null && this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime).getSeconds();
        }
    }
}