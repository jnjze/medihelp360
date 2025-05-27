package com.medihelp360.sync.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "original_user_id", nullable = false, unique = true)
    private UUID originalUserId;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "user_status", nullable = false, columnDefinition = "CHAR(1)")
    private String userStatus;
    
    @Column(name = "user_roles", columnDefinition = "TEXT")
    private String userRoles;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;
    
    @Column(name = "last_event_version", nullable = false)
    @Builder.Default
    private Long lastEventVersion = 1L;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 