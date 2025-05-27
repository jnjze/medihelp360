package com.example.sync.listener;

import com.example.sync.event.UserEvent;
import com.example.sync.service.UserSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener de eventos de usuario desde Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {
    
    private final UserSyncService userSyncService;
    private final ObjectMapper objectMapper;
    
    /**
     * Maneja eventos de usuario desde Kafka
     */
    @KafkaListener(
        topics = "${spring.kafka.topic.user-events:user-events}",
        groupId = "${spring.kafka.consumer.group-id:database-sync-service-c}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleUserEvent(
            ConsumerRecord<String, Object> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.debug("Mensaje recibido - Topic: {}, Partition: {}, Offset: {}", 
                topic, partition, offset);
        
        try {
            // Extraer el payload del ConsumerRecord
            Object payload = record.value();
            
            // Validar payload
            if (payload == null) {
                log.warn("Payload nulo recibido en topic: {}, partition: {}, offset: {}", 
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }
            
            log.debug("Payload extraído: {}", payload);
            
            // Convertir payload a UserEvent
            UserEvent userEvent = convertToUserEvent(payload);
            
            // Validar evento
            if (userEvent.getEventType() == null || userEvent.getUserId() == null) {
                log.warn("Evento inválido recibido: eventType={}, userId={}", 
                        userEvent.getEventType(), userEvent.getUserId());
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("Procesando evento: ID={}, Tipo={}, UserId={}, Email={}", 
                    userEvent.getEventIdAsString(), userEvent.getEventType(), 
                    userEvent.getUserIdAsString(), userEvent.getEmail());
            
            // Procesar según el tipo de evento
            switch (userEvent.getEventType()) {
                case "UserCreatedEvent":
                    userSyncService.processUserCreatedEvent(userEvent);
                    break;
                case "UserUpdatedEvent":
                    userSyncService.processUserUpdatedEvent(userEvent);
                    break;
                case "UserDeletedEvent":
                    userSyncService.processUserDeletedEvent(userEvent);
                    break;
                default:
                    log.warn("Tipo de evento no soportado: {}", userEvent.getEventType());
                    break;
            }
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            log.info("Evento procesado exitosamente: ID={}, Tipo={}", 
                    userEvent.getEventIdAsString(), userEvent.getEventType());
            
        } catch (Exception e) {
            log.error("Error procesando mensaje de Kafka - Topic: {}, Partition: {}, Offset: {}", 
                    topic, partition, offset, e);
            
            // En caso de error, también confirmamos para evitar reprocessamiento infinito
            // En un entorno de producción, podrías enviar a un dead letter topic
            acknowledgment.acknowledge();
            
            throw new RuntimeException("Error procesando evento de usuario", e);
        }
    }
    
    /**
     * Convierte el payload a UserEvent
     */
    private UserEvent convertToUserEvent(Object payload) throws Exception {
        if (payload instanceof UserEvent) {
            return (UserEvent) payload;
        } else if (payload instanceof Map) {
            // Si es un Map, convertir a UserEvent
            return objectMapper.convertValue(payload, UserEvent.class);
        } else if (payload instanceof String) {
            // Si es String JSON, deserializar
            return objectMapper.readValue((String) payload, UserEvent.class);
        } else {
            // Intentar convertir usando ObjectMapper
            String jsonPayload = objectMapper.writeValueAsString(payload);
            return objectMapper.readValue(jsonPayload, UserEvent.class);
        }
    }
} 