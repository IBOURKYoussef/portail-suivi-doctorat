package ma.spring.userservice.config;

import ma.spring.userservice.model.User;
import ma.spring.userservice.model.UserRole;
import ma.spring.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de base au démarrage de l'application
 * Crée un compte administrateur par défaut si aucun n'existe
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@doctorat.ma}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminAccount();
    }

    /**
     * Crée un compte administrateur par défaut si aucun admin n'existe
     */
    private void initializeAdminAccount() {
        // Vérifier s'il existe déjà un administrateur
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        
        if (adminCount == 0) {
            logger.info("Aucun compte administrateur trouvé. Création du compte admin par défaut...");
            
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setFirstName("Administrateur");
            admin.setLastName("Système");
            admin.setPhone("+212600000000");
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            
            userRepository.save(admin);
            
            logger.info("========================================");
            logger.info("Compte administrateur créé avec succès!");
            logger.info("Username: {}", adminUsername);
            logger.info("Password: {}", adminPassword);
            logger.info("Email: {}", adminEmail);
            logger.info("IMPORTANT: Changez ce mot de passe dès la première connexion!");
            logger.info("========================================");
        } else {
            logger.info("Compte(s) administrateur(s) déjà existant(s) ({} compte(s))", adminCount);
        }
    }
}
