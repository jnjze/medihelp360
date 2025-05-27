package com.example.sync.controller;

import com.example.sync.document.UserDocument;
import com.example.sync.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para consultar datos de usuarios sincronizados en MongoDB
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserSyncController {
    
    private final UserDocumentRepository userRepository;
    
    /**
     * Obtiene todos los usuarios con paginación
     */
    @GetMapping
    public ResponseEntity<Page<UserDocument>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "syncUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDocument> users = userRepository.findAll(pageable);
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca un usuario por su ID original
     */
    @GetMapping("/original/{originalId}")
    public ResponseEntity<UserDocument> getUserByOriginalId(@PathVariable Long originalId) {
        Optional<UserDocument> user = userRepository.findByOriginalId(originalId);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca un usuario por userId
     */
    @GetMapping("/userid/{userId}")
    public ResponseEntity<UserDocument> getUserByUserId(@PathVariable String userId) {
        Optional<UserDocument> user = userRepository.findByUserId(userId);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca un usuario por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDocument> getUserByEmail(@PathVariable String email) {
        Optional<UserDocument> user = userRepository.findByEmail(email);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca un usuario por username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDocument> getUserByUsername(@PathVariable String username) {
        Optional<UserDocument> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca usuarios por rol
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDocument>> getUsersByRole(@PathVariable String role) {
        List<UserDocument> users = userRepository.findByRole(role);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca usuarios activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserDocument>> getActiveUsers() {
        List<UserDocument> users = userRepository.findByActiveTrue();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca usuarios por departamento
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<UserDocument>> getUsersByDepartment(@PathVariable String department) {
        List<UserDocument> users = userRepository.findByDepartment(department);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca usuarios por especialización
     */
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<UserDocument>> getUsersBySpecialization(@PathVariable String specialization) {
        List<UserDocument> users = userRepository.findBySpecialization(specialization);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca usuarios por texto (nombre, email, username)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserDocument>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDocument> users = userRepository.findBySearchText(query, pageable);
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Busca usuarios por rol y estado activo con paginación
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<UserDocument>> filterUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (role != null && active != null) {
            Page<UserDocument> users = userRepository.findByRoleAndActive(role, active, pageable);
            return ResponseEntity.ok(users);
        } else {
            Page<UserDocument> users = userRepository.findAll(pageable);
            return ResponseEntity.ok(users);
        }
    }
    
    /**
     * Obtiene estadísticas de usuarios
     */
    @GetMapping("/stats")
    public ResponseEntity<UserStats> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        
        UserStats stats = UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .build();
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Busca usuarios sincronizados después de una fecha
     */
    @GetMapping("/sync/after")
    public ResponseEntity<List<UserDocument>> getUsersSyncedAfter(
            @RequestParam String dateTime) {
        
        try {
            LocalDateTime syncDateTime = LocalDateTime.parse(dateTime);
            List<UserDocument> users = userRepository.findBySyncUpdatedAtAfter(syncDateTime);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error parsing dateTime: {}", dateTime, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Busca usuarios por estado de sincronización
     */
    @GetMapping("/sync/status/{status}")
    public ResponseEntity<List<UserDocument>> getUsersBySyncStatus(@PathVariable String status) {
        try {
            UserDocument.SyncStatus syncStatus = UserDocument.SyncStatus.valueOf(status.toUpperCase());
            List<UserDocument> users = userRepository.findBySyncStatus(syncStatus);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            log.error("Invalid sync status: {}", status, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Clase para estadísticas de usuarios
     */
    public static class UserStats {
        public long totalUsers;
        public long activeUsers;
        public long inactiveUsers;
        
        public static UserStatsBuilder builder() {
            return new UserStatsBuilder();
        }
        
        public static class UserStatsBuilder {
            private long totalUsers;
            private long activeUsers;
            private long inactiveUsers;
            
            public UserStatsBuilder totalUsers(long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }
            
            public UserStatsBuilder activeUsers(long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }
            
            public UserStatsBuilder inactiveUsers(long inactiveUsers) {
                this.inactiveUsers = inactiveUsers;
                return this;
            }
            
            public UserStats build() {
                UserStats stats = new UserStats();
                stats.totalUsers = this.totalUsers;
                stats.activeUsers = this.activeUsers;
                stats.inactiveUsers = this.inactiveUsers;
                return stats;
            }
        }
    }
} 