package ma.spring.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ma.spring.userservice.model.UserRole;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private UserRole role = UserRole.CANDIDAT; // Rôle par défaut

    // Champs spécifiques selon le rôle
    private String studentId; // Pour CANDIDAT/DOCTORANT
    private String laboratoire; // Pour DIRECTEUR_THESE
    private String grade; // Pour DIRECTEUR_THESE
}