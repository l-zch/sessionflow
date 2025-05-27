package com.sessionflow.controller;

import com.sessionflow.config.ApiResponseTemplates;
import com.sessionflow.dto.ScheduleEntryRequest;
import com.sessionflow.dto.ScheduleEntryResponse;
import com.sessionflow.exception.ErrorResponse;
import com.sessionflow.service.ScheduleEntryService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule-entries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ScheduleEntry", description = "排程管理 API")
public class ScheduleEntryController {
    
    private final ScheduleEntryService scheduleEntryService;
    
    @PostMapping
    @Operation(summary = "建立排程", description = "建立新的排程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "排程建立成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ScheduleEntryResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.VALIDATION_ERROR_REF))),
        @ApiResponse(responseCode = "422", description = "時間區間錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.INVALID_TIME_RANGE_REF)))
    })
    public ResponseEntity<ScheduleEntryResponse> createScheduleEntry(
            @Parameter(description = "排程建立請求", required = true)
            @Valid @RequestBody ScheduleEntryRequest request) {
        log.info("建立排程請求 - title: {}", request.getTitle());
        
        ScheduleEntryResponse response = scheduleEntryService.createScheduleEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新排程", description = "更新指定排程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "排程更新成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ScheduleEntryResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.VALIDATION_ERROR_REF))),
        @ApiResponse(responseCode = "404", description = "排程不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.SCHEDULE_ENTRY_NOT_FOUND_REF))),
        @ApiResponse(responseCode = "422", description = "時間區間錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.INVALID_TIME_RANGE_REF)))
    })
    public ResponseEntity<ScheduleEntryResponse> updateScheduleEntry(
            @Parameter(description = "排程 ID", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "排程更新請求", required = true)
            @Valid @RequestBody ScheduleEntryRequest request) {
        log.info("更新排程請求 - ID: {}, title: {}", id, request.getTitle());
        
        ScheduleEntryResponse response = scheduleEntryService.updateScheduleEntry(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除排程", description = "刪除指定排程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "排程刪除成功"),
        @ApiResponse(responseCode = "404", description = "排程不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.SCHEDULE_ENTRY_NOT_FOUND_REF)))
    })
    public ResponseEntity<Void> deleteScheduleEntry(
            @Parameter(description = "排程 ID", required = true, example = "1") @PathVariable Long id) {
        log.info("刪除排程請求 - ID: {}", id);
        
        scheduleEntryService.deleteScheduleEntry(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @Operation(summary = "查詢排程", description = "根據指定時間區間查詢所有排程")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功",
                content = @Content(mediaType = "application/json", 
                array = @ArraySchema(schema = @Schema(implementation = ScheduleEntryResponse.class)))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(ref = ApiResponseTemplates.TYPE_MISMATCH_ERROR_REF)))
    })
    public ResponseEntity<List<ScheduleEntryResponse>> getScheduleEntries(
            @Parameter(description = "開始日期", required = true, example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", required = true, example = "2024-01-16")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("查詢排程請求 - startDate: {}, endDate: {}", startDate, endDate);
        
        List<ScheduleEntryResponse> responses = scheduleEntryService.getScheduleEntries(startDate, endDate);
        return ResponseEntity.ok(responses);
    }
} 