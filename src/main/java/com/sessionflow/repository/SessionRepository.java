package com.sessionflow.repository;

import com.sessionflow.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    List<Session> findByDate(LocalDate date);
    
    List<Session> findByTaskId(Long taskId);
    
    @Query("SELECT s FROM Session s WHERE s.date = :date ORDER BY s.startTime ASC")
    List<Session> findByDateOrderByStartTime(@Param("date") LocalDate date);
    
    @Query("SELECT s FROM Session s JOIN FETCH s.logs WHERE s.id = :id")
    Session findByIdWithLogs(@Param("id") Long id);
} 