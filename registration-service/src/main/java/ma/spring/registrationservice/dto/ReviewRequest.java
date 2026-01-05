package ma.spring.registrationservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ma.spring.registrationservice.model.RegistrationStatus;
import ma.spring.registrationservice.model.RegistrationType;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "La décision est obligatoire")
    private Boolean approved;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String comment;
}

