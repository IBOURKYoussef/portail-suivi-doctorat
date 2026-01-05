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
public class RegistrationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long campaignId;
    private String campaignName;
    private RegistrationType type;
    private RegistrationStatus status;
    private String thesisTitle;
    private String thesisDescription;
    private String researchField;
    private Long directorId;
    private String directorName;
    private Long coDirectorId;
    private String coDirectorName;
    private String laboratory;
    private List<String> documentPaths;
    private String directorComment;
    private String adminComment;
    private LocalDateTime directorReviewDate;
    private LocalDateTime adminReviewDate;
    private Integer academicYear;
    private Integer doctoralYear;
    private LocalDateTime submissionDate;
    private LocalDateTime approvalDate;
    private LocalDateTime createdAt;
    
    // Nouveaux champs pour candidature
    private String candidateName;
    private String programName;
    private String academicYearPeriod;
    private String previousEducation;
    private String institution;
    private Integer graduationYear;
    private String grade;
    private String phone;
    private String researchTitle;
    private String researchSummary;
    private String motivationLetter;
}
