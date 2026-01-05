package ma.spring.registrationservice.repository;

import ma.spring.registrationservice.model.Document;
import ma.spring.registrationservice.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByEntityIdAndActiveTrue(Long entityId);
    
    List<Document> findByTypeAndEntityIdAndActiveTrue(DocumentType type, Long entityId);
    
    List<Document> findByUploadedByAndActiveTrue(Long uploadedBy);
    
    Optional<Document> findByIdAndActiveTrue(Long id);
    
    boolean existsByEntityIdAndTypeAndActiveTrue(Long entityId, DocumentType type);
}
