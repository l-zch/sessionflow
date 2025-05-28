package com.sessionflow.service.impl;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.exception.TaskNotFoundException;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import com.sessionflow.repository.TaskRepository;
import com.sessionflow.repository.SessionRepository;
import com.sessionflow.repository.SessionRecordRepository;
import com.sessionflow.repository.ScheduleEntryRepository;
import com.sessionflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SessionRepository sessionRepository;
    private final SessionRecordRepository sessionRecordRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        log.info("Creating new task with title: {}", taskRequest.getTitle());

        Task task = taskMapper.toEntity(taskRequest);
        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());
        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(String status) {
        log.info("Fetching all tasks with status filter: {}", status);

        List<Task> tasks;

        if (status == null || status.trim().isEmpty()) {
            tasks = taskRepository.findAllOrderByCreatedAtDesc();
        } else {
            TaskStatus taskStatus = parseTaskStatus(status);
            tasks = taskRepository.findByStatusOrderByCreatedAtDesc(taskStatus);
        }

        log.info("Found {} tasks", tasks.size());
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest taskRequest) {
        log.info("Updating task with id: {}", id);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskMapper.updateEntityFromRequest(existingTask, taskRequest);
        Task updatedTask = taskRepository.save(existingTask);

        log.info("Task updated successfully with id: {}", updatedTask.getId());
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task and all related entities with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        // 1. 刪除所有關聯的 SessionRecord
        log.debug("Deleting related session records");
        sessionRecordRepository.deleteByTaskId(id);

        // 2. 刪除所有關聯的 Session
        log.debug("Deleting related sessions");
        sessionRepository.deleteByTaskId(id);

        // 3. 刪除所有關聯的 ScheduleEntry
        log.debug("Deleting related schedule entries");
        scheduleEntryRepository.deleteByTaskId(id);

        // 4. 最後刪除任務本身
        log.debug("Deleting task");
        taskRepository.deleteById(id);

        log.info("Task and all related entities deleted successfully with id: {}", id);
    }

    @Override
    public TaskResponse completeTask(Long id) {
        log.info("Completing task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.markAsComplete();
        Task completedTask = taskRepository.save(task);

        log.info("Task completed successfully with id: {}", completedTask.getId());
        return taskMapper.toResponse(completedTask);
    }

    @Override
    public TaskResponse reopenTask(Long id) {
        log.info("Marking task as pending with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.markAsPending();
        Task pendingTask = taskRepository.save(task);

        log.info("Task marked as pending successfully with id: {}", pendingTask.getId());
        return taskMapper.toResponse(pendingTask);
    }

    private TaskStatus parseTaskStatus(String status) {
        try {
            return switch (status.toLowerCase()) {
                case "pending" -> TaskStatus.PENDING;
                case "complete" -> TaskStatus.COMPLETE;
                default -> throw new IllegalArgumentException("Invalid task status: " + status);
            };
        } catch (Exception e) {
            log.warn("Invalid task status provided: {}", status);
            throw new IllegalArgumentException(
                    "Invalid task status: " + status + ". Valid values are: PENDING, COMPLETE");
        }
    }
}