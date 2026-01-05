package ma.spring.defenseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.event.DefenseEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "defense-events";

    public void sendDefenseEvent(DefenseEvent event) {
        try {
            log.info("📤 Envoi événement Kafka: {} pour defense {}", event.getEventType(), event.getDefenseId());
            kafkaTemplate.send(TOPIC, event);
            log.info("✅ Événement envoyé avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur envoi événement Kafka: {}", e.getMessage(), e);
        }
    }
}
