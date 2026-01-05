package ma.spring.registrationservice.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import ma.spring.registrationservice.model.RegistrationStatus;
import ma.spring.registrationservice.model.RegistrationType;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private String name;
    private String description;
    private RegistrationType type;
    private Integer academicYear;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private Long createdBy;
    private String createdByName;
}
