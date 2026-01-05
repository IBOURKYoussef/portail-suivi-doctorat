package ma.spring.defenseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.spring.defenseservice.model.DocumentType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private DocumentType type;
    private Long entityId;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
    private String description;
    private String downloadUrl;
}
