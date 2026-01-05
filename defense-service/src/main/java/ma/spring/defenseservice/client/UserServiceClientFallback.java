package ma.spring.defenseservice.client;

import ma.spring.defenseservice.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long id) {
        log.warn("Fallback called for getUserById: {}", id);
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setFirstName("Fallback");
        user.setLastName("User");
        user.setEmail("fallback@univ.ma");
        user.setRole("DOCTORANT");
        return user;
    }

    @Override
    public UserDTO getCurrentUser(String userId) {
        log.warn("Fallback called for getCurrentUser: {}", userId);
        try {
            Long id = Long.parseLong(userId);
            return getUserById(id);
        } catch (NumberFormatException e) {
            UserDTO user = new UserDTO();
            user.setId(0L);
            user.setFirstName("Unknown");
            user.setLastName("User");
            return user;
        }
    }
}