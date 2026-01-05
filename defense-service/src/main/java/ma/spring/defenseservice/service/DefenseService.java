package ma.spring.defenseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.client.UserServiceClient;
import ma.spring.defenseservice.dto.DefenseRequest;
import ma.spring.defenseservice.dto.DefenseResponse;
import ma.spring.defenseservice.dto.UserDTO;
import ma.spring.defenseservice.event.DefenseEvent;
import ma.spring.defenseservice.event.EventPublisher;
import ma.spring.defenseservice.exception.BusinessException;
import ma.spring.defenseservice.exception.ResourceNotFoundException;
import ma.spring.defenseservice.exception.ValidationException;
import ma.spring.defenseservice.mapper.DefenseMapper;
import ma.spring.defenseservice.model.Defense;
import ma.spring.defenseservice.model.DefenseStatus;
import ma.spring.defenseservice.repository.DefenseRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefenseService {

    private final DefenseRepository defenseRepository;
    private final DefenseMapper defenseMapper;
    private final UserServiceClient userServiceClient;
    private final EventPublisher eventPublisher;
    private final PrerequisiteService prerequisiteService;

    @Transactional
    public DefenseResponse submitDefenseRequest(DefenseRequest request, Long doctorantId) {
        // Vérifier si le doctorant a déjà une soutenance active
        defenseRepository.findActiveDefenseByDoctorant(doctorantId)
                .ifPresent(d -> {
                    throw new BusinessException("Vous avez déjà une soutenance en cours");
                });

        // Vérifier les prérequis
        if (!prerequisiteService.checkPrerequisites(
                request.getPublicationsCount(),
                request.getConferencesCount(),
                request.getTrainingHours())) {
            throw new BusinessException("Les prérequis ne sont pas satisfaits");
        }

        // Créer la soutenance
        Defense defense = defenseMapper.toEntity(request);
        defense.setDoctorantId(doctorantId);

        // Initialiser le statut
        defense.setStatus(DefenseStatus.PREREQUISITES_CHECK);

        Defense savedDefense = defenseRepository.save(defense);
        log.info("Defense request submitted: {}", savedDefense.getId());

        // Publier l'événement - TEMPORAIREMENT DESACTIVE pour debug
        try {
            publishDefenseEvent(savedDefense, "DEFENSE_SUBMITTED");
        } catch (Exception e) {
            log.error("Failed to publish defense event, but defense was created successfully", e);
        }

        return enrichDefenseResponse(savedDefense);
    }

    @Cacheable(value = "defenses", key = "#id")
    public DefenseResponse getDefenseById(Long id) {
        Defense defense = defenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));
        return enrichDefenseResponse(defense);
    }

    public List<DefenseResponse> getAllDefenses() {
        List<Defense> defenses = defenseRepository.findAll();
        return defenses.stream()
                .map(this::enrichDefenseResponse)
                .toList();
    }

    public Page<DefenseResponse> getMyDefenses(Long doctorantId, Pageable pageable) {
        // Utiliser la méthode avec pagination
        return defenseRepository.findByDoctorantIdPaged(doctorantId, pageable)
                .map(this::enrichDefenseResponse);
    }

    public List<DefenseResponse> getDefensesByDoctorant(Long doctorantId) {
        // Méthode sans pagination pour récupérer toutes les soutenances d'un doctorant
        List<Defense> defenses = defenseRepository.findByDoctorantId(doctorantId);
        return defenses.stream()
                .map(this::enrichDefenseResponse)
                .toList();
    }

    public Page<DefenseResponse> getPendingForDirector(Long directorId, Pageable pageable) {
        return defenseRepository.findPendingByDirector(directorId, pageable)
                .map(this::enrichDefenseResponse);
    }

    public Page<DefenseResponse> getPendingForAdmin(Pageable pageable) {
        return defenseRepository.findPendingForAdmin(pageable)
                .map(this::enrichDefenseResponse);
    }

    /**
     * Enrichir la réponse avec les noms des utilisateurs
     */
    private DefenseResponse enrichDefenseResponse(Defense defense) {
        DefenseResponse response = defenseMapper.toResponse(defense);
        
        try {
            // Récupérer le nom du doctorant
            if (defense.getDoctorantId() != null) {
                UserDTO doctorant = userServiceClient.getUserById(defense.getDoctorantId());
                if (doctorant != null) {
                    response.setDoctorantName(doctorant.getFirstName() + " " + doctorant.getLastName());
                }
            }
            
            // Récupérer le nom du directeur
            if (defense.getDirectorId() != null) {
                UserDTO director = userServiceClient.getUserById(defense.getDirectorId());
                if (director != null) {
                    response.setDirectorName(director.getFirstName() + " " + director.getLastName());
                }
            }
            
            // Récupérer le nom du co-directeur si présent
            if (defense.getCoDirectorId() != null) {
                UserDTO coDirector = userServiceClient.getUserById(defense.getCoDirectorId());
                if (coDirector != null) {
                    response.setCoDirectorName(coDirector.getFirstName() + " " + coDirector.getLastName());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to enrich defense response with user names: {}", e.getMessage());
        }
        
        return response;
    }

    @Transactional
    public DefenseResponse validatePrerequisites(Long defenseId, Boolean approved, String comment) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        // Accepter SUBMITTED ou PREREQUISITES_CHECK pour la validation
        if (!defense.getStatus().equals(DefenseStatus.PREREQUISITES_CHECK) && 
            !defense.getStatus().equals(DefenseStatus.SUBMITTED)) {
            throw new BusinessException("La vérification des prérequis n'est pas applicable. Statut actuel : " + defense.getStatus());
        }

        if (approved) {
            defense.setStatus(DefenseStatus.PREREQUISITES_VALIDATED);
            defense.setAdminComment(comment);
            defense.setAdminApprovalDate(LocalDateTime.now());

            // Notifier le directeur
            publishDefenseEvent(defense, "PREREQUISITES_VALIDATED");
        } else {
            defense.setStatus(DefenseStatus.PREREQUISITES_REJECTED);
            defense.setAdminComment(comment);

            // Notifier le doctorant
            publishDefenseEvent(defense, "PREREQUISITES_REJECTED");
        }

        Defense updatedDefense = defenseRepository.save(defense);
        return enrichDefenseResponse(updatedDefense);
    }

    @Transactional
    public DefenseResponse authorizeDefense(Long defenseId, Boolean authorized,
                                            LocalDateTime defenseDate,
                                            String location, String room, String comment) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        // Accepter PREREQUISITES_VALIDATED ou REPORTS_RECEIVED pour autorisation
        if (!defense.getStatus().equals(DefenseStatus.REPORTS_RECEIVED) &&
            !defense.getStatus().equals(DefenseStatus.PREREQUISITES_VALIDATED)) {
            throw new BusinessException("La soutenance n'est pas prête pour autorisation. Statut actuel : " + defense.getStatus());
        }

        if (authorized) {
            defense.setStatus(DefenseStatus.AUTHORIZED);
            defense.setDefenseDate(defenseDate);
            defense.setDefenseLocation(location);
            defense.setDefenseRoom(room);
            defense.setAuthorizationDate(LocalDateTime.now());
            defense.setAdminComment(comment);

            // Planifier automatiquement après autorisation
            scheduleDefense(defenseId, defenseDate, location, room);
        } else {
            defense.setStatus(DefenseStatus.CANCELLED);
            defense.setAdminComment(comment);
        }

        Defense updatedDefense = defenseRepository.save(defense);

        // Publier l'événement
        if (authorized) {
            publishDefenseEvent(defense, "DEFENSE_AUTHORIZED");
        } else {
            publishDefenseEvent(defense, "DEFENSE_REJECTED");
        }

        return enrichDefenseResponse(updatedDefense);
    }

    @Transactional
    public DefenseResponse scheduleDefense(Long defenseId, LocalDateTime defenseDate,
                                           String location, String room) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        if (!defense.getStatus().equals(DefenseStatus.AUTHORIZED)) {
            throw new BusinessException("La soutenance doit être autorisée avant planification");
        }

        defense.setDefenseDate(defenseDate);
        defense.setDefenseLocation(location);
        defense.setDefenseRoom(room);
        defense.setStatus(DefenseStatus.SCHEDULED);

        Defense updatedDefense = defenseRepository.save(defense);

        // Publier l'événement de planification
        publishDefenseEvent(defense, "DEFENSE_SCHEDULED");

        return enrichDefenseResponse(updatedDefense);
    }

    @Transactional
    public DefenseResponse recordDefenseResult(Long defenseId, String result,
                                               String remarks, String mention) {
        Defense defense = defenseRepository.findById(defenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Soutenance non trouvée"));

        // Accepter SCHEDULED ou AUTHORIZED pour enregistrer le résultat
        if (!defense.getStatus().equals(DefenseStatus.SCHEDULED) &&
            !defense.getStatus().equals(DefenseStatus.AUTHORIZED)) {
            throw new BusinessException("La soutenance n'est pas prête pour enregistrer le résultat. Statut actuel : " + defense.getStatus());
        }

        defense.setResult(ma.spring.defenseservice.model.DefenseResult.valueOf(result));
        defense.setJuryRemarks(remarks);
        defense.setMention(mention);
        defense.setStatus(DefenseStatus.COMPLETED);

        Defense updatedDefense = defenseRepository.save(defense);

        // Publier l'événement de résultat
        publishDefenseEvent(defense, "DEFENSE_COMPLETED");

        return enrichDefenseResponse(updatedDefense);
    }

    private void publishDefenseEvent(Defense defense, String eventType) {
        try {
            // Récupérer les informations utilisateur via Feign
            UserDTO doctorant = userServiceClient.getUserById(defense.getDoctorantId());
            UserDTO director = userServiceClient.getUserById(defense.getDirectorId());

            DefenseEvent event = DefenseEvent.builder()
                    .defenseId(defense.getId())
                    .doctorantId(defense.getDoctorantId())
                    .doctorantEmail(doctorant.getEmail())
                    .directorEmail(director.getEmail())
                    .eventType(eventType)
                    .status(defense.getStatus())
                    .result(defense.getResult())
                    .thesisTitle(defense.getThesisTitle())
                    .defenseDate(defense.getDefenseDate())
                    .defenseLocation(defense.getDefenseLocation())
                    .message("Événement de soutenance: " + eventType)
                    .build();

            eventPublisher.publishDefenseEvent(event);
            eventPublisher.publishNotification(event);

        } catch (Exception e) {
            log.error("Failed to publish defense event", e);
        }
    }

    public List<DefenseResponse> getScheduledDefenses(LocalDateTime start, LocalDateTime end) {
        return defenseRepository.findScheduledBetween(start, end)
                .stream()
                .map(defenseMapper::toResponse)
                .toList();
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", defenseRepository.count());
        stats.put("submitted", defenseRepository.countByStatus(DefenseStatus.SUBMITTED));
        stats.put("authorized", defenseRepository.countByStatus(DefenseStatus.AUTHORIZED));
        stats.put("scheduled", defenseRepository.countByStatus(DefenseStatus.SCHEDULED));
        stats.put("completed", defenseRepository.countByStatus(DefenseStatus.COMPLETED));
        stats.put("cancelled", defenseRepository.countByStatus(DefenseStatus.CANCELLED));
        return stats;
    }
}