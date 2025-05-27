package com.sessionflow.repository;

import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * 根據狀態查詢任務
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * 查詢所有任務並按創建時間降序排列
     */
    @Query("SELECT t FROM Task t ORDER BY t.createdAt DESC")
    List<Task> findAllOrderByCreatedAtDesc();
    
    /**
     * 根據狀態查詢任務並按創建時間降序排列
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Task> findByStatusOrderByCreatedAtDesc(@Param("status") TaskStatus status);
} 