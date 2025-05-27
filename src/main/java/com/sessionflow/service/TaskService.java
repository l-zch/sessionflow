package com.sessionflow.service;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;

import java.util.List;

public interface TaskService {
    
    /**
     * 建立新任務
     */
    TaskResponse createTask(TaskRequest taskRequest);
    
    /**
     * 查詢所有任務，可依狀態篩選
     */
    List<TaskResponse> getAllTasks(String status);
    
    /**
     * 根據 ID 更新任務
     */
    TaskResponse updateTask(Long id, TaskRequest taskRequest);
    
    /**
     * 根據 ID 刪除任務
     */
    void deleteTask(Long id);
    
    /**
     * 標記任務為完成
     */
    TaskResponse completeTask(Long id);
} 