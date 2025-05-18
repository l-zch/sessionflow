package com.sessionflow.service;

import com.sessionflow.dto.TaskDto;
import com.sessionflow.model.Task;

import java.util.List;

public interface TaskService {

    /**
     * Create a new task
     */
    TaskDto createTask(TaskDto taskDto);

    /**
     * Get all tasks, optionally filtered by status and parentId
     */
    List<TaskDto> getAllTasks(Task.TaskStatus status, Long parentId);

    /**
     * Get a single task by ID with its children
     */
    TaskDto getTaskById(Long id);

    /**
     * Update an existing task
     */
    TaskDto updateTask(Long id, TaskDto taskDto);

    /**
     * Delete a task and its children
     */
    void deleteTask(Long id);
}