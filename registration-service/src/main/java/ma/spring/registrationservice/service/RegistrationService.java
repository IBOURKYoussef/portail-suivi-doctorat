package ma.spring.registrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.client.UserServiceClient;
import ma.spring.registrationservice.dto.*;
import ma.spring.registrationservice.event.RegistrationEvent;
import ma.spring.registrationservice.exception.CampaignNotFoundException;
import ma.spring.registrationservice.exception.RegistrationAlreadyExistsException;
import ma.spring.registrationservice.exception.RegistrationNotFoundException;
import ma.spring.registrationservice.model.Campaign;
import ma.spring.registrationservice.model.Registration;
import ma.spring.registrationservice.model.RegistrationStatus;
import ma.spring.registrationservice.repository.CampaignRepository;
import ma.spring.registrationservice.repository.RegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final CampaignRepository campaignRepository;
    private final UserServiceClient userServiceClient;
    private final EventPublisher eventPublisher;

    /**
     * Créer une nouvelle inscription
     */
    public RegistrationResponse createRegistration(RegistrationRequest request, Long userId) {
        log.info("Creating registration for user {}", userId);

        Registration registration;
        Campaign campaign = null;
        UserDTO user = userServiceClient.getUserById(userId);

        // Cas 1: Candidature simple (candidat) - sans campagne obligatoire
        if (request.getProgramName() != null) {
            log.info("Creating simple candidature for program: {}", request.getProgramName());
            
            registration = Registration.builder()
                    .userId(userId)
                    .status(RegistrationStatus.PENDING)
                    .candidateName(request.getCandidateName() != null ? request.getCandidateName() : 
                                  user.getFirstName() + " " + user.getLastName())
                    .programName(request.getProgramName())
                    .researchField(request.getResearchField())
                    .academicYearPeriod(request.getAcademicYear())
                    .previousEducation(request.getPreviousEducation())
                    .institution(request.getInstitution())
                    .graduationYear(request.getGraduationYear())
                    .grade(request.getGrade())
                    .phone(request.getPhone())
                    .researchTitle(request.getResearchTitle())
                    .researchSummary(request.getResearchSummary())
                    .motivationLetter(request.getMotivationLetter())
                    .documentPaths(request.getDocumentPaths())
                    .build();
        }
        // Cas 2: Inscription doctorale (doctorant) - avec campagne obligatoire
        else {
            log.info("Creating doctoral registration for campaign {}", request.getCampaignId());
            
            // Vérifier que la campagne existe et est ouverte
            campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new CampaignNotFoundException("Campagne non trouvée"));

            if (!campaign.isOpen()) {
                throw new IllegalStateException("La campagne n'est pas ouverte");
            }

            // Vérifier que l'utilisateur n'a pas déjà une inscription pour cette campagne
            registrationRepository.findByUserIdAndCampaignId(userId, request.getCampaignId())
                    .ifPresent(r -> {
                        throw new RegistrationAlreadyExistsException(
                                "Une inscription existe déjà pour cette campagne"
                        );
                    });

            // Récupérer les informations du directeur
            UserDTO director = userServiceClient.getUserById(request.getDirectorId());

            registration = Registration.builder()
                    .userId(userId)
                    .campaignId(request.getCampaignId())
                    .type(request.getType())
                    .status(RegistrationStatus.PENDING)
                    .thesisTitle(request.getThesisTitle())
                    .thesisDescription(request.getThesisDescription())
                    .researchField(request.getResearchField())
                    .directorId(request.getDirectorId())
                    .directorName(director.getFirstName() + " " + director.getLastName())
                    .coDirectorId(request.getCoDirectorId())
                    .laboratory(request.getLaboratory())
                    .academicYear(campaign.getAcademicYear())
                    .doctoralYear(request.getDoctoralYear())
                    .documentPaths(request.getDocumentPaths())
                    .build();

            if (request.getCoDirectorId() != null) {
                UserDTO coDirector = userServiceClient.getUserById(request.getCoDirectorId());
                registration.setCoDirectorName(coDirector.getFirstName() + " " + coDirector.getLastName());
            }
        }

        Registration saved = registrationRepository.save(registration);

        // Publier un événement Kafka
        RegistrationEvent event = RegistrationEvent.builder()
                .registrationId(saved.getId())
                .userId(userId)
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .eventType("SUBMITTED")
                .status(saved.getStatus().name())
                .thesisTitle(saved.getThesisTitle() != null ? saved.getThesisTitle() : saved.getResearchTitle())
                .directorName(saved.getDirectorName())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishRegistrationEvent(event);
        eventPublisher.publishNotificationEvent(event);

        log.info("Registration created with id: {}", saved.getId());

        return mapToResponse(saved, campaign, user);
    }

    /**
     * Valider une inscription par le directeur
     */
    public RegistrationResponse reviewByDirector(Long registrationId, Long directorId, ReviewRequest request) {
        log.info("Director {} reviewing registration {}", directorId, registrationId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException("Inscription non trouvée"));

        // Vérifier que c'est bien le directeur de cette inscription
        if (!registration.getDirectorId().equals(directorId)) {
            throw new IllegalStateException("Vous n'êtes pas le directeur de cette inscription");
        }

        // Vérifier le statut
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Cette inscription ne peut plus être modifiée");
        }

        // Mettre à jour le statut
        registration.setStatus(request.getApproved()
                ? RegistrationStatus.APPROVED_BY_DIRECTOR
                : RegistrationStatus.REJECTED_BY_DIRECTOR);
        registration.setDirectorComment(request.getComment());
        registration.setDirectorReviewDate(LocalDateTime.now());

        Registration saved = registrationRepository.save(registration);

        // Publier un événement
        UserDTO user = userServiceClient.getUserById(registration.getUserId());
        Campaign campaign = campaignRepository.findById(registration.getCampaignId()).orElse(null);

        RegistrationEvent event = RegistrationEvent.builder()
                .registrationId(saved.getId())
                .userId(saved.getUserId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .eventType(request.getApproved() ? "APPROVED_BY_DIRECTOR" : "REJECTED_BY_DIRECTOR")
                .status(saved.getStatus().name())
                .thesisTitle(saved.getThesisTitle())
                .directorName(saved.getDirectorName())
                .comment(request.getComment())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishRegistrationEvent(event);
        eventPublisher.publishNotificationEvent(event);

        log.info("Registration {} reviewed by director: {}", registrationId, request.getApproved());

        return mapToResponse(saved, campaign, user);
    }

    /**
     * Valider une inscription par l'administration
     */
    public RegistrationResponse reviewByAdmin(Long registrationId, ReviewRequest request) {
        log.info("Admin reviewing registration {}", registrationId);

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException("Inscription non trouvée"));

        // Vérifier le statut
        if (registration.getStatus() != RegistrationStatus.APPROVED_BY_DIRECTOR) {
            throw new IllegalStateException(
                    "L'inscription doit d'abord être approuvée par le directeur"
            );
        }

        // Mettre à jour le statut
        registration.setStatus(request.getApproved()
                ? RegistrationStatus.APPROVED_BY_ADMIN
                : RegistrationStatus.REJECTED_BY_ADMIN);
        registration.setAdminComment(request.getComment());
        registration.setAdminReviewDate(LocalDateTime.now());

        if (request.getApproved()) {
            registration.setApprovalDate(LocalDateTime.now());
            registration.setStatus(RegistrationStatus.COMPLETED);
        }

        Registration saved = registrationRepository.save(registration);

        // Publier un événement
        UserDTO user = userServiceClient.getUserById(registration.getUserId());
        Campaign campaign = campaignRepository.findById(registration.getCampaignId()).orElse(null);

        RegistrationEvent event = RegistrationEvent.builder()
                .registrationId(saved.getId())
                .userId(saved.getUserId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .eventType(request.getApproved() ? "APPROVED_BY_ADMIN" : "REJECTED_BY_ADMIN")
                .status(saved.getStatus().name())
                .thesisTitle(saved.getThesisTitle())
                .comment(request.getComment())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishRegistrationEvent(event);
        eventPublisher.publishNotificationEvent(event);

        log.info("Registration {} reviewed by admin: {}", registrationId, request.getApproved());

        return mapToResponse(saved, campaign, user);
    }

    /**
     * Récupérer une inscription par ID
     */
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("Inscription non trouvée"));

        UserDTO user = userServiceClient.getUserById(registration.getUserId());
        Campaign campaign = campaignRepository.findById(registration.getCampaignId()).orElse(null);

        return mapToResponse(registration, campaign, user);
    }

    /**
     * Récupérer les inscriptions d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getUserRegistrations(Long userId) {
        List<Registration> registrations = registrationRepository.findByUserId(userId);
        UserDTO user = userServiceClient.getUserById(userId);

        return registrations.stream()
                .map(r -> {
                    Campaign campaign = campaignRepository.findById(r.getCampaignId()).orElse(null);
                    return mapToResponse(r, campaign, user);
                })
                .toList();
    }

    /**
     * Récupérer les inscriptions en attente pour un directeur
     */
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> getPendingRegistrationsForDirector(Long directorId, Pageable pageable) {
        Page<Registration> registrations = registrationRepository.findPendingByDirector(directorId, pageable);

        return registrations.map(r -> {
            UserDTO user = userServiceClient.getUserById(r.getUserId());
            Campaign campaign = campaignRepository.findById(r.getCampaignId()).orElse(null);
            return mapToResponse(r, campaign, user);
        });
    }

    /**
     * Mapper Registration vers RegistrationResponse
     */
    private RegistrationResponse mapToResponse(Registration registration, Campaign campaign, UserDTO user) {
        return RegistrationResponse.builder()
                .id(registration.getId())
                .userId(registration.getUserId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .campaignId(registration.getCampaignId())
                .campaignName(campaign != null ? campaign.getName() : null)
                .type(registration.getType())
                .status(registration.getStatus())
                .thesisTitle(registration.getThesisTitle())
                .thesisDescription(registration.getThesisDescription())
                .researchField(registration.getResearchField())
                .directorId(registration.getDirectorId())
                .directorName(registration.getDirectorName())
                .coDirectorId(registration.getCoDirectorId())
                .coDirectorName(registration.getCoDirectorName())
                .laboratory(registration.getLaboratory())
                .documentPaths(registration.getDocumentPaths())
                .directorComment(registration.getDirectorComment())
                .adminComment(registration.getAdminComment())
                .directorReviewDate(registration.getDirectorReviewDate())
                .adminReviewDate(registration.getAdminReviewDate())
                .academicYear(registration.getAcademicYear())
                .doctoralYear(registration.getDoctoralYear())
                .submissionDate(registration.getSubmissionDate())
                .approvalDate(registration.getApprovalDate())
                .createdAt(registration.getCreatedAt())
                // Nouveaux champs pour candidature
                .candidateName(registration.getCandidateName())
                .programName(registration.getProgramName())
                .academicYearPeriod(registration.getAcademicYearPeriod())
                .previousEducation(registration.getPreviousEducation())
                .institution(registration.getInstitution())
                .graduationYear(registration.getGraduationYear())
                .grade(registration.getGrade())
                .phone(registration.getPhone())
                .researchTitle(registration.getResearchTitle())
                .researchSummary(registration.getResearchSummary())
                .motivationLetter(registration.getMotivationLetter())
                .build();
    }
}
