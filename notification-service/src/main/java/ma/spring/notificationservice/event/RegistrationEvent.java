package ma.spring.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationEvent {
    private String eventType; // REGISTRATION_CREATED, REGISTRATION_APPROVED, etc.
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long directorId;
    private String directorEmail;
    private String directorName;
    private String status;
    private String comment;
    private LocalDateTime eventDate;
    private String thesisTitle; // Ajout de ce champ
}