package ma.spring.registrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.event.RegistrationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object message) {
        try {
            log.info("📤 Envoi message Kafka topic {}: {}", topic, message);
            kafkaTemplate.send(topic, message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Message Kafka envoyé avec succès - topic: {}, offset: {}",
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Échec envoi message Kafka topic {}: {}", topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ Erreur critique envoi Kafka topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Erreur d'envoi Kafka", e);
        }
    }

    // Méthode spécifique pour les événements d'inscription
    public void sendRegistrationEvent(RegistrationEvent event) {
        log.info("📤 Envoi RegistrationEvent: {}", event);

        // Convertir en Map pour éviter les problèmes de désérialisation
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventType", event.getEventType());
        eventMap.put("registrationId", event.getRegistrationId());
        eventMap.put("userId", event.getUserId());
        eventMap.put("userEmail", event.getUserEmail());
        eventMap.put("userName", event.getUserName());
        eventMap.put("thesisTitle", event.getThesisTitle());
        eventMap.put("status", event.getStatus());
        eventMap.put("timestamp", event.getTimestamp().toString());

        sendMessage("registration-events", eventMap);
    }
}

//v2
//package ma.spring.registrationservice.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaProducerService {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    public void sendMessage(String topic, Object message) {
//        try {
//            log.info("Envoi message Kafka topic {}: {}", topic, message);
//            kafkaTemplate.send(topic, message).whenComplete((result, ex) -> {
//                if (ex == null) {
//                    log.info("Message envoyé avec succès topic {}: offset {}",
//                            topic, result.getRecordMetadata().offset());
//                } else {
//                    log.error("Échec envoi message topic {}: {}", topic, ex.getMessage());
//                }
//            });
//        } catch (Exception e) {
//            log.error("Erreur critique envoi Kafka topic {}: {}", topic, e.getMessage());
//        }
//    }
//
//    // Méthode spécifique pour les événements d'inscription
//    public void sendRegistrationEvent(Object event) {
//        sendMessage("registration-events", event);
//    }
//}

//v1
//package ma.spring.registrationservice.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaProducerService {
//
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    public void sendMessage(String topic, Object message) {
//        kafkaTemplate.send(topic, message);
//        System.out.println("Message envoyé à Kafka: " + message);
//    }
//}
