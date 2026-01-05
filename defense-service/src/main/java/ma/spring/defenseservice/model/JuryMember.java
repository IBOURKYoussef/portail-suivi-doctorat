// ============= JuryMember.java =============
package ma.spring.defenseservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jury_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JuryMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "defense_id", nullable = false)
    private Defense defense;

    private Long professorId; // ID du professeur (peut venir du user-service)

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String institution; // Établissement

    @Column(nullable = false)
    private String grade; // Grade (Professeur, HDR, etc.)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JuryRole role;

    private String email;

    @Enumerated(EnumType.STRING)
    private MemberStatus status; // INVITED, ACCEPTED, DECLINED
}

