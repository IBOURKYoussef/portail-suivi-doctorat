package ma.spring.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import ma.spring.notificationservice.dto.NotificationRequest;
import ma.spring.notificationservice.dto.NotificationResponse;
import ma.spring.notificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF')")
    public ResponseEntity<Map<String, String>> createNotification(
            @RequestBody NotificationRequest request) {

        notificationService.processNotification(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification créée et en cours de traitement");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DOCTORANT', 'CANDIDAT', 'DIRECTEUR_THESE')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            Pageable pageable) {

        Page<NotificationResponse> notifications =
                notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DOCTORANT', 'CANDIDAT', 'DIRECTEUR_THESE')")
    @GetMapping("/my")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable) {

        Page<NotificationResponse> notifications =
                notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DOCTORANT', 'CANDIDAT', 'DIRECTEUR_THESE')")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        long unreadCount = notificationService.countUnreadNotifications(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DOCTORANT', 'CANDIDAT', 'DIRECTEUR_THESE')")
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        notificationService.markAsRead(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marquée comme lue");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        return ResponseEntity.ok(health);
    }
}