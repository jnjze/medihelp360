package com.medihelp360.sync.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de usuario recibido desde Kafka
 * Compatible con eventos del user-management-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {
    
    // Campos del evento base
    private UUID eventId;
    private String eventType; // UserCreatedEvent, UserUpdatedEvent, etc.
    private UUID aggregateId; // ID del usuario
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    // Campos específicos del evento de usuario (del servicio A)
    private UUID userId;
    private String email;
    private String name;
    private Set<String> roles;
    private String status;
    private String previousStatus; // Para eventos de actualización
    
    // Métodos de conveniencia para compatibilidad
    public String getEventId() {
        return eventId != null ? eventId.toString() : null;
    }
    
    public String getAggregateId() {
        return aggregateId != null ? aggregateId.toString() : null;
    }
    
    public Long getUserIdAsLong() {
        return userId != null ? userId.getMostSignificantBits() : null;
    }
    
    public String getFirstName() {
        // Extraer primer nombre del campo 'name'
        if (name != null && name.contains(" ")) {
            return name.split(" ")[0];
        }
        return name;
    }
    
    public String getLastName() {
        // Extraer apellido del campo 'name'
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ");
            if (parts.length > 1) {
                return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
            }
        }
        return "";
    }
    
    public String getRole() {
        // Convertir Set<String> roles a String
        if (roles != null && !roles.isEmpty()) {
            return String.join(",", roles);
        }
        return null;
    }
    
    public Boolean getActive() {
        // Mapear status a boolean
        return !"DISABLED".equals(status);
    }
} 