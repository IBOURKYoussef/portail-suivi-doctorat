package ma.spring.defenseservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.kafka.event.DefenseEvent;
import ma.spring.defenseservice.kafka.event.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Topics définis dans le cahier des charges
    private static final String TOPIC_DEFENSE_EVENTS = "defense-events";
    private static final String TOPIC_NOTIFICATIONS = "notification-events";
    private static final String TOPIC_DOCUMENT_GENERATION = "document-generation-events";
    private static final String TOPIC_AUDIT_LOGS = "audit-log-events";

    public void publishDefenseEvent(DefenseEvent event) {
        String key = event.getDefenseId() != null ?
                event.getDefenseId().toString() :
                event.getDoctorantId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC_DEFENSE_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Defense event published successfully: {} to topic: {}",
                        event.getEventType(), result.getRecordMetadata().topic());
            } else {
                log.error("Failed to publish defense event: {}", event.getEventType(), ex);
                // TODO: Implémenter une stratégie de retry ou stockage dans DLQ
            }
        });
    }

    public void publishNotification(NotificationEvent notification) {
        kafkaTemplate.send(TOPIC_NOTIFICATIONS, notification)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish notification: {}", notification.getTemplateCode(), ex);
                    }
                });
    }

    public void publishDocumentGenerationEvent(Long defenseId, String documentType, Map<String, Object> data) {
        Map<String, Object> event = Map.of(
                "defenseId", defenseId,
                "documentType", documentType,
                "templateCode", "DEFENSE_" + documentType,
                "data", data,
                "timestamp", java.time.LocalDateTime.now()
        );

        kafkaTemplate.send(TOPIC_DOCUMENT_GENERATION, event);
    }

    public void publishAuditLog(String action, Long userId, String userRole, Object details) {
        Map<String, Object> auditEvent = Map.of(
                "service", "defense-service",
                "action", action,
                "userId", userId,
                "userRole", userRole,
                "details", details,
                "timestamp", java.time.LocalDateTime.now(),
                "ipAddress", "extract-from-context" // À extraire du contexte HTTP
        );

        kafkaTemplate.send(TOPIC_AUDIT_LOGS, auditEvent);
    }
}
