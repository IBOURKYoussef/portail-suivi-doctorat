package ma.spring.defenseservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(@Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        if (kafkaTemplate == null) {
            log.warn("Kafka is not configured - events will not be published");
        }
    }

    public void publishDefenseEvent(DefenseEvent event) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not available - skipping defense event publication: {}", event.getEventType());
            return;
        }
        try {
            kafkaTemplate.send("defense-events", event);
            log.info("Defense event published: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish defense event", e);
        }
    }

    public void publishNotification(DefenseEvent event) {
        if (kafkaTemplate == null) {
            log.warn("Kafka not available - skipping notification publication: {}", event.getEventType());
            return;
        }
        try {
            kafkaTemplate.send("notifications", event);
            log.info("Notification published: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish notification", e);
        }
    }
}