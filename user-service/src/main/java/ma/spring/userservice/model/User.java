package ma.spring.userservice.model;

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
}