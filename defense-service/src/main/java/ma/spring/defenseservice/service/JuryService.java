package ma.spring.defenseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.dto.JuryMemberRequest;
import ma.spring.defenseservice.dto.JuryProposalRequest;
import ma.spring.defenseservice.exception.BusinessException;
import ma.spring.defenseservice.exception.ResourceNotFoundException;
import ma.spring.defenseservice.model.Defense;
import ma.spring.defenseservice.model.DefenseStatus;
import ma.spring.defenseservice.model.JuryMember;
import ma.spring.defenseservice.model.JuryRole;
import ma.spring.defenseservice.model.MemberStatus;
import ma.spring.defenseservice.repository.DefenseRepository;
import ma.spring.defenseservice.repository.JuryMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JuryService {

    private final DefenseRepository defenseRepository;
    private final JuryMemberRepository juryMemberRepository;
    private final RapporteurService rapporteurService;

    @Transactional
    public void proposeJury(Long defenseId, JuryProposalRequest request, Long directorId) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        // Vérifier que le demandeur est le directeur
        if (!defense.getDirectorId().equals(directorId)) {
            throw new BusinessException("Seul le directeur peut proposer le jury");
        }

        // Vérifier le statut
        if (!defense.getStatus().equals(DefenseStatus.PREREQUISITES_VALIDATED)) {
            throw new BusinessException("Les prérequis doivent être validés avant de proposer le jury");
        }

        // Supprimer l'ancien jury s'il existe
        juryMemberRepository.deleteByDefenseId(defenseId);

        // Ajouter le président
        JuryMember president = createJuryMember(defense, request.getPresident());
        president.setRole(JuryRole.PRESIDENT);
        juryMemberRepository.save(president);

        // Ajouter les examinateurs
        List<JuryMember> examiners = request.getExaminers().stream()
                .map(examinerRequest -> {
                    JuryMember examiner = createJuryMember(defense, examinerRequest);
                    examiner.setRole(JuryRole.EXAMINER);
                    return examiner;
                })
                .collect(Collectors.toList());
        juryMemberRepository.saveAll(examiners);

        // Mettre à jour le statut
        defense.setStatus(DefenseStatus.JURY_PROPOSED);
        defenseRepository.save(defense);

        // Désigner les rapporteurs
        rapporteurService.designateRapporteurs(defenseId, request.getRapporteurs());

        log.info("Jury proposed for defense: {}", defenseId);
    }

    @Transactional
    public void validateJury(Long defenseId) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        if (!defense.getStatus().equals(DefenseStatus.JURY_PROPOSED)) {
            throw new BusinessException("Le jury doit être proposé avant validation");
        }

        // Vérifier la composition du jury
        List<JuryMember> jury = juryMemberRepository.findByDefenseId(defenseId);

        // Vérifier qu'il y a un président
        boolean hasPresident = jury.stream()
                .anyMatch(member -> member.getRole().equals(JuryRole.PRESIDENT));

        // Vérifier qu'il y a au moins 1 examinateur
        long examinersCount = jury.stream()
                .filter(member -> member.getRole().equals(JuryRole.EXAMINER))
                .count();

        // Vérifier qu'il y a au moins 2 rapporteurs
        long rapporteursCount = rapporteurService.getRapporteursByDefense(defenseId).size();

        log.info("Validating jury composition - President: {}, Examiners: {}, Rapporteurs: {}", 
                hasPresident, examinersCount, rapporteursCount);

        if (!hasPresident) {
            throw new BusinessException("Le jury doit avoir un président");
        }
        if (examinersCount < 1) {
            throw new BusinessException("Le jury doit avoir au moins un examinateur");
        }
        if (rapporteursCount < 2) {
            throw new BusinessException("Le jury doit avoir au moins deux rapporteurs");
        }

        // Mettre à jour le statut
        defense.setStatus(DefenseStatus.JURY_VALIDATED);
        defenseRepository.save(defense);

        log.info("Jury validated for defense: {}", defenseId);
    }

    public List<JuryMember> getJuryMembers(Long defenseId) {
        return juryMemberRepository.findByDefenseId(defenseId);
    }

    private JuryMember createJuryMember(Defense defense, JuryMemberRequest request) {
        return JuryMember.builder()
                .defense(defense)
                .professorId(request.getProfessorId())
                .name(request.getName())
                .institution(request.getInstitution())
                .grade(request.getGrade())
                .email(request.getEmail())
                .role(request.getRole())
                .status(MemberStatus.INVITED)
                .build();
    }
}