package com.sessionflow.repository;

import com.sessionflow.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(Task.TaskStatus status);
    
    List<Task> findByParentId(Long parentId);
    
    List<Task> findByParentIsNull();
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.children WHERE t.id = :id")
    Task findByIdWithChildren(@Param("id") Long id);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.tags WHERE t.id = :id")
    Task findByIdWithTags(@Param("id") Long id);
} 