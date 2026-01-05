package ma.spring.notificationservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utilitaire pour extraire les informations utilisateur depuis les headers HTTP
 * Ces headers sont ajoutés par l'API Gateway après validation du JWT
 */
@Component
public class UserContext {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-User-Username";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    public Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return userId != null ? Long.parseLong(userId) : null;
    }

    public String getUsername(HttpServletRequest request) {
        return request.getHeader(USERNAME_HEADER);
    }

    public String getUserRole(HttpServletRequest request) {
        return request.getHeader(USER_ROLE_HEADER);
    }

    public boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(getUserRole(request));
    }

    public boolean isAdministratif(HttpServletRequest request) {
        return "ADMINISTRATIF".equals(getUserRole(request));
    }

    public boolean canSendNotification(HttpServletRequest request) {
        String role = getUserRole(request);
        return "ADMIN".equals(role) || "ADMINISTRATIF".equals(role);
    }
}
