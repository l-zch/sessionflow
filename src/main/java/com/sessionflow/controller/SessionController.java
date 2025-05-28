package com.sessionflow.controller;

import com.sessionflow.config.ApiResponseTemplates;
import com.sessionflow.dto.SessionRecordCreateRequest;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRequest;
import com.sessionflow.dto.SessionResponse;
import com.sessionflow.exception.ErrorResponse;
import com.sessionflow.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session", description = "工作階段管理 API")
public class SessionController {
    
    private final SessionService sessionService;
    
    @PostMapping
    @Operation(summary = "建立工作階段", description = "建立新的工作階段")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "工作階段建立成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = SessionResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Validation Error", ref = ApiResponseTemplates.VALIDATION_ERROR_REF)))
    })
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "工作階段建立請求", required = true)
            @Valid @RequestBody SessionRequest request) {
        
        log.info("Received request to create session: {}", request.getTitle());
        
        SessionResponse response = sessionService.createSession(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "查詢所有工作階段", description = "查詢目前所有存在的工作階段")
    @ApiResponse(responseCode = "200", description = "查詢成功",
            content = @Content(mediaType = "application/json", 
            array = @ArraySchema(schema = @Schema(implementation = SessionResponse.class))))
    public ResponseEntity<List<SessionResponse>> getAllSessions() {
        
        log.info("Received request to get all sessions");
        
        List<SessionResponse> responses = sessionService.getAllSessions();
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{id}/end")
    @Operation(summary = "結束工作階段", description = "結束指定的工作階段並建立 SessionRecord")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "工作階段結束成功，SessionRecord 已建立",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = SessionRecordResponse.class))),
        @ApiResponse(responseCode = "404", description = "工作階段不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Session Not Found", ref = ApiResponseTemplates.SESSION_NOT_FOUND_REF))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "Validation Error", ref = ApiResponseTemplates.VALIDATION_ERROR_REF)))
    })
    public ResponseEntity<SessionRecordResponse> endSession(
            @Parameter(description = "工作階段 ID", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "工作階段結束請求", required = true)
            @Valid @RequestBody SessionRecordCreateRequest request) {
        
        log.info("Received request to end session: {}", id);
        
        SessionRecordResponse response = sessionService.endSession(id, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 