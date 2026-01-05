package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ma.spring.defenseservice.model.ReportOpinion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSubmissionRequest {

    @NotBlank(message = "Le rapport est obligatoire")
    @Size(max = 5000)
    private String report;

    private String reportFilePath;

    @NotNull(message = "L'avis est obligatoire")
    private ReportOpinion opinion;
}

