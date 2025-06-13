package com.sessionflow.repository;

import com.sessionflow.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    /**
     * 查詢所有工作階段，按建立時間降序排列
     */
    List<Session> findAllByOrderByIdDesc();

    /**
     * 根據任務 ID 查詢所有相關的工作階段
     */
    @Query("SELECT s FROM Session s WHERE s.task.id = :taskId")
    List<Session> findByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Transactional
    @Query("UPDATE Session s SET s.task = null WHERE s.task.id = :taskId")
    void setTaskToNullByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
} 