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
public class CampaignEvent {
    private String eventType;
    private Long campaignId;
    private String campaignName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime eventDate;
}