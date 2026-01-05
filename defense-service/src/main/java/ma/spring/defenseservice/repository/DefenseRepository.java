package ma.spring.defenseservice.repository;

import ma.spring.defenseservice.model.Defense;
import ma.spring.defenseservice.model.DefenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefenseRepository extends JpaRepository<Defense, Long> {

    // Version avec pagination - requête personnalisée
    @Query("SELECT d FROM Defense d WHERE d.doctorantId = :doctorantId")
    Page<Defense> findByDoctorantIdPaged(@Param("doctorantId") Long doctorantId, Pageable pageable);

    // Version sans pagination (pour les autres besoins)
    List<Defense> findByDoctorantId(Long doctorantId);

    // Trouver les soutenances d'un directeur
    Page<Defense> findByDirectorId(Long directorId, Pageable pageable);

    // Trouver par statut
    Page<Defense> findByStatus(DefenseStatus status, Pageable pageable);

    // Trouver les soutenances en attente de validation du directeur
    @Query("SELECT d FROM Defense d WHERE d.directorId = :directorId " +
            "AND d.status IN ('SUBMITTED', 'PREREQUISITES_VALIDATED', 'JURY_PROPOSED')")
    Page<Defense> findPendingByDirector(@Param("directorId") Long directorId, Pageable pageable);

    // Trouver les soutenances en attente de validation admin
    @Query("SELECT d FROM Defense d WHERE d.status IN ('JURY_VALIDATED', 'REPORTS_RECEIVED')")
    Page<Defense> findPendingForAdmin(Pageable pageable);

    // Trouver les soutenances programmées
    @Query("SELECT d FROM Defense d WHERE d.status = 'SCHEDULED' " +
            "AND d.defenseDate BETWEEN :startDate AND :endDate")
    List<Defense> findScheduledBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Vérifier si un doctorant a déjà une soutenance en cours
    @Query("SELECT d FROM Defense d WHERE d.doctorantId = :doctorantId " +
            "AND d.status NOT IN ('COMPLETED', 'CANCELLED')")
    Optional<Defense> findActiveDefenseByDoctorant(@Param("doctorantId") Long doctorantId);

    // Statistiques
    long countByStatus(DefenseStatus status);

    @Query("SELECT COUNT(d) FROM Defense d WHERE d.status = 'COMPLETED' " +
            "AND d.academicYear = :year")
    long countCompletedByYear(@Param("year") Integer year);

    // Trouver les soutenances par année académique
    List<Defense> findByAcademicYear(Integer academicYear);
}