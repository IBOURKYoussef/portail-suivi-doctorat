package ma.spring.defenseservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre pour extraire les informations d'authentification des headers HTTP
 * ajoutés par l'API Gateway (X-User-Id, X-User-Username, X-User-Role)
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Extraire les headers ajoutés par l'API Gateway
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Username");
        String role = request.getHeader("X-User-Role");

        logger.info("=== HeaderAuthenticationFilter ===");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("X-User-Id: {}", userId);
        logger.info("X-User-Username: {}", username);
        logger.info("X-User-Role: {}", role);

        if (userId != null && username != null && role != null) {
            // Créer une authentification basée sur les headers
            // Le Gateway envoie le rôle sans préfixe (ex: ADMIN)
            // Mais hasRole('ADMIN') cherche 'ROLE_ADMIN', donc on doit ajouter le préfixe
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    Collections.singletonList(authority)
                );
            
            // Ajouter l'ID utilisateur comme détail
            authentication.setDetails(userId);
            
            // Mettre l'authentification dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.info("Authentication set: username={}, authority={}", username, authority);
        } else {
            logger.warn("Missing headers - userId: {}, username: {}, role: {}", userId, username, role);
        }

        filterChain.doFilter(request, response);
    }
}
