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
public class CampaignRequest {

    @NotBlank(message = "Le nom de la campagne est obligatoire")
    private String name;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotNull(message = "Le type est obligatoire")
    private RegistrationType type;

    @NotNull(message = "L'année académique est obligatoire")
    @Min(value = 2020, message = "Année invalide")
    private Integer academicYear;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;
}

