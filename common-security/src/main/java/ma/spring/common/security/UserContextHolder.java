package ma.spring.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Utilitaire pour extraire et valider les informations utilisateur
 * depuis les headers HTTP ajoutés par l'API Gateway
 */
@Component
public class UserContextHolder {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-User-Username";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    /**
     * Extraire l'ID utilisateur depuis les headers
     */
    public Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * Extraire le username depuis les headers
     */
    public String getUsername(HttpServletRequest request) {
        return request.getHeader(USERNAME_HEADER);
    }

    /**
     * Extraire le rôle depuis les headers
     */
    public String getUserRole(HttpServletRequest request) {
        return request.getHeader(USER_ROLE_HEADER);
    }

    /**
     * Vérifier si l'utilisateur a le rôle requis
     */
    public boolean hasRole(HttpServletRequest request, String requiredRole) {
        String userRole = getUserRole(request);
        return userRole != null && userRole.equals(requiredRole);
    }

    /**
     * Vérifier si l'utilisateur a l'un des rôles requis
     */
    public boolean hasAnyRole(HttpServletRequest request, String... requiredRoles) {
        String userRole = getUserRole(request);
        return userRole != null && Arrays.asList(requiredRoles).contains(userRole);
    }

    /**
     * Vérifier si l'utilisateur est un administrateur
     */
    public boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }

    /**
     * Vérifier si l'utilisateur est un doctorant
     */
    public boolean isDoctorant(HttpServletRequest request) {
        return hasRole(request, "DOCTORANT");
    }

    /**
     * Vérifier si l'utilisateur est un directeur de thèse
     */
    public boolean isDirecteur(HttpServletRequest request) {
        return hasRole(request, "DIRECTEUR_THESE");
    }
}
