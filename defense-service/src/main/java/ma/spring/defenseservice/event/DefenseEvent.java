package ma.spring.defenseservice.event;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.spring.defenseservice.model.DefenseStatus;
import ma.spring.defenseservice.model.DefenseResult;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseEvent {
    private Long defenseId;
    private Long doctorantId;
    private String doctorantEmail;
    private String directorEmail;
    private String adminEmail;
    private String eventType; // DEFENSE_SUBMITTED, JURY_PROPOSED, DEFENSE_AUTHORIZED, etc.
    private DefenseStatus status;
    private DefenseResult result;
    private String thesisTitle;
    private LocalDateTime defenseDate;
    private String defenseLocation;
    private String message;
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}