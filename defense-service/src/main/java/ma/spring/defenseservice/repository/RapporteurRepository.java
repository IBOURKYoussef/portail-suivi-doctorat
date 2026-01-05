package ma.spring.defenseservice.repository;

import ma.spring.defenseservice.model.Rapporteur;
import ma.spring.defenseservice.model.ReportOpinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RapporteurRepository extends JpaRepository<Rapporteur, Long> {

    List<Rapporteur> findByDefenseId(Long defenseId);

    // Nouvelle méthode pour compter tous les rapporteurs d'une soutenance
    @Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId")
    long countByDefenseId(@Param("defenseId") Long defenseId);

    @Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
            "AND r.reportSubmissionDate IS NOT NULL")
    long countSubmittedReportsByDefense(@Param("defenseId") Long defenseId);

    @Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
            "AND r.opinion = 'FAVORABLE'")
    long countFavorableOpinionsByDefense(@Param("defenseId") Long defenseId);

    // Méthode générée par Spring Data JPA
    void deleteByDefenseId(Long defenseId);
}


//// ============= RapporteurRepository.java =============
//package ma.spring.defenseservice.repository;
//
//import ma.spring.defenseservice.model.Rapporteur;
//import ma.spring.defenseservice.model.ReportOpinion;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface RapporteurRepository extends JpaRepository<Rapporteur, Long> {
//    List<Rapporteur> findByDefenseId(Long defenseId);
//
//    @Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
//            "AND r.reportSubmissionDate IS NOT NULL")
//    long countSubmittedReportsByDefense(@Param("defenseId") Long defenseId);
//
//    @Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
//            "AND r.opinion = 'FAVORABLE'")
//    long countFavorableOpinionsByDefense(@Param("defenseId") Long defenseId);
//
//    void deleteByDefenseId(Long defenseId);
//}