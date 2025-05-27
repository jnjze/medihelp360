package com.example.sync.service;

import com.example.sync.document.UserDocument;
import com.example.sync.event.UserEvent;
import com.example.sync.repository.UserDocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de sincronización de usuarios para MongoDB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {
    
    private final UserDocumentRepository userRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Procesa un evento de creación de usuario
     */
    @Transactional
    public void processUserCreatedEvent(UserEvent event) {
        log.info("Procesando creación de usuario: userId={}, email={}", 
                event.getUserIdAsString(), event.getEmail());
        
        try {
            Long originalId = event.getOriginalIdFromUserId();
            
            // Verificar si el usuario ya existe
            Optional<UserDocument> existingUser = userRepository.findByOriginalId(originalId);
            if (existingUser.isPresent()) {
                log.warn("Usuario ya existe con originalId={}, actualizando en su lugar", originalId);
                processUserUpdatedEvent(event);
                return;
            }
            
            // Crear nuevo documento de usuario
            UserDocument userDocument = UserDocument.builder()
                    .originalId(originalId)
                    .userId(event.getUserIdAsString())
                    .email(event.getEmail())
                    .username(generateUsername(event.getEmail()))
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .fullName(event.getName())
                    .role(event.getRolesAsString())
                    .active(event.isActive())
                    .source("user-management-service")
                    .version("1.0")
                    .originalCreatedAt(event.getTimestamp())
                    .originalUpdatedAt(event.getTimestamp())
                    .syncStatus(UserDocument.SyncStatus.SYNCED)
                    .lastEventId(event.getEventIdAsString())
                    .lastEventType(event.getEventType())
                    .eventMetadata(serializeMetadata(event))
                    .build();
            
            UserDocument savedUser = userRepository.save(userDocument);
            log.info("Usuario creado exitosamente: id={}, originalId={}, email={}", 
                    savedUser.getId(), savedUser.getOriginalId(), savedUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error procesando evento de creación de usuario: {}", event.getEventIdAsString(), e);
            throw new RuntimeException("Error procesando evento de creación de usuario", e);
        }
    }
    
    /**
     * Procesa un evento de actualización de usuario
     */
    @Transactional
    public void processUserUpdatedEvent(UserEvent event) {
        log.info("Procesando actualización de usuario: userId={}, email={}", 
                event.getUserIdAsString(), event.getEmail());
        
        try {
            Long originalId = event.getOriginalIdFromUserId();
            
            Optional<UserDocument> existingUserOpt = userRepository.findByOriginalId(originalId);
            if (existingUserOpt.isEmpty()) {
                log.warn("Usuario no encontrado con originalId={}, creando nuevo usuario", originalId);
                processUserCreatedEvent(event);
                return;
            }
            
            UserDocument existingUser = existingUserOpt.get();
            
            // Actualizar campos
            existingUser.setEmail(event.getEmail());
            existingUser.setFirstName(event.getFirstName());
            existingUser.setLastName(event.getLastName());
            existingUser.setFullName(event.getName());
            existingUser.setRole(event.getRolesAsString());
            existingUser.setActive(event.isActive());
            existingUser.setOriginalUpdatedAt(event.getTimestamp());
            existingUser.setSyncStatus(UserDocument.SyncStatus.SYNCED);
            existingUser.setLastEventId(event.getEventIdAsString());
            existingUser.setLastEventType(event.getEventType());
            existingUser.setEventMetadata(serializeMetadata(event));
            
            UserDocument savedUser = userRepository.save(existingUser);
            log.info("Usuario actualizado exitosamente: id={}, originalId={}, email={}", 
                    savedUser.getId(), savedUser.getOriginalId(), savedUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error procesando evento de actualización de usuario: {}", event.getEventIdAsString(), e);
            throw new RuntimeException("Error procesando evento de actualización de usuario", e);
        }
    }
    
    /**
     * Procesa un evento de eliminación de usuario
     */
    @Transactional
    public void processUserDeletedEvent(UserEvent event) {
        log.info("Procesando eliminación de usuario: userId={}", event.getUserIdAsString());
        
        try {
            Long originalId = event.getOriginalIdFromUserId();
            
            Optional<UserDocument> existingUserOpt = userRepository.findByOriginalId(originalId);
            if (existingUserOpt.isEmpty()) {
                log.warn("Usuario no encontrado para eliminación con originalId={}", originalId);
                return;
            }
            
            UserDocument existingUser = existingUserOpt.get();
            
            // Marcar como eliminado en lugar de eliminar físicamente
            existingUser.setActive(false);
            existingUser.setSyncStatus(UserDocument.SyncStatus.DELETED);
            existingUser.setLastEventId(event.getEventIdAsString());
            existingUser.setLastEventType(event.getEventType());
            existingUser.setEventMetadata(serializeMetadata(event));
            
            UserDocument savedUser = userRepository.save(existingUser);
            log.info("Usuario marcado como eliminado: id={}, originalId={}", 
                    savedUser.getId(), savedUser.getOriginalId());
            
        } catch (Exception e) {
            log.error("Error procesando evento de eliminación de usuario: {}", event.getEventIdAsString(), e);
            throw new RuntimeException("Error procesando evento de eliminación de usuario", e);
        }
    }
    
    /**
     * Genera un username basado en el email
     */
    private String generateUsername(String email) {
        if (email == null) return null;
        return email.split("@")[0];
    }
    
    /**
     * Serializa los metadatos del evento a JSON
     */
    private String serializeMetadata(UserEvent event) {
        try {
            return objectMapper.writeValueAsString(event.getMetadata());
        } catch (JsonProcessingException e) {
            log.warn("Error serializando metadatos del evento: {}", event.getEventIdAsString(), e);
            return "{}";
        }
    }
} 