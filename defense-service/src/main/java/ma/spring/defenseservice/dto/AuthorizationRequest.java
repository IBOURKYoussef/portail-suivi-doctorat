package ma.spring.defenseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

// ============= AuthorizationRequest.java =============
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequest {

    @NotNull(message = "La décision est obligatoire")
    private Boolean authorized;

    @Size(max = 1000)
    private String comment;

    private LocalDateTime defenseDate;

    private String defenseLocation;

    private String defenseRoom;
}

