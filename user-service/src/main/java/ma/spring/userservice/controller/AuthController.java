package ma.spring.userservice.controller;

<<<<<<< HEAD
import jakarta.validation.Valid;
import ma.spring.userservice.config.JwtTokenProvider;
import ma.spring.userservice.dto.*;
import ma.spring.userservice.model.User;
import ma.spring.userservice.model.UserRole;
import ma.spring.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
=======
import ma.spring.userservice.config.JwtTokenProvider;
import ma.spring.userservice.dto.JwtAuthenticationResponse;
import ma.spring.userservice.dto.LoginRequest;
import ma.spring.userservice.dto.RegisterRequest;
import ma.spring.userservice.model.User;
import ma.spring.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Map;

=======
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
<<<<<<< HEAD
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Récupérer l'utilisateur pour générer le token avec les bonnes informations
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String jwt = tokenProvider.generateTokenFromUser(user);

            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .enabled(user.getEnabled())
                    .studentId(user.getStudentId())
                    .laboratoire(user.getLaboratoire())
                    .grade(user.getGrade())
                    .build();

            JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt);
            response.setUser(userResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Identifiants invalides");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
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

        // Créer le nouvel utilisateur
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.CANDIDAT);
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        // Informations spécifiques selon le rôle
        if (user.getRole() == UserRole.CANDIDAT || user.getRole() == UserRole.DOCTORANT) {
            user.setStudentId(registerRequest.getStudentId());
        } else if (user.getRole() == UserRole.DIRECTEUR_THESE) {
            user.setLaboratoire(registerRequest.getLaboratoire());
            user.setGrade(registerRequest.getGrade());
        }

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
                .studentId(savedUser.getStudentId())
                .laboratoire(savedUser.getLaboratoire())
                .grade(savedUser.getGrade())
                .build();

        // Générer un token JWT pour l'utilisateur nouvellement enregistré
        String jwt = tokenProvider.generateTokenFromUser(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Utilisateur enregistré avec succès!");
        response.put("user", userResponse);
        response.put("token", jwt);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse userResponse = UserResponse.builder()
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

        return ResponseEntity.ok(userResponse);
    }
}
=======
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("DOCTORANT"); // Rôle par défaut

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}





>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
