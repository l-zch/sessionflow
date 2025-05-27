package com.sessionflow.repository;

import com.sessionflow.model.SessionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRecordRepository extends JpaRepository<SessionRecord, Long> {
    
    /**
     * 根據開始時間區間查詢 SessionRecord
     */
    @Query("SELECT sr FROM SessionRecord sr WHERE sr.startAt >= :startDate AND sr.startAt < :endDate")
    List<SessionRecord> findByStartAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根據 taskId 查詢所有對應紀錄
     */
    @Query("SELECT sr FROM SessionRecord sr WHERE sr.task.id = :taskId")
    List<SessionRecord> findByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根據開始時間區間和 taskId 查詢 SessionRecord
     */
    @Query("SELECT sr FROM SessionRecord sr WHERE sr.startAt >= :startDate AND sr.startAt < :endDate AND sr.task.id = :taskId")
    List<SessionRecord> findByStartAtBetweenAndTaskId(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate,
                                                     @Param("taskId") Long taskId);
    
    /**
     * 查詢所有 SessionRecord，按 ID 降序排列
     */
    @Query("SELECT sr FROM SessionRecord sr ORDER BY sr.id DESC")
    List<SessionRecord> findAllByOrderByIdDesc();
    
    @Modifying
    @Transactional
    @Query("UPDATE SessionRecord sr SET sr.task = null WHERE sr.task.id = :taskId")
    void setTaskToNullByTaskId(@Param("taskId") Long taskId);
} 