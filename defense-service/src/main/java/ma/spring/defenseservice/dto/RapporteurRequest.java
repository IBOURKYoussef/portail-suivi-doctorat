package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RapporteurRequest {
    private Long professorId;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "L'établissement est obligatoire")
    private String institution;

    @NotBlank(message = "Le grade est obligatoire")
    private String grade;

    @Email(message = "Email invalide")
    private String email;
}
