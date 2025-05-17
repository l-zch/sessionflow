package com.sessionflow.service.impl;

import com.sessionflow.dto.TaskDto;
import com.sessionflow.exception.ResourceNotFoundException;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.model.Task;
import com.sessionflow.repository.TaskRepository;
import com.sessionflow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        Task task = taskMapper.toEntity(taskDto);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasks(Task.TaskStatus status, Long parentId) {
        List<Task> tasks;

        if (status != null && parentId != null) {
            // Filter by both status and parentId
            tasks = taskRepository.findAll().stream()
                    .filter(t -> t.status == status && (t.parent != null && t.parent.id.equals(parentId)))
                    .toList();
        } else if (status != null) {
            // Filter by status only
            tasks = taskRepository.findByStatus(status);
        } else if (parentId != null) {
            // Filter by parentId only
            tasks = taskRepository.findByParentId(parentId);
        } else {
            // Get root tasks (parent is null)
            tasks = taskRepository.findByParentIsNull();
        }

        return taskMapper.toDtoList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findByIdWithChildren(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found with ID: " + id);
        }
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        // Update the fields
        existingTask.title = taskDto.title();
        if (taskDto.description() != null) {
            existingTask.description = taskDto.description();
        }
        if (taskDto.estimatedDuration() != null) {
            existingTask.estimatedDuration = taskDto.estimatedDuration();
        }
        if (taskDto.status() != null) {
            existingTask.status = taskDto.status();
        }

        // Handle parent relationship if changed
        if (taskDto.parentId() != null &&
                (existingTask.parent == null || !existingTask.parent.id.equals(taskDto.parentId()))) {
            Task parent = taskRepository.findById(taskDto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent task not found with ID: " + taskDto.parentId()));
            existingTask.parent = parent;
        } else if (taskDto.parentId() == null && existingTask.parent != null) {
            // Remove parent relationship
            existingTask.parent = null;
        }

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        taskRepository.delete(task);
    }
}