package ma.spring.registrationservice.repository;

import ma.spring.registrationservice.model.Campaign;
import ma.spring.registrationservice.model.RegistrationType;
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
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Trouver les campagnes actives
    List<Campaign> findByActiveTrue();

    // Trouver les campagnes par type
    List<Campaign> findByType(RegistrationType type);

    // Trouver les campagnes ouvertes (actives et dans la période)
    @Query("SELECT c FROM Campaign c WHERE c.active = true " +
            "AND c.startDate <= :now AND c.endDate >= :now")
    List<Campaign> findOpenCampaigns(@Param("now") LocalDateTime now);

    // Trouver une campagne ouverte pour un type spécifique
    @Query("SELECT c FROM Campaign c WHERE c.active = true " +
            "AND c.type = :type " +
            "AND c.startDate <= :now AND c.endDate >= :now")
    Optional<Campaign> findOpenCampaignByType(
            @Param("type") RegistrationType type,
            @Param("now") LocalDateTime now
    );

    // Trouver les campagnes par année académique
    List<Campaign> findByAcademicYear(Integer academicYear);

    // Pagination des campagnes
    Page<Campaign> findAll(Pageable pageable);

    // Vérifier si une campagne existe pour une année et un type
    boolean existsByAcademicYearAndType(Integer academicYear, RegistrationType type);
}
