package ma.spring.defenseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.dto.ReportSubmissionRequest;
import ma.spring.defenseservice.dto.RapporteurRequest;
import ma.spring.defenseservice.exception.BusinessException;
import ma.spring.defenseservice.exception.ResourceNotFoundException;
import ma.spring.defenseservice.model.Defense;
import ma.spring.defenseservice.model.DefenseStatus;
import ma.spring.defenseservice.model.MemberStatus;
import ma.spring.defenseservice.model.Rapporteur;
import ma.spring.defenseservice.model.ReportOpinion;
import ma.spring.defenseservice.repository.DefenseRepository;
import ma.spring.defenseservice.repository.RapporteurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapporteurService {

    private final DefenseRepository defenseRepository;
    private final RapporteurRepository rapporteurRepository;

    @Transactional
    public void designateRapporteurs(Long defenseId, List<RapporteurRequest> requests) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        // Supprimer les anciens rapporteurs
        rapporteurRepository.deleteByDefenseId(defenseId);

        // Créer les nouveaux rapporteurs
        List<Rapporteur> rapporteurs = requests.stream()
                .map(request -> createRapporteur(defense, request))
                .toList();

        rapporteurRepository.saveAll(rapporteurs);

        // Ne pas mettre à jour le statut ici - c'est JuryService.proposeJury() qui gère le statut JURY_PROPOSED
        // Le statut sera mis à jour uniquement après validation du jury et soumission des rapports

        log.info("Rapporteurs designated for defense: {}", defenseId);
    }

    @Transactional
    public void submitReport(Long rapporteurId, ReportSubmissionRequest request) {
        Rapporteur rapporteur = rapporteurRepository.findById(rapporteurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Rapporteur avec l'ID " + rapporteurId + " non trouvé. Assurez-vous d'avoir proposé le jury."));

        Defense defense = rapporteur.getDefense();

        log.info("Submitting report for rapporteur: {}, defense status: {}", rapporteurId, defense.getStatus());

        // Vérifier que la soumission de rapport est possible
        // Le rapport peut être soumis après validation du jury ou après assignation des rapporteurs
        if (!defense.getStatus().equals(DefenseStatus.JURY_VALIDATED) &&
                !defense.getStatus().equals(DefenseStatus.REPORTS_RECEIVED)) {
            throw new BusinessException(
                "La soumission de rapport n'est pas autorisée à ce stade. Status actuel: " + defense.getStatus() + 
                ". Le jury doit d'abord être validé.");
        }

        // Mettre à jour le rapport
        rapporteur.setReport(request.getReport());
        rapporteur.setReportFilePath(request.getReportFilePath());
        rapporteur.setOpinion(request.getOpinion());
        rapporteur.setReportSubmissionDate(LocalDateTime.now());
        rapporteur.setStatus(MemberStatus.ACCEPTED);

        rapporteurRepository.save(rapporteur);

        // Vérifier si tous les rapporteurs ont soumis
        checkAllReportsSubmitted(defense.getId());

        log.info("Report submitted by rapporteur: {}", rapporteurId);
    }

    public List<Rapporteur> getRapporteursByDefense(Long defenseId) {
        return rapporteurRepository.findByDefenseId(defenseId);
    }

    private void checkAllReportsSubmitted(Long defenseId) {
        long totalRapporteurs = rapporteurRepository.countByDefenseId(defenseId);
        long submittedReports = rapporteurRepository.countSubmittedReportsByDefense(defenseId);

        log.info("Checking reports for defense {}: {}/{} submitted", defenseId, submittedReports, totalRapporteurs);

        if (submittedReports == totalRapporteurs && totalRapporteurs > 0) {
            Defense defense = defenseRepository.findById(defenseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

            // Vérifier si la majorité est favorable
            long favorableCount = rapporteurRepository.countFavorableOpinionsByDefense(defenseId);
            log.info("Favorable opinions: {}/{} ({}%)", favorableCount, totalRapporteurs, 
                    (favorableCount * 100.0 / totalRapporteurs));

            if (favorableCount >= Math.ceil(totalRapporteurs / 2.0)) {
                // Les rapports sont favorables, prêt pour autorisation
                defense.setStatus(DefenseStatus.REPORTS_RECEIVED);
                defenseRepository.save(defense);
                log.info("All reports received with majority favorable for defense: {}", defenseId);
            } else {
                // Les rapports ne sont pas favorables
                log.warn("Majority NOT favorable for defense {}: {}/{}", defenseId, favorableCount, totalRapporteurs);
                // Ne pas annuler automatiquement, laisser l'admin décider
                defense.setStatus(DefenseStatus.REPORTS_RECEIVED);
                defenseRepository.save(defense);
            }
        }
    }

    private Rapporteur createRapporteur(Defense defense, RapporteurRequest request) {
        return Rapporteur.builder()
                .defense(defense)
                .professorId(request.getProfessorId())
                .name(request.getName())
                .institution(request.getInstitution())
                .grade(request.getGrade())
                .email(request.getEmail())
                .status(MemberStatus.INVITED)
                .build();
    }
}