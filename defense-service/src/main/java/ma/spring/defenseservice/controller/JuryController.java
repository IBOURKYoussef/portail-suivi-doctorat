package ma.spring.defenseservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.spring.defenseservice.dto.JuryProposalRequest;
import ma.spring.defenseservice.model.JuryMember;
import ma.spring.defenseservice.service.JuryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/defenses/{defenseId}/jury")
@RequiredArgsConstructor
public class JuryController {

    private final JuryService juryService;

    @PostMapping
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public ResponseEntity<Void> proposeJury(
            @PathVariable Long defenseId,
            @Valid @RequestBody JuryProposalRequest request,
            @RequestHeader("X-User-Id") Long directorId) {
        juryService.proposeJury(defenseId, request, directorId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> validateJury(@PathVariable Long defenseId) {
        juryService.validateJury(defenseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<JuryMember>> getJuryMembers(@PathVariable Long defenseId) {
        List<JuryMember> members = juryService.getJuryMembers(defenseId);
        return ResponseEntity.ok(members);
    }
}