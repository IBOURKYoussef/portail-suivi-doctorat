// ============= Rapporteur.java =============
package ma.spring.defenseservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapporteurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rapporteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "defense_id", nullable = false)
    private Defense defense;

    private Long professorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String institution;

    @Column(nullable = false)
    private String grade;

    private String email;

    @Enumerated(EnumType.STRING)
    private MemberStatus status; // INVITED, ACCEPTED, DECLINED

    // Rapport
    @Column(length = 5000)
    private String report; // Texte du rapport

    private String reportFilePath; // Chemin du fichier PDF

    @Enumerated(EnumType.STRING)
    private ReportOpinion opinion; // FAVORABLE, DEFAVORABLE, WITH_RESERVES

    private LocalDateTime reportSubmissionDate;
}

