package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuryProposalRequest {

    @NotNull(message = "Le président du jury est obligatoire")
    private JuryMemberRequest president;

    @NotEmpty(message = "Au moins un examinateur est requis")
    private List<JuryMemberRequest> examiners;

    @NotEmpty(message = "Au moins deux rapporteurs sont requis")
    @Size(min = 2, message = "Au moins 2 rapporteurs sont requis")
    private List<RapporteurRequest> rapporteurs;
}
