package ma.spring.registrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.client.UserServiceClient;
import ma.spring.registrationservice.dto.CampaignRequest;
import ma.spring.registrationservice.dto.CampaignResponse;
import ma.spring.registrationservice.dto.UserDTO;
import ma.spring.registrationservice.exception.CampaignNotFoundException;
import ma.spring.registrationservice.model.Campaign;
import ma.spring.registrationservice.repository.CampaignRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// ============= CampaignService.java =============
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserServiceClient userServiceClient;

    /**
     * Créer une nouvelle campagne
     */
    public CampaignResponse createCampaign(CampaignRequest request, Long createdBy) {
        log.info("Creating campaign: {}", request.getName());

        // Vérifier les dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .academicYear(request.getAcademicYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .createdBy(createdBy)
                .build();

        Campaign saved = campaignRepository.save(campaign);

        log.info("Campaign created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Mettre à jour une campagne
     */
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        log.info("Updating campaign: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campagne non trouvée"));

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setType(request.getType());
        campaign.setAcademicYear(request.getAcademicYear());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());

        Campaign saved = campaignRepository.save(campaign);

        return mapToResponse(saved);
    }

    /**
     * Activer/Désactiver une campagne
     */
    public CampaignResponse toggleCampaignStatus(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campagne non trouvée"));

        campaign.setActive(!campaign.getActive());
        Campaign saved = campaignRepository.save(campaign);

        log.info("Campaign {} status toggled to: {}", id, saved.getActive());

        return mapToResponse(saved);
    }

    /**
     * Récupérer une campagne par ID
     */
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campagne non trouvée"));

        return mapToResponse(campaign);
    }

    /**
     * Récupérer toutes les campagnes
     */
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getAllCampaigns(Pageable pageable) {
        Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        return campaigns.map(this::mapToResponse);
    }

    /**
     * Récupérer les campagnes ouvertes
     */
    @Transactional(readOnly = true)
    public List<CampaignResponse> getOpenCampaigns() {
        List<Campaign> campaigns = campaignRepository.findOpenCampaigns(LocalDateTime.now());
        return campaigns.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Récupérer les campagnes actives
     */
    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByActiveTrue();
        return campaigns.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Mapper Campaign vers CampaignResponse
     */
    private CampaignResponse mapToResponse(Campaign campaign) {
        String createdByName = null;
        if (campaign.getCreatedBy() != null) {
            try {
                UserDTO creator = userServiceClient.getUserById(campaign.getCreatedBy());
                createdByName = creator.getFirstName() + " " + creator.getLastName();
            } catch (Exception e) {
                log.warn("Unable to fetch creator info", e);
            }
        }

        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .type(campaign.getType())
                .academicYear(campaign.getAcademicYear())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .active(campaign.getActive())
                .isOpen(campaign.isOpen())
                .createdAt(campaign.getCreatedAt())
                .createdBy(campaign.getCreatedBy())
                .createdByName(createdByName)
                .build();
    }
}
