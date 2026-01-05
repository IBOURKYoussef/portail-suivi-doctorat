package ma.spring.defenseservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.spring.defenseservice.dto.ReportSubmissionRequest;
import ma.spring.defenseservice.model.Rapporteur;
import ma.spring.defenseservice.service.RapporteurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapporteurs")
@RequiredArgsConstructor
public class RapporteurController {

    private final RapporteurService rapporteurService;

    @PostMapping("/{rapporteurId}/report")
    @PreAuthorize("hasAnyRole('DIRECTEUR_THESE', 'ADMIN')")
    public ResponseEntity<Void> submitReport(
            @PathVariable Long rapporteurId,
            @Valid @RequestBody ReportSubmissionRequest request) {
        rapporteurService.submitReport(rapporteurId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/defense/{defenseId}")
    public ResponseEntity<List<Rapporteur>> getRapporteursByDefense(
            @PathVariable Long defenseId) {
        List<Rapporteur> rapporteurs = rapporteurService.getRapporteursByDefense(defenseId);
        return ResponseEntity.ok(rapporteurs);
    }
}