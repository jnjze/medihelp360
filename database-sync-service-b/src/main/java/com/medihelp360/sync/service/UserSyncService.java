package com.medihelp360.sync.service;

import com.medihelp360.sync.entity.UserEntity;
import com.medihelp360.sync.event.UserEvent;
import com.medihelp360.sync.repository.UserEntityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para sincronización de usuarios con MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {
    
    private final UserEntityRepository userRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Maneja la creación de un usuario
     */
    @Transactional
    public void handleUserCreated(UserEvent userEvent) {
        log.info("Procesando creación de usuario: userId={}, email={}", 
                userEvent.getUserId(), userEvent.getEmail());
        
        try {
            // Usar el hash del UUID como ID único para evitar problemas con Long
            Long originalId = Math.abs((long) userEvent.getUserId().hashCode());
            
            // Verificar si el usuario ya existe
            Optional<UserEntity> existingUser = userRepository.findByOriginalId(originalId);
            if (existingUser.isPresent()) {
                log.warn("Usuario ya existe, actualizando en lugar de crear: originalId={}", originalId);
                handleUserUpdated(userEvent);
                return;
            }
            
            // Crear nueva entidad de usuario
            UserEntity userEntity = mapEventToEntity(userEvent);
            userEntity.setSyncStatus(UserEntity.SyncStatus.SYNCED);
            
            UserEntity savedUser = userRepository.save(userEntity);
            log.info("Usuario creado exitosamente: id={}, originalId={}, email={}", 
                    savedUser.getId(), savedUser.getOriginalId(), savedUser.getEmail());
            
        } catch (Exception e) {
            log.error("Error creando usuario: userId={}, error={}", 
                    userEvent.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Maneja la actualización de un usuario
     */
    @Transactional
    public void handleUserUpdated(UserEvent userEvent) {
        log.info("Procesando actualización de usuario: userId={}, email={}", 
                userEvent.getUserId(), userEvent.getEmail());
        
        try {
            Long originalId = Math.abs((long) userEvent.getUserId().hashCode());
            Optional<UserEntity> existingUser = userRepository.findByOriginalId(originalId);
            
            if (existingUser.isPresent()) {
                // Actualizar usuario existente
                UserEntity userEntity = existingUser.get();
                updateEntityFromEvent(userEntity, userEvent);
                userEntity.setSyncStatus(UserEntity.SyncStatus.SYNCED);
                
                UserEntity savedUser = userRepository.save(userEntity);
                log.info("Usuario actualizado exitosamente: id={}, originalId={}, email={}", 
                        savedUser.getId(), savedUser.getOriginalId(), savedUser.getEmail());
            } else {
                // Si no existe, crear nuevo
                log.info("Usuario no encontrado para actualizar, creando nuevo: originalId={}", originalId);
                handleUserCreated(userEvent);
            }
            
        } catch (Exception e) {
            log.error("Error actualizando usuario: userId={}, error={}", 
                    userEvent.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Maneja la eliminación de un usuario
     */
    @Transactional
    public void handleUserDeleted(UserEvent userEvent) {
        log.info("Procesando eliminación de usuario: userId={}", userEvent.getUserId());
        
        try {
            Long originalId = Math.abs((long) userEvent.getUserId().hashCode());
            Optional<UserEntity> existingUser = userRepository.findByOriginalId(originalId);
            
            if (existingUser.isPresent()) {
                UserEntity userEntity = existingUser.get();
                
                // Marcar como eliminado en lugar de eliminar físicamente
                userEntity.setSyncStatus(UserEntity.SyncStatus.DELETED);
                userEntity.setActive(false);
                userEntity.setLastEventId(userEvent.getEventId());
                userEntity.setLastEventType(userEvent.getEventType());
                userEntity.setSyncUpdatedAt(LocalDateTime.now());
                
                userRepository.save(userEntity);
                log.info("Usuario marcado como eliminado: originalId={}", originalId);
            } else {
                log.warn("Usuario no encontrado para eliminar: originalId={}", originalId);
            }
            
        } catch (Exception e) {
            log.error("Error eliminando usuario: userId={}, error={}", 
                    userEvent.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Mapea un evento de usuario a una entidad JPA
     */
    private UserEntity mapEventToEntity(UserEvent userEvent) {
        Long originalId = Math.abs((long) userEvent.getUserId().hashCode());
        
        return UserEntity.builder()
                .originalId(originalId)
                .username(userEvent.getEmail()) // Usar email como username
                .email(userEvent.getEmail())
                .firstName(userEvent.getFirstName())
                .lastName(userEvent.getLastName())
                .phoneNumber(null) // No disponible en el evento del servicio A
                .role(userEvent.getRole())
                .active(userEvent.getActive())
                .department(null) // No disponible en el evento del servicio A
                .specialization(null) // No disponible en el evento del servicio A
                .originalCreatedAt(userEvent.getTimestamp())
                .originalUpdatedAt(userEvent.getTimestamp())
                .lastEventId(userEvent.getEventId())
                .lastEventType(userEvent.getEventType())
                .source("user-management-service")
                .version("1.0")
                .eventMetadata(serializeMetadata(userEvent.getMetadata()))
                .build();
    }
    
    /**
     * Actualiza una entidad existente con datos del evento
     */
    private void updateEntityFromEvent(UserEntity entity, UserEvent userEvent) {
        entity.setUsername(userEvent.getEmail());
        entity.setEmail(userEvent.getEmail());
        entity.setFirstName(userEvent.getFirstName());
        entity.setLastName(userEvent.getLastName());
        entity.setRole(userEvent.getRole());
        entity.setActive(userEvent.getActive());
        entity.setOriginalUpdatedAt(userEvent.getTimestamp());
        entity.setLastEventId(userEvent.getEventId());
        entity.setLastEventType(userEvent.getEventType());
        entity.setSource("user-management-service");
        entity.setVersion("1.0");
        entity.setEventMetadata(serializeMetadata(userEvent.getMetadata()));
    }
    
    /**
     * Serializa los metadatos a JSON string
     */
    private String serializeMetadata(Object metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Error serializando metadatos: {}", e.getMessage());
            return null;
        }
    }
} 