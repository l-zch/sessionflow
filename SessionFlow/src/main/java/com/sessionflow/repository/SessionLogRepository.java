package com.sessionflow.repository;

import com.sessionflow.model.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    
    List<SessionLog> findBySessionId(Long sessionId);
    
    @Query("SELECT sl FROM SessionLog sl WHERE sl.session.id = :sessionId ORDER BY sl.start ASC")
    List<SessionLog> findBySessionIdOrderByStartAsc(@Param("sessionId") Long sessionId);
    
    @Query("SELECT sl FROM SessionLog sl WHERE sl.session.task.id = :taskId")
    List<SessionLog> findByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT COALESCE(SUM(sl.duration), 0) FROM SessionLog sl WHERE sl.session.id = :sessionId")
    Long getTotalDurationBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT COALESCE(SUM(sl.duration), 0) FROM SessionLog sl WHERE sl.session.task.id = :taskId")
    Long getTotalDurationByTaskId(@Param("taskId") Long taskId);
} 