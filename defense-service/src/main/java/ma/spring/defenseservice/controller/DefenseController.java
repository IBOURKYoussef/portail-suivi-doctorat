package ma.spring.defenseservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.spring.defenseservice.dto.DefenseRequest;
import ma.spring.defenseservice.dto.DefenseResponse;
import ma.spring.defenseservice.service.DefenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/defenses")
@RequiredArgsConstructor
public class DefenseController {

    private final DefenseService defenseService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTORANT')")
    @CircuitBreaker(name = "defenseService", fallbackMethod = "submitDefenseFallback")
    public ResponseEntity<DefenseResponse> submitDefense(
            @Valid @RequestBody DefenseRequest request,
            @RequestHeader("X-User-Id") Long doctorantId) {
        DefenseResponse response = defenseService.submitDefenseRequest(request, doctorantId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DIRECTEUR_THESE')")
    public ResponseEntity<List<DefenseResponse>> getAllDefenses() {
        List<DefenseResponse> defenses = defenseService.getAllDefenses();
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<Page<DefenseResponse>> getMyDefenses(
            @RequestHeader("X-User-Id") Long doctorantId,
            Pageable pageable) {
        Page<DefenseResponse> defenses = defenseService.getMyDefenses(doctorantId, pageable);
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/doctorant/{doctorantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DIRECTEUR_THESE', 'DOCTORANT')")
    public ResponseEntity<List<DefenseResponse>> getDefensesByDoctorant(
            @PathVariable Long doctorantId) {
        List<DefenseResponse> defenses = defenseService.getDefensesByDoctorant(doctorantId);
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/director/pending")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public ResponseEntity<Page<DefenseResponse>> getPendingForDirector(
            @RequestHeader("X-User-Id") Long directorId,
            Pageable pageable) {
        Page<DefenseResponse> defenses = defenseService.getPendingForDirector(directorId, pageable);
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DefenseResponse>> getPendingForAdmin(Pageable pageable) {
        Page<DefenseResponse> defenses = defenseService.getPendingForAdmin(pageable);
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefenseResponse> getDefense(@PathVariable Long id) {
        DefenseResponse response = defenseService.getDefenseById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/validate-prerequisites")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DefenseResponse> validatePrerequisites(
            @PathVariable Long id,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String comment) {
        DefenseResponse response = defenseService.validatePrerequisites(id, approved, comment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/authorize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DefenseResponse> authorizeDefense(
            @PathVariable Long id,
            @RequestParam Boolean authorized,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime defenseDate,
            @RequestParam String location,
            @RequestParam String room,
            @RequestParam(required = false) String comment) {
        DefenseResponse response = defenseService.authorizeDefense(
                id, authorized, defenseDate, location, room, comment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/result")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTEUR_THESE')")
    public ResponseEntity<DefenseResponse> recordResult(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String mention) {
        DefenseResponse response = defenseService.recordDefenseResult(id, result, remarks, mention);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<DefenseResponse>> getScheduledDefenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {
        List<DefenseResponse> defenses = defenseService.getScheduledDefenses(start, end);
        return ResponseEntity.ok(defenses);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = defenseService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // Fallback method for Circuit Breaker
    public ResponseEntity<DefenseResponse> submitDefenseFallback(
            DefenseRequest request, Long doctorantId, Throwable t) {
        // Logique de fallback
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(null);
    }
}