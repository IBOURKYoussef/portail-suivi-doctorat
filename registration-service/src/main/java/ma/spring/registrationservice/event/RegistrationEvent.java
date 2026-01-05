package ma.spring.registrationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// ============= RegistrationEvent.java =============
/**
 * Événement envoyé via Kafka lors d'un changement de statut d'inscription
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationEvent {
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private String userName;
    private String eventType; // SUBMITTED, APPROVED_BY_DIRECTOR, REJECTED_BY_DIRECTOR, APPROVED_BY_ADMIN, etc.
    private String status;
    private String thesisTitle;
    private String directorName;
    private String comment;
    private LocalDateTime timestamp;
}
