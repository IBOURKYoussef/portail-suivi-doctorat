package ma.spring.registrationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.config.KafkaTopicConfig;
import ma.spring.registrationservice.dto.RegistrationRequest;
import ma.spring.registrationservice.dto.RegistrationResponse;
import ma.spring.registrationservice.dto.ReviewRequest;
import ma.spring.registrationservice.event.RegistrationEvent;
import ma.spring.registrationservice.service.KafkaProducerService;
import ma.spring.registrationservice.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;
    private final KafkaProducerService producerService; // Doit être final pour l'injection

    /**
     * Test d'envoi de notification
     */
    @PostMapping("/send-test-notification")
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @RequestHeader("X-User-Id") Long userId
    ) {
        try {
            log.info("Envoi notification de test avec RegistrationEvent...");

            // Utiliser l'objet RegistrationEvent structuré
            RegistrationEvent testEvent = RegistrationEvent.builder()
                    .eventType("REGISTRATION_CREATED")
                    .registrationId(12345L)
                    .userId(userId)
                    .userEmail("ibourkyoussef149@gmail.com")
                    .userName("Youssef Ibourk")
                    .thesisTitle("Test de notification Kafka")
                    .status("PENDING")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Envoyer l'objet RegistrationEvent
            producerService.sendRegistrationEvent(testEvent);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification de test envoyée à: " + testEvent.getUserEmail());
            response.put("status", "SUCCESS");

            log.info("Notification de test envoyée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de test", e);

            Map<String, String> error = new HashMap<>();
            error.put("error", "Échec envoi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Ancienne méthode de test - CORRIGÉE pour envoyer un objet
     */
    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        log.info("Envoi message structuré: {}", message);

        // Créer un objet structuré au lieu d'envoyer la String brute
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "TEST_MESSAGE");
        event.put("message", message);
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("userEmail", "test@univ.edu");

        producerService.sendMessage("registration-events", event);
        return "Message structuré envoyé !";
    }



    /**
     * Créer une nouvelle inscription
     * POST /api/registrations
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRegistration(
            @Valid @RequestBody RegistrationRequest request,
            @RequestHeader("X-User-Id") Long userId // ID de l'utilisateur depuis le JWT
    ) {
        log.info("Creating registration for user: {}", userId);

        try {
            RegistrationResponse response = registrationService.createRegistration(request, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Inscription créée avec succès");
            result.put("registration", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating registration", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupérer une inscription par ID
     * GET /api/registrations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponse> getRegistrationById(@PathVariable Long id) {
        log.info("Getting registration: {}", id);

        try {
            RegistrationResponse response = registrationService.getRegistrationById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting registration", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer les inscriptions d'un utilisateur
     * GET /api/registrations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RegistrationResponse>> getUserRegistrations(@PathVariable Long userId) {
        log.info("Getting registrations for user: {}", userId);

        List<RegistrationResponse> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Récupérer les inscriptions d'un candidat
     * GET /api/registrations/candidate/{candidateId}
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByCandidate(@PathVariable Long candidateId) {
        log.info("Getting registrations for candidate: {}", candidateId);

        List<RegistrationResponse> registrations = registrationService.getUserRegistrations(candidateId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Récupérer ses propres inscriptions
     * GET /api/registrations/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Getting my registrations for user: {}", userId);

        List<RegistrationResponse> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Valider une inscription par le directeur
     * PUT /api/registrations/{id}/review/director
     */
    @PutMapping("/{id}/review/director")
    public ResponseEntity<Map<String, Object>> reviewByDirector(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") Long directorId
    ) {
        log.info("Director {} reviewing registration {}", directorId, id);

        try {
            RegistrationResponse response = registrationService.reviewByDirector(id, directorId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", request.getApproved()
                    ? "Inscription approuvée"
                    : "Inscription rejetée");
            result.put("registration", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error reviewing registration", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valider une inscription par l'administration
     * PUT /api/registrations/{id}/review/admin
     */
    @PutMapping("/{id}/review/admin")
    public ResponseEntity<Map<String, Object>> reviewByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request
    ) {
        log.info("Admin reviewing registration {}", id);

        try {
            RegistrationResponse response = registrationService.reviewByAdmin(id, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", request.getApproved()
                    ? "Inscription approuvée définitivement"
                    : "Inscription rejetée");
            result.put("registration", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error reviewing registration", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupérer les inscriptions en attente pour un directeur
     * GET /api/registrations/pending/director
     */
    @GetMapping("/pending/director")
    public ResponseEntity<Page<RegistrationResponse>> getPendingForDirector(
            @RequestHeader("X-User-Id") Long directorId,
            Pageable pageable
    ) {
        log.info("Getting pending registrations for director: {}", directorId);

        Page<RegistrationResponse> registrations =
                registrationService.getPendingRegistrationsForDirector(directorId, pageable);

        return ResponseEntity.ok(registrations);
    }

    /**
     * Health check
     * GET /api/registrations/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "registration-service");
        return ResponseEntity.ok(health);
    }
}
