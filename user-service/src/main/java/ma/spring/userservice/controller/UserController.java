package ma.spring.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.spring.userservice.dto.RegisterRequest;
import ma.spring.userservice.dto.UserResponse;
import ma.spring.userservice.model.User;
import ma.spring.userservice.model.UserRole;
import ma.spring.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lister tous les utilisateurs (ADMIN only via API Gateway)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserResponse> response = users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .enabled(user.getEnabled())
                        .createdAt(user.getCreatedAt())
                        .studentId(user.getStudentId())
                        .laboratoire(user.getLaboratoire())
                        .grade(user.getGrade())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un utilisateur par ID (pour les autres microservices)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    UserResponse response = UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phone(user.getPhone())
                            .role(user.getRole())
                            .enabled(user.getEnabled())
                            .createdAt(user.getCreatedAt())
                            .studentId(user.getStudentId())
                            .laboratoire(user.getLaboratoire())
                            .grade(user.getGrade())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Utilisateur non trouvé");
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Récupérer un utilisateur par username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    UserResponse response = UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phone(user.getPhone())
                            .role(user.getRole())
                            .enabled(user.getEnabled())
                            .createdAt(user.getCreatedAt())
                            .studentId(user.getStudentId())
                            .laboratoire(user.getLaboratoire())
                            .grade(user.getGrade())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les directeurs de thèse
     * GET /api/users/directors
     */
    @GetMapping("/directors")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF', 'DOCTORANT', 'CANDIDAT')")
    public ResponseEntity<List<UserResponse>> getAllDirectors() {
        List<User> directors = userRepository.findByRole(ma.spring.userservice.model.UserRole.DIRECTEUR_THESE);

        List<UserResponse> response = directors.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .laboratoire(user.getLaboratoire())
                        .grade(user.getGrade())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer le profil actuel (endpoint protégé)
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .map(user -> {
                    UserResponse response = UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .phone(user.getPhone())
                            .role(user.getRole())
                            .enabled(user.getEnabled())
                            .createdAt(user.getCreatedAt())
                            .studentId(user.getStudentId())
                            .laboratoire(user.getLaboratoire())
                            .grade(user.getGrade())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Créer un utilisateur administratif (ADMIN only)
     * POST /api/users/admin/create
     * Permet aux admins de créer d'autres admins ou du personnel administratif
     */
    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdminUser(@Valid @RequestBody RegisterRequest registerRequest, Authentication authentication) {
        // Vérifier que l'utilisateur actuel est bien un ADMIN
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Accès refusé. Seuls les administrateurs peuvent créer des comptes administratifs.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Vérifier que le rôle demandé est bien ADMIN ou ADMINISTRATIF
        if (registerRequest.getRole() != UserRole.ADMIN && registerRequest.getRole() != UserRole.ADMINISTRATIF) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ce endpoint est réservé à la création de comptes ADMIN et ADMINISTRATIF uniquement.");
            return ResponseEntity.badRequest().body(error);
        }

        // Vérifier si le username existe déjà
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ce nom d'utilisateur est déjà utilisé");
            return ResponseEntity.badRequest().body(error);
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cet email est déjà utilisé");
            return ResponseEntity.badRequest().body(error);
        }

        // Créer le nouvel utilisateur administratif
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole());
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        User savedUser = userRepository.save(user);

        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .enabled(savedUser.getEnabled())
                .createdAt(savedUser.getCreatedAt())
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Compte administratif créé avec succès!");
        response.put("user", userResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Activer/Désactiver un utilisateur (ADMIN only)
     * PATCH /api/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(body.get("enabled"));
                    User updatedUser = userRepository.save(user);

                    UserResponse response = UserResponse.builder()
                            .id(updatedUser.getId())
                            .username(updatedUser.getUsername())
                            .email(updatedUser.getEmail())
                            .firstName(updatedUser.getFirstName())
                            .lastName(updatedUser.getLastName())
                            .phone(updatedUser.getPhone())
                            .role(updatedUser.getRole())
                            .enabled(updatedUser.getEnabled())
                            .createdAt(updatedUser.getCreatedAt())
                            .build();

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Supprimer un utilisateur (ADMIN only)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Utilisateur supprimé avec succès");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}