package ma.spring.defenseservice.client;

import ma.spring.defenseservice.dto.UserDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // Ou "test"
public class MockUserServiceClient implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long id) {
        // Simuler une réponse
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setFirstName("Mock");
        user.setLastName("User");
        user.setEmail("user" + id + "@univ.ma");
        user.setRole(id == 2L ? "DIRECTOR" : "DOCTORANT");
        user.setStudentId("STD" + id);
        user.setLaboratoire("Lab " + id);
        return user;
    }

    @Override
    public UserDTO getCurrentUser(String userId) {
        Long id = Long.parseLong(userId);
        return getUserById(id);
    }
}