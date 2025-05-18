package com.sessionflow.controller;

import com.sessionflow.dto.SessionLogDto;
import com.sessionflow.service.SessionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SessionLogController {

    @Autowired
    private SessionLogService sessionLogService;

    @PostMapping("/sessions/{sessionId}/logs")
    public ResponseEntity<SessionLogDto> startSessionLog(@PathVariable Long sessionId) {
        SessionLogDto createdLog = sessionLogService.startSessionLog(sessionId);
        return new ResponseEntity<>(createdLog, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}/logs")
    public ResponseEntity<List<SessionLogDto>> getLogsBySessionId(@PathVariable Long sessionId) {
        List<SessionLogDto> logs = sessionLogService.getLogsBySessionId(sessionId);
        return ResponseEntity.ok(logs);
    }

    @PatchMapping("/logs/{id}")
    public ResponseEntity<SessionLogDto> updateSessionLog(
            @PathVariable Long id,
            @Valid @RequestBody SessionLogDto sessionLogDto) {
        SessionLogDto updatedLog = sessionLogService.updateSessionLog(id, sessionLogDto);
        return ResponseEntity.ok(updatedLog);
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Void> deleteSessionLog(@PathVariable Long id) {
        sessionLogService.deleteSessionLog(id);
        return ResponseEntity.noContent().build();
    }
}