package com.sessionflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "系統健康檢查 API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康檢查", description = "檢查系統是否正常運作")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "系統正常運作")
    })
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "Task Management System"
        ));
    }
} 