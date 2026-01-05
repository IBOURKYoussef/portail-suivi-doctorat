package ma.spring.registrationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.registrationservice.dto.CampaignRequest;
import ma.spring.registrationservice.dto.CampaignResponse;
import ma.spring.registrationservice.service.CampaignService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Créer une nouvelle campagne
     * POST /api/campaigns
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCampaign(
            @Valid @RequestBody CampaignRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Creating campaign: {}", request.getName());

        try {
            CampaignResponse response = campaignService.createCampaign(request, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Campagne créée avec succès");
            result.put("campaign", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating campaign", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Mettre à jour une campagne
     * PUT /api/campaigns/{id}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request
    ) {
        log.info("Updating campaign: {}", id);

        try {
            CampaignResponse response = campaignService.updateCampaign(id, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Campagne mise à jour avec succès");
            result.put("campaign", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating campaign", e);

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Activer/Désactiver une campagne
     * PATCH /api/campaigns/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CampaignResponse> toggleCampaignStatus(@PathVariable Long id) {
        log.info("Toggling campaign status: {}", id);

        CampaignResponse response = campaignService.toggleCampaignStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une campagne par ID
     * GET /api/campaigns/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long id) {
        log.info("Getting campaign: {}", id);

        try {
            CampaignResponse response = campaignService.getCampaignById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting campaign", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer toutes les campagnes
     * GET /api/campaigns
     */
    @GetMapping
    public ResponseEntity<Page<CampaignResponse>> getAllCampaigns(Pageable pageable) {
        log.info("Getting all campaigns");

        Page<CampaignResponse> campaigns = campaignService.getAllCampaigns(pageable);
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Récupérer les campagnes ouvertes
     * GET /api/campaigns/open
     */
    @GetMapping("/open")
    public ResponseEntity<List<CampaignResponse>> getOpenCampaigns() {
        log.info("Getting open campaigns");

        List<CampaignResponse> campaigns = campaignService.getOpenCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    /**
     * Récupérer les campagnes actives
     * GET /api/campaigns/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActiveCampaigns() {
        log.info("Getting active campaigns");

        List<CampaignResponse> campaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(campaigns);
    }
}
