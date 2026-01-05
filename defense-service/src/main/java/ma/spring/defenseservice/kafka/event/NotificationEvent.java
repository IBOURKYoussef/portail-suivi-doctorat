package ma.spring.defenseservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String type; // EMAIL, SMS, IN_APP
    private String templateCode; // DEFENSE_SUBMITTED, JURY_APPOINTED, etc.
    private Map<String, String> recipients; // email/phone -> name
    private Map<String, Object> parameters; // Variables du template
    private String priority; // HIGH, MEDIUM, LOW
}

