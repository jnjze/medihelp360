package com.example.sync.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de usuario compatible con la estructura del servicio A
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {
    
    @JsonProperty("eventId")
    private UUID eventId;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("aggregateId")
    private UUID aggregateId;
    
    @JsonProperty("timestamp")
    private Object timestamp; // Puede ser String, LocalDateTime o Array
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    // Campos específicos de eventos de usuario
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("roles")
    private Set<String> roles;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("previousStatus")
    private String previousStatus;
    
    // Métodos de conveniencia para compatibilidad
    public String getEventIdAsString() {
        return eventId != null ? eventId.toString() : null;
    }
    
    public String getAggregateIdAsString() {
        return aggregateId != null ? aggregateId.toString() : null;
    }
    
    public String getUserIdAsString() {
        return userId != null ? userId.toString() : null;
    }
    
    public String getFirstName() {
        if (name == null) return null;
        String[] parts = name.split(" ", 2);
        return parts[0];
    }
    
    public String getLastName() {
        if (name == null) return null;
        String[] parts = name.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }
    
    public String getRolesAsString() {
        if (roles == null || roles.isEmpty()) return null;
        return String.join(",", roles);
    }
    
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
    
    public Long getOriginalIdFromUserId() {
        if (userId == null) return null;
        return Math.abs(userId.hashCode()) % 1000000000L;
    }
    
    /**
     * Convierte el timestamp a LocalDateTime manejando diferentes formatos
     */
    public LocalDateTime getTimestamp() {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        
        if (timestamp instanceof LocalDateTime) {
            return (LocalDateTime) timestamp;
        }
        
        if (timestamp instanceof String) {
            try {
                return LocalDateTime.parse((String) timestamp);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
        
        if (timestamp instanceof List) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> timestampArray = (List<Integer>) timestamp;
                if (timestampArray.size() >= 6) {
                    return LocalDateTime.of(
                        timestampArray.get(0), // year
                        timestampArray.get(1), // month
                        timestampArray.get(2), // day
                        timestampArray.get(3), // hour
                        timestampArray.get(4), // minute
                        timestampArray.get(5), // second
                        timestampArray.size() > 6 ? timestampArray.get(6) : 0 // nanoseconds
                    );
                }
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
        
        return LocalDateTime.now();
    }
} 