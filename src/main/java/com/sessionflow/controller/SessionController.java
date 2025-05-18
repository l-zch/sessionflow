package com.sessionflow.controller;

import com.sessionflow.dto.SessionDto;
import com.sessionflow.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionDto> createSession(@Valid @RequestBody SessionDto sessionDto) {
        SessionDto createdSession = sessionService.createSession(sessionDto);
        return new ResponseEntity<>(createdSession, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> getSessionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SessionDto> sessions = sessionService.getSessionsByDate(date);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> getSessionById(@PathVariable Long id) {
        SessionDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionDto sessionDto) {
        SessionDto updatedSession = sessionService.updateSession(id, sessionDto);
        return ResponseEntity.ok(updatedSession);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}