package ma.spring.cloud.apigateway.filter;

import ma.spring.cloud.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Filtre pour vérifier les rôles requis pour accéder aux endpoints
 */
@Component
public class RoleBasedAccessFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Définition des règles d'accès par chemin et rôle
    private static final Map<String, List<String>> ROLE_BASED_ACCESS = Map.ofEntries(
            // User Service - Endpoints admin
            Map.entry("/api/users", Arrays.asList("ADMIN")),
            Map.entry("/users", Arrays.asList("ADMIN")),
            
            // Defense Service - Tous les endpoints /api/defenses
            Map.entry("/api/defenses", Arrays.asList("DOCTORANT", "DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/defenses", Arrays.asList("DOCTORANT", "DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/api/defense/requests", Arrays.asList("DOCTORANT", "DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/defense/requests", Arrays.asList("DOCTORANT", "DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/api/defense/approve", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/defense/approve", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/api/defense/schedule", Arrays.asList("ADMIN")),
            Map.entry("/defense/schedule", Arrays.asList("ADMIN")),
            Map.entry("/api/defense/jury", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/defense/jury", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/api/rapporteurs", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            Map.entry("/rapporteurs", Arrays.asList("DIRECTEUR_THESE", "ADMIN")),
            
            // Registration Service
            Map.entry("/api/registration/campaigns", Arrays.asList("ADMIN", "CANDIDAT", "DOCTORANT")),
            Map.entry("/registration/campaigns", Arrays.asList("ADMIN", "CANDIDAT", "DOCTORANT")),
            Map.entry("/api/registration/applications", Arrays.asList("CANDIDAT", "ADMIN")),
            Map.entry("/registration/applications", Arrays.asList("CANDIDAT", "ADMIN")),
            Map.entry("/api/registration/validate", Arrays.asList("ADMIN")),
            Map.entry("/registration/validate", Arrays.asList("ADMIN")),
            
            // Notification Service  
            Map.entry("/api/notifications", Arrays.asList("ADMIN", "ADMINISTRATIF", "DOCTORANT", "CANDIDAT", "DIRECTEUR_THESE")),
            Map.entry("/notifications", Arrays.asList("ADMIN", "ADMINISTRATIF", "DOCTORANT", "CANDIDAT", "DIRECTEUR_THESE")),
            Map.entry("/api/notification/send", Arrays.asList("ADMIN", "ADMINISTRATIF")),
            Map.entry("/notification/send", Arrays.asList("ADMIN", "ADMINISTRATIF")),
            Map.entry("/api/notification/my-notifications", Arrays.asList("ADMIN", "ADMINISTRATIF", "DOCTORANT", "CANDIDAT", "DIRECTEUR_THESE")),
            Map.entry("/notification/my-notifications", Arrays.asList("ADMIN", "ADMINISTRATIF", "DOCTORANT", "CANDIDAT", "DIRECTEUR_THESE"))
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Vérifier si le chemin nécessite une vérification de rôle
        String matchedPath = findMatchingPath(path);
        if (matchedPath == null) {
            // Pas de restriction spécifique, continuer
            return chain.filter(exchange);
        }

        // Extraire le rôle du header ajouté par JwtAuthenticationFilter
        String userRole = request.getHeaders().getFirst("X-User-Role");
        
        if (userRole == null) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Vérifier si l'utilisateur a le rôle requis
        List<String> allowedRoles = ROLE_BASED_ACCESS.get(matchedPath);
        if (!allowedRoles.contains(userRole)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    /**
     * Trouver le chemin correspondant dans les règles d'accès
     */
    private String findMatchingPath(String requestPath) {
        // Chercher une correspondance exacte d'abord
        for (String configuredPath : ROLE_BASED_ACCESS.keySet()) {
            if (requestPath.equals(configuredPath) || requestPath.startsWith(configuredPath)) {
                return configuredPath;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 1; // Après JwtAuthenticationFilter (order=-1)
    }
}
