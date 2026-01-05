package ma.spring.userservice.model;

<<<<<<< HEAD
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Informations spécifiques pour les doctorants
    private String studentId; // CNE ou numéro d'étudiant

    // Informations spécifiques pour les directeurs de thèse
    private String laboratoire;
    private String grade; // Professeur, HDR, etc.

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
=======
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "users") // <---- FIX: avoid reserved keyword
@Data // Lombok pour générer getters, setters, etc.
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    private String role; // Ex: "DOCTORANT", "ADMINISTRATIF"
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
}