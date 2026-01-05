package ma.spring.userservice.dto;

// DTOs (Data Transfer Objects) pour les requêtes et réponses
@lombok.Getter @lombok.Setter
public class LoginRequest {
    private String username;
    private String password;
}

