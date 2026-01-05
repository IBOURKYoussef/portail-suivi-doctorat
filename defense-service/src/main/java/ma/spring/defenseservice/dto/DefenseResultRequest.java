package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ma.spring.defenseservice.model.DefenseResult;

// ============= DefenseResultRequest.java =============
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseResultRequest {

    @NotNull(message = "Le résultat est obligatoire")
    private DefenseResult result;

    @Size(max = 2000)
    private String juryRemarks;

    private String mention; // Très Honorable, Honorable, etc.
}


