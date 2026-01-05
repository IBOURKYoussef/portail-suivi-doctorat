package ma.spring.defenseservice.mapper;

import ma.spring.defenseservice.dto.*;
import ma.spring.defenseservice.model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DefenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctorantId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "juryMembers", ignore = true)
    @Mapping(target = "rapporteurs", ignore = true)
    @Mapping(target = "defenseDate", ignore = true)
    @Mapping(target = "defenseLocation", ignore = true)
    @Mapping(target = "defenseRoom", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "juryRemarks", ignore = true)
    @Mapping(target = "mention", ignore = true)
    @Mapping(target = "directorComment", ignore = true)
    @Mapping(target = "adminComment", ignore = true)
    @Mapping(target = "directorApprovalDate", ignore = true)
    @Mapping(target = "adminApprovalDate", ignore = true)
    @Mapping(target = "authorizationDate", ignore = true)
    @Mapping(target = "submissionDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Defense toEntity(DefenseRequest request);

    DefenseResponse toResponse(Defense defense);

    List<DefenseResponse> toResponseList(List<Defense> defenses);

    JuryMemberDTO toJuryMemberDTO(JuryMember juryMember);

    List<JuryMemberDTO> toJuryMemberDTOList(List<JuryMember> juryMembers);

    RapporteurDTO toRapporteurDTO(Rapporteur rapporteur);

    List<RapporteurDTO> toRapporteurDTOList(List<Rapporteur> rapporteurs);
}
//package ma.spring.defenseservice.mapper;
//
//import ma.spring.defenseservice.dto.*;
//import ma.spring.defenseservice.model.*;
//import org.mapstruct.*;
//
//import java.util.List;
//
//@Mapper(
//        componentModel = "spring",
//        uses = {}, // Vous pouvez ajouter d'autres mappers ici si nécessaire
//        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
//        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
//        unmappedTargetPolicy = ReportingPolicy.IGNORE
//)
//public interface DefenseMapper {
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "doctorantId", ignore = true)
//    @Mapping(target = "status", ignore = true)
//    @Mapping(target = "juryMembers", ignore = true)
//    @Mapping(target = "rapporteurs", ignore = true)
//    @Mapping(target = "defenseDate", ignore = true)
//    @Mapping(target = "defenseLocation", ignore = true)
//    @Mapping(target = "defenseRoom", ignore = true)
//    @Mapping(target = "result", ignore = true)
//    @Mapping(target = "juryRemarks", ignore = true)
//    @Mapping(target = "mention", ignore = true)
//    @Mapping(target = "directorComment", ignore = true)
//    @Mapping(target = "adminComment", ignore = true)
//    @Mapping(target = "directorApprovalDate", ignore = true)
//    @Mapping(target = "adminApprovalDate", ignore = true)
//    @Mapping(target = "authorizationDate", ignore = true)
//    @Mapping(target = "submissionDate", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "documentPaths", source = "documentPaths")
//    Defense toEntity(DefenseRequest request);
//
//    DefenseResponse toResponse(Defense defense);
//
//    List<DefenseResponse> toResponseList(List<Defense> defenses);
//
//    // Ajoutez ces méthodes si elles manquent
//    JuryMemberDTO toJuryMemberDTO(JuryMember juryMember);
//
//    List<JuryMemberDTO> toJuryMemberDTOList(List<JuryMember> juryMembers);
//
//    RapporteurDTO toRapporteurDTO(Rapporteur rapporteur);
//
//    List<RapporteurDTO> toRapporteurDTOList(List<Rapporteur> rapporteurs);
//}