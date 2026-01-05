package ma.spring.registrationservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ma.spring.registrationservice.model.RegistrationStatus;
import ma.spring.registrationservice.model.RegistrationType;
import java.time.LocalDateTime;
import java.util.List;

// ============= RegistrationRequest.java =============
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    // Champs communs
    private Long candidateId;
    private String candidateName;
    
    // Champs pour inscription doctorale (doctorant)
    private Long campaignId;
    private RegistrationType type;
    
    @Size(max = 500, message = "Le titre ne peut pas dépasser 500 caractères")
    private String thesisTitle;
    
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String thesisDescription;
    
    private String researchField;
    private Long directorId;
    private Long coDirectorId;
    private String laboratory;
    private Integer doctoralYear;
    
    // Champs pour candidature (candidat)
    private String programName;
    private String academicYear;
    private String previousEducation;
    private String institution;
    private Integer graduationYear;
    private String grade;
    private String phone;
    private String researchTitle;
    private String researchSummary;
    private String motivationLetter;
    
    // Paths des documents uploadés
    private List<String> documentPaths;
}
