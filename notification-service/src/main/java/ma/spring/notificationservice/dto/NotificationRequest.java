package ma.spring.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.spring.notificationservice.model.NotificationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String userEmail;
    private String userName;
    private NotificationType type;
    private String title;
    private String message;
    private Object data; // Données supplémentaires pour le template
}