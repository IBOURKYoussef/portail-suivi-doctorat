package ma.spring.defenseservice.dto;

import lombok.*;
import ma.spring.defenseservice.model.JuryRole;
import ma.spring.defenseservice.model.MemberStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JuryMemberDTO {
    private Long id;
    private Long professorId;
    private String name;
    private String institution;
    private String grade;
    private JuryRole role;
    private String email;
    private MemberStatus status;
}
