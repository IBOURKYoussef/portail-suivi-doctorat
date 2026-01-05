package ma.spring.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.notificationservice.dto.NotificationRequest;
import ma.spring.notificationservice.dto.NotificationResponse;
import ma.spring.notificationservice.model.*;
import ma.spring.notificationservice.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateService templateService;

    @Async
    public void processNotification(NotificationRequest request) {
        try {
            log.info("Traitement de la notification: {} pour l'utilisateur {}",
                    request.getType(), request.getUserId());

            // Créer la notification
            Notification notification = createNotification(request);

            // Envoyer l'email
            sendEmailNotification(notification, request);

            // Marquer comme envoyé
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification traitée avec succès: {}", notification.getId());

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la notification: {}", e.getMessage());

            // Sauvegarder l'échec
            Notification failedNotification = createNotification(request);
            failedNotification.setStatus(NotificationStatus.FAILED);
            failedNotification.setErrorMessage(e.getMessage());
            notificationRepository.save(failedNotification);
        }
    }

    private Notification createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .status(NotificationStatus.PENDING)
                .email(request.getUserEmail())
                .build();

        return notificationRepository.save(notification);
    }

    private void sendEmailNotification(Notification notification, NotificationRequest request) {
        try {
            String emailContent = templateService.generateEmailContent(
                    request.getType().name(),
                    request.getData()
            );

            emailService.sendEmail(
                    request.getUserEmail(),
                    request.getTitle(),
                    emailContent
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getPendingNotifications() {
        return notificationRepository.findByStatusAndCreatedAtBefore(
                        NotificationStatus.PENDING,
                        LocalDateTime.now().minusMinutes(5)
                ).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findByStatusAndCreatedAtBefore(
                NotificationStatus.FAILED,
                LocalDateTime.now().minusHours(1)
        );

        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() < 3) {
                // Implémenter la logique de réessai
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setStatus(NotificationStatus.RETRY);
                notificationRepository.save(notification);
            }
        }
    }

    public long countUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> n.getStatus() != NotificationStatus.READ)
                .count();
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Accès non autorisé");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .errorMessage(notification.getErrorMessage())
                .build();
    }
}