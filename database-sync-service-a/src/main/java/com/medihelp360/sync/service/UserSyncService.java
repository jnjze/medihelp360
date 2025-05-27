package com.medihelp360.sync.service;

import com.medihelp360.sync.domain.SyncUser;
import com.medihelp360.sync.repository.SyncUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSyncService {
    
    private final SyncUserRepository syncUserRepository;
    
    public void handleUserCreated(Map<String, Object> eventData) {
        log.info("Handling user created event for user: {}", eventData.get("userId"));
        
        try {
            UUID userId = UUID.fromString((String) eventData.get("userId"));
            String email = (String) eventData.get("email");
            String name = (String) eventData.get("name");
            String status = (String) eventData.get("status");
            List<String> roles = (List<String>) eventData.get("roles");
            
            // Verificar si el usuario ya existe para evitar duplicados
            if (syncUserRepository.existsByOriginalUserId(userId)) {
                log.warn("User already exists, skipping creation: {}", userId);
                return;
            }
            
            // Transformar datos según el esquema específico de esta base de datos
            SyncUser syncUser = SyncUser.builder()
                .originalUserId(userId)
                .userEmail(email)
                .userName(name)
                .userStatus(mapStatus(status))
                .userRoles(String.join(",", roles))
                .isActive(!"DISABLED".equals(status))
                .syncedAt(LocalDateTime.now())
                .lastEventVersion(System.currentTimeMillis()) // Usar timestamp como versión
                .build();
            
            syncUserRepository.save(syncUser);
            
            log.info("User synchronized successfully: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to sync user creation: {}", eventData, e);
            throw new RuntimeException("User sync failed", e);
        }
    }
    
    public void handleUserUpdated(Map<String, Object> eventData) {
        log.info("Handling user updated event for user: {}", eventData.get("userId"));
        
        try {
            UUID userId = UUID.fromString((String) eventData.get("userId"));
            String email = (String) eventData.get("email");
            String name = (String) eventData.get("name");
            String status = (String) eventData.get("status");
            List<String> roles = (List<String>) eventData.get("roles");
            Long currentVersion = System.currentTimeMillis();
            
            SyncUser existingUser = syncUserRepository.findByOriginalUserId(userId)
                .orElse(null);
            
            if (existingUser == null) {
                // Si el usuario no existe, crear uno nuevo (manejo de eventos desordenados)
                log.warn("User not found for update, creating new record: {}", userId);
                handleUserCreated(eventData);
                return;
            }
            
            // Actualizar datos
            existingUser.setUserEmail(email);
            existingUser.setUserName(name);
            existingUser.setUserStatus(mapStatus(status));
            existingUser.setUserRoles(String.join(",", roles));
            existingUser.setIsActive(!"DISABLED".equals(status));
            existingUser.setSyncedAt(LocalDateTime.now());
            existingUser.setLastEventVersion(currentVersion);
            
            syncUserRepository.save(existingUser);
            
            log.info("User updated successfully: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to sync user update: {}", eventData, e);
            throw new RuntimeException("User sync failed", e);
        }
    }
    
    private String mapStatus(String originalStatus) {
        // Mapear el status del dominio principal al esquema específico de esta base de datos
        switch (originalStatus) {
            case "ACTIVE":
                return "A";
            case "INACTIVE":
                return "I";
            case "DISABLED":
                return "D";
            case "PENDING_VERIFICATION":
                return "P";
            default:
                return "U"; // Unknown
        }
    }
} 