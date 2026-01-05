package ma.spring.defenseservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ============= Defense.java =============
@Entity
@Table(name = "defenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Defense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorantId; // ID du doctorant

    @Column(nullable = false)
    private Long directorId; // ID du directeur de thèse

    private Long coDirectorId; // Co-directeur (optionnel)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefenseStatus status;

    // Informations sur la thèse
    @Column(nullable = false, length = 500)
    private String thesisTitle;

    @Column(length = 2000)
    private String thesisAbstract;

    @Column(nullable = false)
    private String researchField;

    private String laboratory;

    // Prérequis
    @Column(nullable = false)
    private Integer publicationsCount = 0; // Nombre d'articles

    @Column(nullable = false)
    private Integer conferencesCount = 0; // Nombre de conférences

    @Column(nullable = false)
    private Integer trainingHours = 0; // Heures de formation

    // Documents
    @ElementCollection
    @CollectionTable(name = "defense_documents",
            joinColumns = @JoinColumn(name = "defense_id"))
    @Column(name = "document_path")
    private List<String> documentPaths = new ArrayList<>();

    // Jury
    @OneToMany(mappedBy = "defense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JuryMember> juryMembers = new ArrayList<>();

    // Rapporteurs
    @OneToMany(mappedBy = "defense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rapporteur> rapporteurs = new ArrayList<>();

    // Dates et lieu
    private LocalDateTime proposedDate; // Date proposée par le doctorant

    private LocalDateTime defenseDate; // Date finale de soutenance

    private String defenseLocation; // Lieu de soutenance

    private String defenseRoom; // Salle

    // Résultat
    @Enumerated(EnumType.STRING)
    private DefenseResult result;

    @Column(length = 2000)
    private String juryRemarks; // Remarques du jury

    private String mention; // Mention (Très Honorable, Honorable, etc.)

    // Commentaires et avis
    @Column(length = 1000)
    private String directorComment;

    @Column(length = 1000)
    private String adminComment;

    private LocalDateTime directorApprovalDate;

    private LocalDateTime adminApprovalDate;

    private LocalDateTime authorizationDate; // Date d'autorisation de soutenance

    // Année académique
    @Column(nullable = false)
    private Integer academicYear;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        submissionDate = LocalDateTime.now();
        status = DefenseStatus.SUBMITTED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean meetsPrerequisites() {
        return publicationsCount >= 2 &&
                conferencesCount >= 2 &&
                trainingHours >= 200;
    }
}

