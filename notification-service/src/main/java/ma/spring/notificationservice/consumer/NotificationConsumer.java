package ma.spring.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.notificationservice.service.NotificationService;
import ma.spring.notificationservice.dto.NotificationRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "registration-events", groupId = "notification-service")
    public void consumeRegistrationEvent(@Payload Map<String, Object> event) {
        log.info("=== ÉVÉNEMENT REGISTRATION KAFKA REÇU ===");
        log.info("Contenu: {}", event);

        try {
            processRegistrationEvent(event);
            log.info("=== NOTIFICATION REGISTRATION TRAITÉE AVEC SUCCÈS ===");
        } catch (Exception e) {
            log.error("❌ ERREUR traitement événement Registration: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "defense-events", groupId = "notification-service")
    public void consumeDefenseEvent(@Payload Map<String, Object> event) {
        log.info("=== ÉVÉNEMENT DEFENSE KAFKA REÇU ===");
        log.info("Contenu: {}", event);

        try {
            processDefenseEvent(event);
            log.info("=== NOTIFICATION DEFENSE TRAITÉE AVEC SUCCÈS ===");
        } catch (Exception e) {
            log.error("❌ ERREUR traitement événement Defense: {}", e.getMessage(), e);
        }
    }

    private void processRegistrationEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String userEmail = (String) event.get("userEmail");
        String userName = (String) event.get("userName");
        String thesisTitle = (String) event.get("thesisTitle");
        Long userId = event.get("userId") != null ?
                ((Number) event.get("userId")).longValue() : 1L;

        log.info("Traitement événement: {} pour {}", eventType, userEmail);

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .userEmail(userEmail != null ? userEmail : "default@example.com")
                .userName(userName != null ? userName : "Utilisateur")
                .type(mapEventTypeToNotificationType(eventType))
                .title(generateTitle(eventType))
                .message(generateMessage(eventType, userName, thesisTitle))
                .data(event)
                .build();

        notificationService.processNotification(request);
    }

    private void processDefenseEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String doctorantEmail = (String) event.get("doctorantEmail");
        String doctorantName = (String) event.get("doctorantName");
        String directorEmail = (String) event.get("directorEmail");
        String directorName = (String) event.get("directorName");
        String thesisTitle = (String) event.get("thesisTitle");
        Long doctorantId = event.get("doctorantId") != null ?
                ((Number) event.get("doctorantId")).longValue() : null;
        Long directorId = event.get("directorId") != null ?
                ((Number) event.get("directorId")).longValue() : null;

        log.info("Traitement événement Defense: {} pour {}", eventType, doctorantEmail);

        // Notification pour le doctorant
        if (doctorantId != null && doctorantEmail != null) {
            NotificationRequest doctorantRequest = NotificationRequest.builder()
                    .userId(doctorantId)
                    .userEmail(doctorantEmail)
                    .userName(doctorantName != null ? doctorantName : "Doctorant")
                    .type(mapDefenseEventToNotificationType(eventType))
                    .title(generateDefenseTitle(eventType, false))
                    .message(generateDefenseMessage(eventType, doctorantName, thesisTitle, false))
                    .data(event)
                    .build();
            notificationService.processNotification(doctorantRequest);
        }

        // Notification pour le directeur (pour certains événements)
        if (directorId != null && directorEmail != null && shouldNotifyDirector(eventType)) {
            NotificationRequest directorRequest = NotificationRequest.builder()
                    .userId(directorId)
                    .userEmail(directorEmail)
                    .userName(directorName != null ? directorName : "Directeur")
                    .type(mapDefenseEventToNotificationType(eventType))
                    .title(generateDefenseTitle(eventType, true))
                    .message(generateDefenseMessage(eventType, doctorantName, thesisTitle, true))
                    .data(event)
                    .build();
            notificationService.processNotification(directorRequest);
        }
    }

    private boolean shouldNotifyDirector(String eventType) {
        return "DEFENSE_SUBMITTED".equals(eventType) ||
               "DEFENSE_SCHEDULED".equals(eventType) ||
               "DEFENSE_COMPLETED".equals(eventType);
    }

    private ma.spring.notificationservice.model.NotificationType mapEventTypeToNotificationType(String eventType) {
        if (eventType == null) {
            return ma.spring.notificationservice.model.NotificationType.SYSTEM_ALERT;
        }

        switch (eventType) {
            case "REGISTRATION_CREATED":
            case "SUBMITTED":
                return ma.spring.notificationservice.model.NotificationType.REGISTRATION_SUBMITTED;
            case "APPROVED_BY_DIRECTOR":
                return ma.spring.notificationservice.model.NotificationType.REGISTRATION_APPROVED_BY_DIRECTOR;
            case "REJECTED_BY_DIRECTOR":
                return ma.spring.notificationservice.model.NotificationType.REGISTRATION_REJECTED_BY_DIRECTOR;
            case "APPROVED_BY_ADMIN":
                return ma.spring.notificationservice.model.NotificationType.REGISTRATION_APPROVED_BY_ADMIN;
            case "REJECTED_BY_ADMIN":
                return ma.spring.notificationservice.model.NotificationType.REGISTRATION_REJECTED_BY_ADMIN;
            default:
                return ma.spring.notificationservice.model.NotificationType.SYSTEM_ALERT;
        }
    }

    private ma.spring.notificationservice.model.NotificationType mapDefenseEventToNotificationType(String eventType) {
        if (eventType == null) {
            return ma.spring.notificationservice.model.NotificationType.SYSTEM_ALERT;
        }

        switch (eventType) {
            case "DEFENSE_SUBMITTED":
                return ma.spring.notificationservice.model.NotificationType.DEFENSE_SUBMITTED;
            case "PREREQUISITES_VALIDATED":
            case "DEFENSE_AUTHORIZED":
                return ma.spring.notificationservice.model.NotificationType.DEFENSE_APPROVED;
            case "PREREQUISITES_REJECTED":
            case "DEFENSE_REJECTED":
                return ma.spring.notificationservice.model.NotificationType.DEFENSE_REJECTED;
            case "DEFENSE_SCHEDULED":
                return ma.spring.notificationservice.model.NotificationType.DEFENSE_SCHEDULED;
            case "DEFENSE_COMPLETED":
                return ma.spring.notificationservice.model.NotificationType.DEFENSE_COMPLETED;
            default:
                return ma.spring.notificationservice.model.NotificationType.SYSTEM_ALERT;
        }
    }

    private String generateTitle(String eventType) {
        if (eventType == null) {
            return "Notification du Portail Doctorat";
        }

        switch (eventType) {
            case "REGISTRATION_CREATED":
                return "Inscription soumise - Portail Doctorat";
            case "APPROVED_BY_DIRECTOR":
                return "Inscription approuvée par le directeur";
            case "APPROVED_BY_ADMIN":
                return "Félicitations - Votre inscription est validée";
            default:
                return "Notification du Portail Doctorat";
        }
    }

    private String generateDefenseTitle(String eventType, boolean isForDirector) {
        if (eventType == null) {
            return "Notification - Soutenance de thèse";
        }

        switch (eventType) {
            case "DEFENSE_SUBMITTED":
                return isForDirector ? 
                    "Nouvelle demande de soutenance" : 
                    "Demande de soutenance soumise";
            case "PREREQUISITES_VALIDATED":
                return "Prérequis validés";
            case "DEFENSE_AUTHORIZED":
                return "Soutenance autorisée";
            case "DEFENSE_SCHEDULED":
                return isForDirector ?
                    "Soutenance programmée" :
                    "Votre soutenance est programmée";
            case "DEFENSE_COMPLETED":
                return "Soutenance terminée";
            default:
                return "Notification - Soutenance de thèse";
        }
    }

    private String generateMessage(String eventType, String userName, String thesisTitle) {
        String safeUserName = userName != null ? userName : "Cher doctorant";
        String safeThesisTitle = thesisTitle != null ? thesisTitle : "Votre sujet de thèse";

        switch (eventType) {
            case "REGISTRATION_CREATED":
                return String.format(
                        "Bonjour %s,<br>Votre inscription pour la thèse '%s' a été soumise avec succès.",
                        safeUserName, safeThesisTitle
                );
            case "APPROVED_BY_DIRECTOR":
                return String.format(
                        "Bonjour %s,<br>Votre inscription a été approuvée par votre directeur de thèse.",
                        safeUserName
                );
            case "APPROVED_BY_ADMIN":
                return String.format(
                        "Bonjour %s,<br>Félicitations ! Votre inscription au doctorat a été validée définitivement.",
                        safeUserName
                );
            default:
                return String.format(
                        "Bonjour %s,<br>Vous avez une nouvelle notification concernant votre inscription.",
                        safeUserName
                );
        }
    }

    private String generateDefenseMessage(String eventType, String userName, String thesisTitle, boolean isForDirector) {
        String safeUserName = userName != null ? userName : "Cher doctorant";
        String safeThesisTitle = thesisTitle != null ? thesisTitle : "Votre sujet de thèse";

        switch (eventType) {
            case "DEFENSE_SUBMITTED":
                return isForDirector ?
                    String.format("Bonjour,<br>Une nouvelle demande de soutenance a été soumise par %s pour la thèse '%s'.", 
                        safeUserName, safeThesisTitle) :
                    String.format("Bonjour %s,<br>Votre demande de soutenance pour la thèse '%s' a été soumise avec succès.",
                        safeUserName, safeThesisTitle);
            case "PREREQUISITES_VALIDATED":
                return String.format("Bonjour %s,<br>Les prérequis pour votre soutenance ont été validés.", safeUserName);
            case "DEFENSE_AUTHORIZED":
                return String.format("Bonjour %s,<br>Votre soutenance de thèse a été autorisée !", safeUserName);
            case "DEFENSE_SCHEDULED":
                return isForDirector ?
                    String.format("Bonjour,<br>La soutenance de %s pour la thèse '%s' a été programmée.", 
                        safeUserName, safeThesisTitle) :
                    String.format("Bonjour %s,<br>Votre soutenance de thèse a été programmée. Consultez les détails dans votre tableau de bord.",
                        safeUserName);
            case "DEFENSE_COMPLETED":
                return isForDirector ?
                    String.format("Bonjour,<br>La soutenance de %s est terminée. Le résultat a été enregistré.", safeUserName) :
                    String.format("Bonjour %s,<br>Félicitations ! Votre soutenance est terminée. Le résultat est disponible dans votre dossier.", safeUserName);
            default:
                return String.format("Bonjour %s,<br>Vous avez une nouvelle notification concernant votre soutenance.", safeUserName);
        }
    }
}
