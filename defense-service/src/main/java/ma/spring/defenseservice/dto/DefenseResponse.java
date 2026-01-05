package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ma.spring.defenseservice.model.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseResponse {
    private Long id;
    private Long doctorantId;
    private String doctorantName;
    private Long directorId;
    private String directorName;
    private Long coDirectorId;
    private String coDirectorName;
    private DefenseStatus status;
    private String thesisTitle;
    private String thesisAbstract;
    private String researchField;
    private String laboratory;
    private Integer publicationsCount;
    private Integer conferencesCount;
    private Integer trainingHours;
    private Boolean meetsPrerequisites;
    private List<String> documentPaths;
    private List<JuryMemberDTO> juryMembers;
    private List<RapporteurDTO> rapporteurs;
    private LocalDateTime proposedDate;
    private LocalDateTime defenseDate;
    private String defenseLocation;
    private String defenseRoom;
    private DefenseResult result;
    private String juryRemarks;
    private String mention;
    private String directorComment;
    private String adminComment;
    private LocalDateTime authorizationDate;
    private Integer academicYear;
    private LocalDateTime submissionDate;
    private LocalDateTime createdAt;
}

