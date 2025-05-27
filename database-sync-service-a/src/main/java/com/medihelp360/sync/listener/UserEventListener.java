package com.medihelp360.sync.listener;

import com.medihelp360.sync.service.UserSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    
    private final UserSyncService userSyncService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "user-events", groupId = "database-sync-service-a")
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleUserEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received event from topic: {}, offset: {}", topic, offset);
        
        try {
            // Parsear el evento genérico
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);
            String eventType = (String) eventData.get("eventType");
            
            log.info("Processing event type: {}", eventType);
            
            switch (eventType) {
                case "UserCreatedEvent":
                    userSyncService.handleUserCreated(eventData);
                    break;
                case "UserUpdatedEvent":
                    userSyncService.handleUserUpdated(eventData);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("Error processing user event: {}", payload, e);
            throw new RuntimeException("Failed to process user event", e);
        }
    }
} 