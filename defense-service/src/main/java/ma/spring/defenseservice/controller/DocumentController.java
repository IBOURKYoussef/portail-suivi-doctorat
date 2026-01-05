package ma.spring.defenseservice.controller;

import lombok.RequiredArgsConstructor;
import ma.spring.defenseservice.dto.DocumentResponse;
import ma.spring.defenseservice.model.DocumentType;
import ma.spring.defenseservice.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTORANT', 'DIRECTEUR_THESE', 'ADMIN')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type,
            @RequestParam("entityId") Long entityId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "description", required = false) String description) {

        // Validation des paramètres
        if (file == null || file.isEmpty()) {
            throw new ma.spring.defenseservice.exception.BusinessException("Le fichier est requis");
        }
        if (type == null) {
            throw new ma.spring.defenseservice.exception.BusinessException(
                "Le type de document est requis. Valeurs possibles: " +
                "MANUSCRIPT, PLAGIARISM_REPORT, CV, THESIS_PDF, RAPPORTEUR_REPORT, etc.");
        }
        if (entityId == null) {
            throw new ma.spring.defenseservice.exception.BusinessException(
                "L'entityId est requis (ID de la défense ou de l'inscription)");
        }

        DocumentResponse response = documentService.uploadDocument(file, type, entityId, userId, description);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        Resource resource = documentService.downloadDocument(id, userId);
        DocumentResponse metadata = documentService.getDocumentMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentMetadata(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentMetadata(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByEntity(@PathVariable Long entityId) {
        List<DocumentResponse> documents = documentService.getDocumentsByEntity(entityId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/entity/{entityId}/type/{type}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByEntityAndType(
            @PathVariable Long entityId,
            @PathVariable DocumentType type) {
        
        List<DocumentResponse> documents = documentService.getDocumentsByEntityAndType(entityId, type);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(
            @RequestHeader("X-User-Id") Long userId) {
        
        List<DocumentResponse> documents = documentService.getMyDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTORANT', 'DIRECTEUR_THESE', 'ADMIN')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        
        documentService.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entity/{entityId}/validate")
    public ResponseEntity<Boolean> hasRequiredDocuments(
            @PathVariable Long entityId,
            @RequestParam("types") List<DocumentType> types) {
        
        boolean hasAll = documentService.hasRequiredDocuments(
            entityId, 
            types.toArray(new DocumentType[0])
        );
        return ResponseEntity.ok(hasAll);
    }
}
