package ma.spring.defenseservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseDocument {

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    private String fileName;

    private String filePath;

    private LocalDateTime uploadedAt;
}
