package com.sessionflow.mapper;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.model.Task;

import java.util.List;

public interface TaskMapper {
    
    /**
     * 將 TaskRequest 轉換為 Task 實體
     */
    Task toEntity(TaskRequest taskRequest);
    
    /**
     * 將 Task 實體轉換為 TaskResponse
     */
    TaskResponse toResponse(Task task);
    
    /**
     * 將 Task 實體列表轉換為 TaskResponse 列表
     */
    List<TaskResponse> toResponseList(List<Task> tasks);
    
    /**
     * 使用 TaskRequest 更新現有的 Task 實體
     */
    void updateEntityFromRequest(Task task, TaskRequest taskRequest);
} 