package ma.spring.defenseservice.dto;

import lombok.*;
import ma.spring.defenseservice.model.MemberStatus;
import ma.spring.defenseservice.model.ReportOpinion;

import java.time.LocalDateTime;

// ============= RapporteurDTO.java =============
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapporteurDTO {
    private Long id;
    private Long professorId;
    private String name;
    private String institution;
    private String grade;
    private String email;
    private MemberStatus status;
    private String reportFilePath;
    private ReportOpinion opinion;
    private LocalDateTime reportSubmissionDate;
}

