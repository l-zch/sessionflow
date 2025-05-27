package com.sessionflow.controller;

import com.sessionflow.dto.TaskRequest;
import com.sessionflow.dto.TaskResponse;
import com.sessionflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task", description = "任務管理 API")
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    @Operation(summary = "建立任務", description = "建立新的任務")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "任務建立成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數驗證失敗"),
        @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest taskRequest) {
        log.info("Received request to create task: {}", taskRequest.getTitle());
        
        TaskResponse response = taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "查詢所有任務", description = "查詢所有任務，可依狀態篩選")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "無效的狀態參數"),
        @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @Parameter(description = "任務狀態篩選 (PENDING/COMPLETE)", example = "PENDING")
            @RequestParam(required = false) String status) {
        log.info("Received request to get all tasks with status: {}", status);
        
        List<TaskResponse> responses = taskService.getAllTasks(status);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新任務", description = "根據 ID 更新任務")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "任務更新成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數驗證失敗"),
        @ApiResponse(responseCode = "404", description = "任務不存在"),
        @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "任務 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest taskRequest) {
        log.info("Received request to update task with id: {}", id);
        
        TaskResponse response = taskService.updateTask(id, taskRequest);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "標記任務為完成", description = "標記指定任務為完成狀態")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "任務標記完成成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "404", description = "任務不存在"),
        @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public ResponseEntity<TaskResponse> completeTask(
            @Parameter(description = "任務 ID", example = "1")
            @PathVariable Long id) {
        log.info("Received request to complete task with id: {}", id);
        
        TaskResponse response = taskService.completeTask(id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除任務", description = "根據 ID 刪除任務")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "任務刪除成功"),
        @ApiResponse(responseCode = "404", description = "任務不存在"),
        @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "任務 ID", example = "1")
            @PathVariable Long id) {
        log.info("Received request to delete task with id: {}", id);
        
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
} 