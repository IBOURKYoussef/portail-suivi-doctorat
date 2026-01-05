package ma.spring.defenseservice;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.spring.defenseservice.model.DefenseResult;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseResultRequest {

    @NotNull(message = "Le résultat est obligatoire")
    private DefenseResult result;

    @Size(max = 200)
    private String mention;

    @Size(max = 2000)
    private String juryObservations;
}