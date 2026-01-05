package ma.spring.registrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.config.KafkaTopicConfig;
import ma.spring.registrationservice.event.RegistrationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service pour publier des événements sur Kafka
 * Avec gestion d'erreur pour continuer si Kafka n'est pas disponible
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publier un événement d'inscription
     */
    public void publishRegistrationEvent(RegistrationEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.REGISTRATION_EVENTS_TOPIC, event);
            log.info("✅ Published registration event: {}", event.getEventType());
        } catch (Exception e) {
            // ⚠️ Log l'erreur mais ne bloque pas l'application
            log.warn("⚠️ Could not publish registration event to Kafka (service will continue): {}",
                    e.getMessage());
            log.debug("Kafka error details:", e);
        }
    }

    /**
     * Publier un événement de notification
     */
    public void publishNotificationEvent(RegistrationEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.NOTIFICATION_TOPIC, event);
            log.info("✅ Published notification event for user: {}", event.getUserEmail());
        } catch (Exception e) {
            log.warn("⚠️ Could not publish notification event to Kafka (service will continue): {}",
                    e.getMessage());
            log.debug("Kafka error details:", e);
        }
    }
}

//package ma.spring.registrationservice.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import ma.spring.registrationservice.config.KafkaTopicConfig;
//import ma.spring.registrationservice.event.RegistrationEvent;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
///**
// * Service pour publier des événements sur Kafka
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EventPublisher {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    /**
//     * Publier un événement d'inscription
//     */
//    public void publishRegistrationEvent(RegistrationEvent event) {
//        try {
//            kafkaTemplate.send(KafkaTopicConfig.REGISTRATION_EVENTS_TOPIC, event);
//            log.info("Published registration event: {}", event.getEventType());
//        } catch (Exception e) {
//            log.error("Error publishing registration event", e);
//        }
//    }
//
//    /**
//     * Publier un événement de notification
//     */
//    public void publishNotificationEvent(RegistrationEvent event) {
//        try {
//            kafkaTemplate.send(KafkaTopicConfig.NOTIFICATION_TOPIC, event);
//            log.info("Published notification event for user: {}", event.getUserEmail());
//        } catch (Exception e) {
//            log.error("Error publishing notification event", e);
//        }
//    }
//}