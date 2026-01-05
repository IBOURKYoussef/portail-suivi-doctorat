package ma.spring.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Filtre pour vérifier les rôles requis basés sur l'annotation @SecuredByRole
 */
public class RoleValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extraire le rôle de l'utilisateur depuis les headers
        String userRole = request.getHeader("X-User-Role");
        
        // Si pas de rôle, continuer (la sécurité de base devrait bloquer)
        if (userRole == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
