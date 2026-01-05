package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ma.spring.defenseservice.model.*;
import java.time.LocalDateTime;
import java.util.List;
// ============= DefenseRequest.java =============
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseRequest {

    @NotBlank(message = "Le titre de la thèse est obligatoire")
    @Size(max = 500)
    private String thesisTitle;

    @NotBlank(message = "Le résumé est obligatoire")
    @Size(max = 2000)
    private String thesisAbstract;

    @NotBlank(message = "Le domaine de recherche est obligatoire")
    private String researchField;

    @NotBlank(message = "Le laboratoire est obligatoire")
    private String laboratory;

    @NotNull(message = "Le directeur de thèse est obligatoire")
    private Long directorId;

    private Long coDirectorId;

    @NotNull(message = "Le nombre de publications est obligatoire")
    @Min(value = 0)
    private Integer publicationsCount;

    @NotNull(message = "Le nombre de conférences est obligatoire")
    @Min(value = 0)
    private Integer conferencesCount;

    @NotNull(message = "Les heures de formation sont obligatoires")
    @Min(value = 0)
    private Integer trainingHours;

    @NotNull(message = "La date proposée est obligatoire")
    private LocalDateTime proposedDate;

    @NotNull(message = "L'année académique est obligatoire")
    private Integer academicYear;

    private List<String> documentPaths;
}
