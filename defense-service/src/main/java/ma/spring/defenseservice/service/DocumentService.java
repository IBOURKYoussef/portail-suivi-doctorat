package ma.spring.defenseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.defenseservice.dto.DocumentResponse;
import ma.spring.defenseservice.exception.BusinessException;
import ma.spring.defenseservice.exception.ResourceNotFoundException;
import ma.spring.defenseservice.model.Document;
import ma.spring.defenseservice.model.DocumentType;
import ma.spring.defenseservice.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.document.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.document.max-file-size:10485760}") // 10MB par défaut
    private Long maxFileSize;

    @Transactional
    public DocumentResponse uploadDocument(
            MultipartFile file,
            DocumentType type,
            Long entityId,
            Long uploadedBy,
            String description) {

        // Validation du fichier
        try {
            validateFile(file);
        } catch (BusinessException e) {
            log.error("File validation failed: {}", e.getMessage());
            throw e;
        }
        
        log.info("Uploading document - Type: {}, EntityId: {}, OriginalFileName: {}", 
                type, entityId, file.getOriginalFilename());

        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new BusinessException("Impossible de créer le répertoire de stockage");
        }

        // Générer un nom de fichier unique
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        String filePath = uploadPath.resolve(fileName).toString();

        try {
            // Copier le fichier
            Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

            // Sauvegarder les métadonnées
            Document document = Document.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .type(type)
                    .entityId(entityId)
                    .uploadedBy(uploadedBy)
                    .uploadedAt(LocalDateTime.now())
                    .description(description)
                    .active(true)
                    .build();

            Document savedDocument = documentRepository.save(document);
            log.info("Document uploaded: {} for entity: {}", fileName, entityId);

            return mapToResponse(savedDocument);

        } catch (IOException e) {
            log.error("Failed to upload document", e);
            throw new BusinessException("Échec de l'upload du document");
        }
    }

    public Resource downloadDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("Document downloaded: {} by user: {}", document.getFileName(), userId);
                return resource;
            } else {
                throw new ResourceNotFoundException("Document introuvable ou illisible");
            }
        } catch (MalformedURLException e) {
            throw new BusinessException("Erreur lors de la lecture du document");
        }
    }

    public DocumentResponse getDocumentMetadata(Long documentId) {
        Document document = documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));
        return mapToResponse(document);
    }

    public List<DocumentResponse> getDocumentsByEntity(Long entityId) {
        return documentRepository.findByEntityIdAndActiveTrue(entityId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DocumentResponse> getDocumentsByEntityAndType(Long entityId, DocumentType type) {
        return documentRepository.findByTypeAndEntityIdAndActiveTrue(type, entityId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DocumentResponse> getMyDocuments(Long userId) {
        return documentRepository.findByUploadedByAndActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndActiveTrue(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));

        // Vérifier que l'utilisateur est le propriétaire
        if (!document.getUploadedBy().equals(userId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à supprimer ce document");
        }

        // Soft delete
        document.setActive(false);
        document.setDeletedAt(LocalDateTime.now());
        documentRepository.save(document);

        log.info("Document deleted: {} by user: {}", document.getFileName(), userId);
    }

    public boolean hasRequiredDocuments(Long entityId, DocumentType... requiredTypes) {
        for (DocumentType type : requiredTypes) {
            if (!documentRepository.existsByEntityIdAndTypeAndActiveTrue(entityId, type)) {
                return false;
            }
        }
        return true;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Le fichier est vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    String.format("Le fichier est trop volumineux. Taille maximale: %d MB", 
                    maxFileSize / 1024 / 1024)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/pdf") && 
             !contentType.startsWith("image/") &&
             !contentType.equals("application/msword") &&
             !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new BusinessException("Type de fichier non autorisé. Formats acceptés: PDF, Images, Word");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .fileSize(document.getFileSize())
                .contentType(document.getContentType())
                .type(document.getType())
                .entityId(document.getEntityId())
                .uploadedBy(document.getUploadedBy())
                .uploadedAt(document.getUploadedAt())
                .description(document.getDescription())
                .downloadUrl("/api/documents/" + document.getId() + "/download")
                .build();
    }
}
