package ma.spring.userservice.repository;

import ma.spring.userservice.model.User;
<<<<<<< HEAD
import ma.spring.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByEnabled(Boolean enabled);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRole(UserRole role);
}
=======
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
