package ma.spring.notificationservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.notificationservice.dto.NotificationRequest;
import ma.spring.notificationservice.model.NotificationType;
import ma.spring.notificationservice.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    private static final String REGISTRATION_SERVICE_URL = "http://localhost:8084";
    private static final String DEFENSE_SERVICE_URL = "http://localhost:8083";

    // Vérifier les campagnes proches de clôture chaque jour à 9h
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkCampaignEndingSoon() {
        log.info("=== VÉRIFICATION DES CAMPAGNES PROCHES DE CLÔTURE ===");
        
        try {
            // Récupérer les campagnes actives
            String url = REGISTRATION_SERVICE_URL + "/api/campaigns/active";
            Map<String, Object>[] campaigns = restTemplate.getForObject(url, Map[].class);

            if (campaigns == null || campaigns.length == 0) {
                log.info("Aucune campagne active à vérifier");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysLater = now.plusDays(3);

            for (Map<String, Object> campaign : campaigns) {
                String endDateStr = (String) campaign.get("endDate");
                LocalDateTime endDate = LocalDateTime.parse(endDateStr);

                // Si la campagne se termine dans moins de 3 jours
                if (endDate.isAfter(now) && endDate.isBefore(threeDaysLater)) {
                    sendCampaignEndingReminder(campaign);
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de la vérification des campagnes: {}", e.getMessage());
        }
    }

    // Vérifier les documents manquants chaque jour à 10h
    @Scheduled(cron = "0 0 10 * * ?")
    public void checkMissingDocuments() {
        log.info("=== VÉRIFICATION DES DOCUMENTS MANQUANTS ===");
        
        try {
            // Récupérer les soutenances en attente de validation
            String url = DEFENSE_SERVICE_URL + "/api/defenses/admin/pending";
            Map<String, Object>[] defenses = restTemplate.getForObject(url, Map[].class);

            if (defenses == null || defenses.length == 0) {
                log.info("Aucune soutenance en attente");
                return;
            }

            for (Map<String, Object> defense : defenses) {
                Long defenseId = ((Number) defense.get("id")).longValue();
                Long doctorantId = ((Number) defense.get("doctorantId")).longValue();
                String thesisTitle = (String) defense.get("thesisTitle");

                // Vérifier si les documents requis sont présents
                String docUrl = DEFENSE_SERVICE_URL + "/api/documents/entity/" + defenseId + 
                               "/validate?types=MANUSCRIPT,PLAGIARISM_REPORT";
                
                try {
                    Boolean hasDocuments = restTemplate.getForObject(docUrl, Boolean.class);
                    
                    if (Boolean.FALSE.equals(hasDocuments)) {
                        sendMissingDocumentsReminder(doctorantId, thesisTitle);
                    }
                } catch (Exception e) {
                    log.warn("Impossible de vérifier les documents pour la soutenance {}", defenseId);
                }
            }

        } catch (Exception e) {
            log.error("Erreur lors de la vérification des documents: {}", e.getMessage());
        }
    }

    // Rappel des soutenances à venir (3 jours avant) chaque jour à 8h
    @Scheduled(cron = "0 0 8 * * ?")
    public void remindUpcomingDefenses() {
        log.info("=== RAPPEL DES SOUTENANCES À VENIR ===");
        
        try {
            LocalDateTime start = LocalDateTime.now().plusDays(2);
            LocalDateTime end = LocalDateTime.now().plusDays(4);

            String url = DEFENSE_SERVICE_URL + "/api/defenses/scheduled?start=" + 
                        start.format(DateTimeFormatter.ISO_DATE_TIME) + 
                        "&end=" + end.format(DateTimeFormatter.ISO_DATE_TIME);

            Map<String, Object>[] defenses = restTemplate.getForObject(url, Map[].class);

            if (defenses == null || defenses.length == 0) {
                log.info("Aucune soutenance dans les 3 prochains jours");
                return;
            }

            for (Map<String, Object> defense : defenses) {
                sendDefenseReminder(defense);
            }

        } catch (Exception e) {
            log.error("Erreur lors du rappel des soutenances: {}", e.getMessage());
        }
    }

    // Nettoyer les anciennes notifications (tous les lundis à 2h du matin)
    @Scheduled(cron = "0 0 2 * * MON")
    public void cleanOldNotifications() {
        log.info("=== NETTOYAGE DES ANCIENNES NOTIFICATIONS ===");
        
        try {
            // Cette méthode devrait être implémentée dans NotificationService
            // pour supprimer les notifications de plus de 90 jours
            log.info("Nettoyage des notifications de plus de 90 jours");
            
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage: {}", e.getMessage());
        }
    }

    private void sendCampaignEndingReminder(Map<String, Object> campaign) {
        try {
            String campaignName = (String) campaign.get("name");
            String endDateStr = (String) campaign.get("endDate");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);
            
            long daysRemaining = java.time.Duration.between(LocalDateTime.now(), endDate).toDays();

            // Note: Vous devrez récupérer la liste des candidats concernés
            // Pour l'instant, log uniquement
            log.info("Campagne '{}' se termine dans {} jour(s)", campaignName, daysRemaining);

            // TODO: Récupérer les utilisateurs concernés et envoyer les notifications
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du rappel de campagne", e);
        }
    }

    private void sendMissingDocumentsReminder(Long userId, String thesisTitle) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .type(NotificationType.REMINDER_DOCUMENTS_MISSING)
                    .title("⚠️ Documents manquants pour votre soutenance")
                    .message(String.format(
                        "<div style='font-family: Arial, sans-serif;'>" +
                        "<h3>Documents requis manquants</h3>" +
                        "<p>Votre dossier de soutenance <strong>%s</strong> est incomplet.</p>" +
                        "<p>Documents requis :</p>" +
                        "<ul>" +
                        "<li>Manuscrit de thèse</li>" +
                        "<li>Rapport anti-plagiat</li>" +
                        "</ul>" +
                        "<p>Veuillez les soumettre dans les plus brefs délais.</p>" +
                        "</div>",
                        thesisTitle
                    ))
                    .build();

            notificationService.processNotification(request);
            log.info("Rappel documents manquants envoyé à l'utilisateur {}", userId);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du rappel de documents", e);
        }
    }

    private void sendDefenseReminder(Map<String, Object> defense) {
        try {
            Long doctorantId = ((Number) defense.get("doctorantId")).longValue();
            Long directorId = ((Number) defense.get("directorId")).longValue();
            String thesisTitle = (String) defense.get("thesisTitle");
            String defenseDateStr = (String) defense.get("defenseDate");
            String location = (String) defense.get("defenseLocation");
            String room = (String) defense.get("defenseRoom");

            LocalDateTime defenseDate = LocalDateTime.parse(defenseDateStr);
            String dateFormatted = defenseDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

            String message = String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h3>📅 Rappel de soutenance dans 3 jours</h3>" +
                "<p><strong>Thèse :</strong> %s</p>" +
                "<p><strong>Date :</strong> %s</p>" +
                "<p><strong>Lieu :</strong> %s - Salle %s</p>" +
                "<p>Veuillez vous assurer d'avoir préparé tous les documents nécessaires.</p>" +
                "</div>",
                thesisTitle, dateFormatted, location, room
            );

            // Notification au doctorant
            NotificationRequest doctorantNotif = NotificationRequest.builder()
                    .userId(doctorantId)
                    .type(NotificationType.DEFENSE_SCHEDULED)
                    .title("📅 Rappel : Soutenance dans 3 jours")
                    .message(message)
                    .build();
            notificationService.processNotification(doctorantNotif);

            // Notification au directeur
            NotificationRequest directorNotif = NotificationRequest.builder()
                    .userId(directorId)
                    .type(NotificationType.DEFENSE_SCHEDULED)
                    .title("📅 Rappel : Soutenance de votre doctorant dans 3 jours")
                    .message(message)
                    .build();
            notificationService.processNotification(directorNotif);

            log.info("Rappel de soutenance envoyé pour la thèse: {}", thesisTitle);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du rappel de soutenance", e);
        }
    }
}
