package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ma.spring.defenseservice.model.JuryRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuryMemberRequest {
    private Long professorId; // Si le professeur existe dans user-service

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "L'établissement est obligatoire")
    private String institution;

    @NotBlank(message = "Le grade est obligatoire")
    private String grade;

    @Email(message = "Email invalide")
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    private JuryRole role;
}
