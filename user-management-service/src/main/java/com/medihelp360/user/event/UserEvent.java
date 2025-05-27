package com.medihelp360.user.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserEvent {
    private UUID eventId;
    private String eventType;
    private UUID aggregateId;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    
    public UserEvent(UUID aggregateId) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.timestamp = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }
} 