package ma.spring.defenseservice.dto;

import lombok.*;

// ============= UserDTO.java =============
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private String studentId;
    private String laboratoire;
    private String grade;
}