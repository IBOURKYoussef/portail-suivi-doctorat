package ma.spring.registrationservice.repository;

import ma.spring.registrationservice.model.Registration;
import ma.spring.registrationservice.model.RegistrationStatus;
import ma.spring.registrationservice.model.RegistrationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ============= RegistrationRepository.java =============
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Trouver toutes les inscriptions d'un utilisateur
    List<Registration> findByUserId(Long userId);

    // Trouver les inscriptions d'un utilisateur par type
    List<Registration> findByUserIdAndType(Long userId, RegistrationType type);

    // Trouver les inscriptions d'une campagne
    Page<Registration> findByCampaignId(Long campaignId, Pageable pageable);

    // Trouver les inscriptions par statut
    Page<Registration> findByStatus(RegistrationStatus status, Pageable pageable);

    // Trouver les inscriptions d'un directeur en attente de validation
    @Query("SELECT r FROM Registration r WHERE r.directorId = :directorId AND r.status = 'PENDING'")
    Page<Registration> findPendingByDirector(@Param("directorId") Long directorId, Pageable pageable);

    // Trouver toutes les inscriptions d'un directeur
    Page<Registration> findByDirectorId(Long directorId, Pageable pageable);

    // Vérifier si un utilisateur a déjà une inscription pour une campagne
    Optional<Registration> findByUserIdAndCampaignId(Long userId, Long campaignId);

    // Vérifier si un utilisateur a une inscription en cours
    @Query("SELECT r FROM Registration r WHERE r.userId = :userId " +
            "AND r.status IN ('PENDING', 'APPROVED_BY_DIRECTOR') " +
            "ORDER BY r.createdAt DESC")
    List<Registration> findActiveRegistrationsByUserId(@Param("userId") Long userId);

    // Compter les inscriptions par statut
    long countByStatus(RegistrationStatus status);

    // Compter les inscriptions d'une campagne
    long countByCampaignId(Long campaignId);

    // Statistiques par année académique
    @Query("SELECT r.academicYear, COUNT(r) FROM Registration r GROUP BY r.academicYear")
    List<Object[]> countByAcademicYear();
}

