package com.medihelp360.sync.listener;

import com.medihelp360.sync.event.UserEvent;
import com.medihelp360.sync.service.UserSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de usuario desde Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {
    
    private final UserSyncService userSyncService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "user-events",
        groupId = "sync-service-b-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Mensaje recibido - Topic: {}, Partition: {}, Offset: {}, Payload: {}", 
                topic, partition, offset, payload);
        
        try {
            // Validar payload
            if (payload == null || payload.trim().isEmpty()) {
                log.warn("Payload vacío o nulo recibido - Topic: {}, Partition: {}, Offset: {}", 
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }
            
            // Deserializar el evento
            UserEvent userEvent = objectMapper.readValue(payload, UserEvent.class);
            
            // Validar evento
            if (userEvent == null || userEvent.getEventType() == null) {
                log.warn("Evento inválido recibido - Topic: {}, Partition: {}, Offset: {}, Payload: {}", 
                        topic, partition, offset, payload);
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("Procesando evento: ID={}, Tipo={}, UserId={}, Email={}", 
                    userEvent.getEventId(), userEvent.getEventType(), 
                    userEvent.getUserId(), userEvent.getEmail());
            
            // Procesar según el tipo de evento
            String eventType = userEvent.getEventType().toString();
            switch (eventType) {
                case "UserCreatedEvent":
                    userSyncService.handleUserCreated(userEvent);
                    break;
                case "UserUpdatedEvent":
                    userSyncService.handleUserUpdated(userEvent);
                    break;
                case "UserDeletedEvent":
                    userSyncService.handleUserDeleted(userEvent);
                    break;
                default:
                    log.warn("Tipo de evento no soportado: {} - Evento: {}", eventType, userEvent);
                    break;
            }
            
            // Confirmar procesamiento exitoso
            acknowledgment.acknowledge();
            log.info("Evento procesado exitosamente: ID={}, Tipo={}", 
                    userEvent.getEventId(), userEvent.getEventType());
            
        } catch (Exception e) {
            log.error("Error procesando evento - Topic: {}, Partition: {}, Offset: {}, Payload: {}, Error: {}", 
                    topic, partition, offset, payload, e.getMessage(), e);
            
            // En caso de error, también hacer acknowledge para evitar reintento infinito
            // En un entorno de producción, podrías enviar a un topic de dead letter
            acknowledgment.acknowledge();
        }
    }
} 