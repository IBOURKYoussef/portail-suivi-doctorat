package ma.spring.registrationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// ============= Registration.java =============
@Entity
@Table(name = "registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Référence au User du user-service

    @Column(nullable = false)
    private Long campaignId; // Référence à la campagne

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationType type; // INSCRIPTION ou REINSCRIPTION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status; // PENDING, APPROVED_BY_DIRECTOR, REJECTED, APPROVED_BY_ADMIN

    // Informations du sujet de thèse
    @Column(length = 500)
    private String thesisTitle;

    @Column(length = 2000)
    private String thesisDescription;

    @Column(length = 200)
    private String researchField;

    // Directeur de thèse
    private Long directorId; // Référence au directeur

    private String directorName;

    // Co-directeur (optionnel)
    private Long coDirectorId;

    private String coDirectorName;

    // Laboratoire
    private String laboratory;

    // Documents
    @ElementCollection
    @CollectionTable(name = "registration_documents",
            joinColumns = @JoinColumn(name = "registration_id"))
    @Column(name = "document_path")
    private java.util.List<String> documentPaths;

    // Commentaires et avis
    @Column(length = 1000)
    private String directorComment;

    @Column(length = 1000)
    private String adminComment;

    private LocalDateTime directorReviewDate;

    private LocalDateTime adminReviewDate;

    // Informations sur l'année de réinscription
    private Integer academicYear; // Ex: 2024 pour 2024-2025

    private Integer doctoralYear; // 1ère, 2ème, 3ème année, etc.

    // Nouveaux champs pour candidature
    private String candidateName;
    private String programName;
    private String academicYearPeriod; // Ex: "2024-2025"
    private String previousEducation;
    private String institution;
    private Integer graduationYear;
    private String grade;
    private String phone;
    private String researchTitle;
    
    @Column(length = 1000)
    private String researchSummary;
    
    @Column(length = 2000)
    private String motivationLetter;

    // Dates
    @Column(nullable = false)
    private LocalDateTime submissionDate;

    private LocalDateTime approvalDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        submissionDate = LocalDateTime.now();
        status = RegistrationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
