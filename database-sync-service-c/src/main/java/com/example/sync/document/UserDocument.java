package com.example.sync.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Documento MongoDB para almacenar usuarios sincronizados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users_sync")
public class UserDocument {
    
    @Id
    private String id;
    
    @Field("original_id")
    @Indexed(unique = true)
    private Long originalId;
    
    @Field("user_id")
    @Indexed
    private String userId;
    
    @Field("email")
    @Indexed
    private String email;
    
    @Field("username")
    @Indexed
    private String username;
    
    @Field("first_name")
    private String firstName;
    
    @Field("last_name")
    private String lastName;
    
    @Field("full_name")
    private String fullName;
    
    @Field("role")
    @Indexed
    private String role;
    
    @Field("active")
    @Indexed
    private Boolean active;
    
    @Field("department")
    @Indexed
    private String department;
    
    @Field("specialization")
    private String specialization;
    
    @Field("phone_number")
    private String phoneNumber;
    
    @Field("version")
    private String version;
    
    @Field("source")
    private String source;
    
    // Campos de auditoría del evento original
    @Field("original_created_at")
    private LocalDateTime originalCreatedAt;
    
    @Field("original_updated_at")
    private LocalDateTime originalUpdatedAt;
    
    // Campos de sincronización
    @Field("sync_created_at")
    @CreatedDate
    private LocalDateTime syncCreatedAt;
    
    @Field("sync_updated_at")
    @LastModifiedDate
    private LocalDateTime syncUpdatedAt;
    
    @Field("sync_status")
    @Indexed
    private SyncStatus syncStatus;
    
    // Información del último evento procesado
    @Field("last_event_id")
    private String lastEventId;
    
    @Field("last_event_type")
    private String lastEventType;
    
    @Field("event_metadata")
    private String eventMetadata;
    
    /**
     * Estados de sincronización
     */
    public enum SyncStatus {
        SYNCED,    // Sincronizado correctamente
        PENDING,   // Pendiente de sincronización
        ERROR,     // Error en la sincronización
        DELETED    // Marcado como eliminado
    }
} 