package com.sessionflow.controller;

import com.sessionflow.config.ApiResponseTemplates;
import com.sessionflow.dto.SessionRecordResponse;
import com.sessionflow.dto.SessionRecordUpdateRequest;
import com.sessionflow.exception.ErrorResponse;
import com.sessionflow.service.SessionRecordService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/session-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SessionRecord", description = "工作階段紀錄管理 API")
public class SessionRecordController {
    
    private final SessionRecordService sessionRecordService;
    
    @GetMapping
    @Operation(summary = "查詢工作階段紀錄", description = "根據時間區間與任務ID查詢工作階段紀錄，所有參數皆為可選")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功",
                content = @Content(mediaType = "application/json", 
                array = @ArraySchema(schema = @Schema(implementation = SessionRecordResponse.class)))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.TYPE_MISMATCH_ERROR_REF)))
    })
    public ResponseEntity<List<SessionRecordResponse>> getSessionRecords(
            @Parameter(description = "開始日期 (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "結束日期 (YYYY-MM-DD)", example = "2024-01-31")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "任務ID", example = "1")
            @RequestParam(required = false) Long taskId) {
        
        log.info("查詢工作階段紀錄請求 - startDate: {}, endDate: {}, taskId: {}", startDate, endDate, taskId);
        
        List<SessionRecordResponse> records = sessionRecordService.getSessionRecords(startDate, endDate, taskId);
        
        log.info("成功查詢到 {} 筆工作階段紀錄", records.size());
        return ResponseEntity.ok(records);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新工作階段紀錄", description = "更新指定ID的工作階段紀錄")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = SessionRecordResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.VALIDATION_ERROR_REF))),
        @ApiResponse(responseCode = "404", description = "工作階段紀錄不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.SESSION_RECORD_NOT_FOUND_REF)))
    })
    public ResponseEntity<SessionRecordResponse> updateSessionRecord(
            @Parameter(description = "工作階段紀錄ID", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "工作階段紀錄更新請求", required = true)
            @Valid @RequestBody SessionRecordUpdateRequest updateRequest) {
        
        log.info("更新工作階段紀錄請求 - ID: {}", id);
        
        SessionRecordResponse updatedRecord = sessionRecordService.updateSessionRecord(id, updateRequest);
        
        log.info("成功更新工作階段紀錄 - ID: {}", updatedRecord.getId());
        return ResponseEntity.ok(updatedRecord);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除工作階段紀錄", description = "刪除指定ID的工作階段紀錄")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "刪除成功"),
        @ApiResponse(responseCode = "404", description = "工作階段紀錄不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.SESSION_RECORD_NOT_FOUND_REF)))
    })
    public ResponseEntity<Void> deleteSessionRecord(
            @Parameter(description = "工作階段紀錄ID", required = true, example = "1")
            @PathVariable Long id) {
        
        log.info("刪除工作階段紀錄請求 - ID: {}", id);
        
        sessionRecordService.deleteSessionRecord(id);
        
        log.info("成功刪除工作階段紀錄 - ID: {}", id);
        return ResponseEntity.noContent().build();
    }
} 