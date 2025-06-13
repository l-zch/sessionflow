package com.sessionflow.repository;

import com.sessionflow.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    
    /**
     * 查詢指定時間區間內的所有排程
     * 排程的開始時間在指定區間內，或排程與指定區間有重疊
     */
    @Query("SELECT s FROM ScheduleEntry s WHERE " +
           "(s.startAt >= :startDateTime AND s.startAt < :endDateTime) OR " +
           "(s.endAt > :startDateTime AND s.endAt <= :endDateTime) OR " +
           "(s.startAt < :startDateTime AND s.endAt > :endDateTime) " +
           "ORDER BY s.startAt ASC")
    List<ScheduleEntry> findByDateRange(@Param("startDateTime") LocalDateTime startDateTime, 
                                       @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * 查詢指定時間區間內的所有排程（簡單版本，只檢查開始時間）
     */
    List<ScheduleEntry> findByStartAtBetweenOrderByStartAtAsc(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * 根據任務 ID 查詢所有相關的排程項目
     */
    @Query("SELECT se FROM ScheduleEntry se WHERE se.task.id = :taskId")
    List<ScheduleEntry> findByTaskId(@Param("taskId") Long taskId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ScheduleEntry se SET se.task = null WHERE se.task.id = :taskId")
    void setTaskToNullByTaskId(@Param("taskId") Long taskId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntry se WHERE se.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
} 