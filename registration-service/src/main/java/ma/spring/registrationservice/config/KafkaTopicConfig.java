package ma.spring.registrationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration des Topics Kafka
 * Activée seulement si Kafka est disponible
 */
@Configuration
@ConditionalOnProperty(
        name = "spring.kafka.enabled",
        havingValue = "true",
        matchIfMissing = true  // Par défaut activé
)
@Slf4j
public class KafkaTopicConfig {

    public static final String REGISTRATION_EVENTS_TOPIC = "registration-events";
    public static final String NOTIFICATION_TOPIC = "notifications";

    @Bean
    public NewTopic registrationEventsTopic() {
        return TopicBuilder.name("registration-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        log.info("📢 Creating Kafka topic: {}", NOTIFICATION_TOPIC);
        return TopicBuilder
                .name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

//package ma.spring.registrationservice.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
///**
// * Configuration des Topics Kafka
// */
//@Configuration
//public class KafkaTopicConfig {
//
//    public static final String REGISTRATION_EVENTS_TOPIC = "registration-events";
//    public static final String NOTIFICATION_TOPIC = "notifications";
//
//    @Bean
//    public NewTopic registrationEventsTopic() {
//        return TopicBuilder
//                .name(REGISTRATION_EVENTS_TOPIC)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//    @Bean
//    public NewTopic notificationTopic() {
//        return TopicBuilder
//                .name(NOTIFICATION_TOPIC)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
//
//
//    @Bean
//    public NewTopic testTopic() {
//        return TopicBuilder
//                .name("test-topic")
//                .partitions(1)
//                .replicas(1)
//                .build();
//    }
//
//}
