package ma.spring.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.notificationservice.service.EmailService;
import ma.spring.notificationservice.service.NotificationService;
import ma.spring.notificationservice.dto.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TestController {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String email) {
        try {
            log.info("Test d'envoi email à: {}", email);

            String subject = "Test Email - Portail Doctorat";
            String body = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Portail Doctorat - Test Email</h1>
                        </div>
                        <div class="content">
                            <p>Ceci est un email de test.</p>
                            <p>Si vous recevez ce message, la configuration SMTP fonctionne correctement.</p>
                            <p>✅ Test réussi !</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

            emailService.sendEmail(email, subject, body);

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Email de test envoyé à: " + email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Échec test email: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Échec envoi: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/notification")
    public ResponseEntity<Map<String, String>> testNotification(@RequestParam String email) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(1L)
                    .userEmail(email)
                    .userName("Utilisateur Test")
                    .type(ma.spring.notificationservice.model.NotificationType.REGISTRATION_SUBMITTED)
                    .title("Test de Notification")
                    .message("Ceci est un test de notification manuel.")
                    .data(new HashMap<>())
                    .build();

            notificationService.processNotification(request);

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Notification de test traitée pour: " + email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Échec test notification: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Échec notification: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }
}