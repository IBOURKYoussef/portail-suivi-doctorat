package ma.spring.registrationservice.client;

import ma.spring.registrationservice.dto.UserDTO;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback en cas d'échec de communication avec User Service
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long id) {
        log.error("Fallback: Unable to get user by id {}", id);
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(id);
        fallbackUser.setUsername("Unknown");
        fallbackUser.setFirstName("User");
        fallbackUser.setLastName("Unavailable");
        return fallbackUser;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.error("Fallback: Unable to get user by username {}", username);
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setUsername(username);
        fallbackUser.setFirstName("User");
        fallbackUser.setLastName("Unavailable");
        return fallbackUser;
    }
}
