package com.medihelp360.sync.controller;

import com.medihelp360.sync.domain.SyncUser;
import com.medihelp360.sync.repository.SyncUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {
    
    private final SyncUserRepository syncUserRepository;
    
    @GetMapping("/users")
    public ResponseEntity<Page<SyncUser>> getAllSyncUsers(Pageable pageable) {
        log.info("Getting all synchronized users with pagination: {}", pageable);
        Page<SyncUser> users = syncUserRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{originalUserId}")
    public ResponseEntity<SyncUser> getSyncUserByOriginalId(@PathVariable UUID originalUserId) {
        log.info("Getting synchronized user by original ID: {}", originalUserId);
        return syncUserRepository.findByOriginalUserId(originalUserId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/users/status/{status}")
    public ResponseEntity<Page<SyncUser>> getUsersByStatus(@PathVariable String status, Pageable pageable) {
        log.info("Getting synchronized users by status: {}", status);
        Page<SyncUser> users = syncUserRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSyncStats() {
        log.info("Getting synchronization statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Contar usuarios por estado
        stats.put("totalUsers", syncUserRepository.count());
        stats.put("activeUsers", syncUserRepository.countActiveUsers());
        stats.put("statusA", syncUserRepository.countByUserStatus("A"));
        stats.put("statusI", syncUserRepository.countByUserStatus("I"));
        stats.put("statusD", syncUserRepository.countByUserStatus("D"));
        stats.put("statusP", syncUserRepository.countByUserStatus("P"));
        stats.put("statusU", syncUserRepository.countByUserStatus("U"));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "database-sync-service-a");
        health.put("database", "PostgreSQL");
        return ResponseEntity.ok(health);
    }
} 