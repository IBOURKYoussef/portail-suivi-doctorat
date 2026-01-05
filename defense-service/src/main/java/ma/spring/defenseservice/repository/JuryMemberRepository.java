package ma.spring.defenseservice.repository;

import ma.spring.defenseservice.model.JuryMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuryMemberRepository extends JpaRepository<JuryMember, Long> {
    List<JuryMember> findByDefenseId(Long defenseId);
    void deleteByDefenseId(Long defenseId);
}




//package ma.spring.defenseservice.repository;
//
//import ma.spring.defenseservice.model.JuryMember;
//import ma.spring.defenseservice.model.JuryRole;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface JuryMemberRepository extends JpaRepository<JuryMember, Long> {
//
//    // Trouver les membres d'un jury
//    List<JuryMember> findByDefenseId(Long defenseId);
//
//    // Trouver les membres par rôle
//    @Query("SELECT j FROM JuryMember j WHERE j.defense.id = :defenseId AND j.role = :role")
//    List<JuryMember> findByDefenseIdAndRole(@Param("defenseId") Long defenseId, @Param("role") JuryRole role);
//
//    // Compter les membres d'un jury
//    long countByDefenseId(Long defenseId);
//
//    // Vérifier si un email est déjà dans le jury
//    @Query("SELECT COUNT(j) > 0 FROM JuryMember j WHERE j.defense.id = :defenseId AND j.email = :email")
//    boolean existsByDefenseIdAndEmail(@Param("defenseId") Long defenseId, @Param("email") String email);
//}
