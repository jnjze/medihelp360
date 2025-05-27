package com.medihelp360.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad de usuario almacenada en MySQL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_sync", indexes = {
    @Index(name = "idx_original_id", columnList = "originalId", unique = true),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_active", columnList = "active"),
    @Index(name = "idx_department", columnList = "department")
})
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "original_id", nullable = false, unique = true)
    private Long originalId; // ID original del servicio de usuarios
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "role", length = 50)
    private String role;
    
    @Column(name = "active")
    private Boolean active;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "specialization", length = 100)
    private String specialization;
    
    // Metadatos de sincronización
    @Column(name = "last_event_id", length = 255)
    private String lastEventId;
    
    @Column(name = "last_event_type", length = 50)
    private String lastEventType;
    
    @Column(name = "source", length = 100)
    private String source;
    
    @Column(name = "version", length = 20)
    private String version;
    
    @CreatedDate
    @Column(name = "sync_created_at", nullable = false, updatable = false)
    private LocalDateTime syncCreatedAt;
    
    @LastModifiedDate
    @Column(name = "sync_updated_at", nullable = false)
    private LocalDateTime syncUpdatedAt;
    
    // Timestamps originales del servicio fuente
    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;
    
    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;
    
    // Metadatos adicionales del evento (como JSON string)
    @Column(name = "event_metadata", columnDefinition = "TEXT")
    private String eventMetadata;
    
    // Estado de sincronización
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.SYNCED;
    
    public enum SyncStatus {
        SYNCED,
        PENDING,
        ERROR,
        DELETED
    }
} 