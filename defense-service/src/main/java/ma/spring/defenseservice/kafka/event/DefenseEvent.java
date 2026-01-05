package ma.spring.defenseservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.spring.defenseservice.model.DefenseResult;
import ma.spring.defenseservice.model.DefenseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseEvent {
    private Long defenseId;
    private Long doctorantId;
    private String doctorantName;
    private String doctorantEmail;
    private Long directorId;
    private String directorEmail;
    private DefenseStatus status;
    private DefenseResult result;
    private LocalDate defenseDate;
    private LocalTime defenseTime;
    private String defenseLocation;
    private String eventType; // DEFENSE_CREATED, STATUS_CHANGED, JURY_COMPOSED, etc.
    private LocalDateTime timestamp;
    private Object payload; // Données supplémentaires
}
