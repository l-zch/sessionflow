package com.sessionflow.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.exception.TaskNotFoundException;
import com.sessionflow.mapper.TaskMapper;
import com.sessionflow.model.Task;
import com.sessionflow.model.TaskStatus;
import com.sessionflow.repository.TaskRepository;
import com.sessionflow.service.TaskService;
import com.sessionflow.service.SessionService;
import com.sessionflow.service.SessionRecordService;
import com.sessionflow.service.ScheduleEntryService;
import com.sessionflow.event.ResourceChangedEvent;
import com.sessionflow.common.NotificationType;
import com.sessionflow.dto.ResourceChangedNotification.Affected;
import com.sessionflow.model.Tag;
import com.sessionflow.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionService sessionService;
    private final SessionRecordService sessionRecordService;
    private final ScheduleEntryService scheduleEntryService;

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        log.info("Creating new task with title: {}", taskRequest.getTitle());

        Task task = taskMapper.toEntity(taskRequest);
        Task savedTask = taskRepository.save(task);
        TaskResponse response = taskMapper.toResponse(savedTask);

        // 發布任務建立事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.TASK_CREATE,
            savedTask.getId(),
            null,
            response,
            null
        ));

        log.info("Task created successfully with id: {}", savedTask.getId());
        return response;
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

        // 將更新邏輯移至 Service 層
        existingTask.setTitle(taskRequest.getTitle());
        existingTask.setDueAt(taskRequest.getDueAt());
        existingTask.setNote(taskRequest.getNote());

        // 更新標籤關聯
        if (taskRequest.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            if (!taskRequest.getTagIds().isEmpty()) {
                tagRepository.findAllById(taskRequest.getTagIds()).forEach(tags::add);
            }
            existingTask.setTags(tags);
        }

        Task updatedTask = taskRepository.save(existingTask);
        TaskResponse response = taskMapper.toResponse(updatedTask);

        // 發布任務更新事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.TASK_UPDATE,
            updatedTask.getId(),
            null,
            response,
            null
        ));

        log.info("Task updated successfully with id: {}", updatedTask.getId());
        return response;
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        // 收集級聯刪除的影響範圍
        List<Long> sessionIds = sessionService.findIdsByTaskId(id);
        List<Long> sessionRecordIds = sessionRecordService.findIdsByTaskId(id);
        List<Long> scheduleEntryIds = scheduleEntryService.findIdsByTaskId(id);

        // 1. 刪除所有關聯的 SessionRecord
        log.debug("Deleting related session records");
        sessionRecordService.deleteByTaskId(id);

        // 2. 刪除所有關聯的 Session
        log.debug("Deleting related sessions");
        sessionService.deleteByTaskId(id);

        // 3. 刪除所有關聯的 ScheduleEntry
        log.debug("Deleting related schedule entries");
        scheduleEntryService.deleteByTaskId(id);
        
        // 4. 最後刪除任務本身
        log.debug("Deleting task");
        taskRepository.deleteById(id);

        // 建立 affected 列表
        List<Affected> affected = new ArrayList<>();
        affected.add(new Affected(NotificationType.SESSION_DELETE, sessionIds));
        affected.add(new Affected(NotificationType.SESSION_RECORD_DELETE, sessionRecordIds));
        affected.add(new Affected(NotificationType.SCHEDULE_ENTRY_DELETE, scheduleEntryIds));
        // 發布任務刪除事件（包含級聯影響）
        eventPublisher.publishEvent(new ResourceChangedEvent<TaskResponse>(
            NotificationType.TASK_DELETE,
            id,
            null,
            null,
            affected
        ));
        
        log.info("Task and all related entities deleted successfully with id: {}", id);
    }

    @Override
    public TaskResponse completeTask(Long id) {
        log.info("Completing task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.markAsComplete();
        Task completedTask = taskRepository.save(task);
        TaskResponse response = taskMapper.toResponse(completedTask);

        // 發布任務完成事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.TASK_UPDATE,
            completedTask.getId(),
            null,
            response,
            null
        ));

        log.info("Task completed successfully with id: {}", completedTask.getId());
        return response;
    }

    @Override
    public TaskResponse reopenTask(Long id) {
        log.info("Marking task as pending with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.markAsPending();
        Task pendingTask = taskRepository.save(task);
        TaskResponse response = taskMapper.toResponse(pendingTask);

        // 發布任務重新開啟事件
        eventPublisher.publishEvent(new ResourceChangedEvent<>(
            NotificationType.TASK_UPDATE,
            pendingTask.getId(),
            null,
            response,
            null
        ));

        log.info("Task marked as pending successfully with id: {}", pendingTask.getId());
        return response;
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