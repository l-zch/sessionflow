package com.sessionflow.controller;

import com.sessionflow.dto.SessionRecordCreateRequest;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session", description = "工作階段管理 API")
public class SessionController {
    
    private final SessionService sessionService;
    
    @PostMapping
    @Operation(summary = "建立工作階段", description = "建立新的工作階段")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "工作階段建立成功"),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤")
    })
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionRequest request) {
        
        log.info("Received request to create session: {}", request.getTitle());
        
        SessionResponse response = sessionService.createSession(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "查詢所有工作階段", description = "查詢目前所有存在的工作階段")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        
        log.info("Received request to get all sessions");
        
        List<SessionResponse> responses = sessionService.getAllSessions();
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{id}/end")
    @Operation(summary = "結束工作階段", description = "結束工作階段並建立 SessionRecord")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "工作階段結束成功，SessionRecord 已建立"),
        @ApiResponse(responseCode = "404", description = "工作階段不存在"),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤")
    })
    public ResponseEntity<SessionRecordResponse> endSession(
            @Parameter(description = "工作階段 ID") @PathVariable Long id,
            @Valid @RequestBody SessionRecordCreateRequest request) {
        
        log.info("Received request to end session: {}", id);
        
        SessionRecordResponse response = sessionService.endSession(id, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 