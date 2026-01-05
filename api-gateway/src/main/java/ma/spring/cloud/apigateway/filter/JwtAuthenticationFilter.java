package ma.spring.cloud.apigateway.filter;

import ma.spring.cloud.apigateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // Liste des endpoints publics qui ne nécessitent pas d'authentification
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/auth/login",
        "/auth/register",
        "/actuator",
        "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        logger.debug("JwtAuthenticationFilter - Path: {}", path);

        // Vérifier si c'est un endpoint public
        if (isPublicEndpoint(path)) {
            logger.debug("Public endpoint, skipping JWT validation");
            return chain.filter(exchange);
        }

        // Extraire le token du header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        logger.debug("Authorization header present: {}", authHeader != null);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        logger.debug("Token extracted, length: {}", token.length());

        // Valider le token
        boolean isValid = jwtUtil.validateToken(token);
        logger.debug("Token validation result: {}", isValid);
        
        if (!isValid) {
            logger.warn("Invalid token");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extraire les informations du token et les ajouter aux headers pour les microservices
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        logger.info("=== JWT VALIDATED ===");
        logger.info("Path: {}", path);
        logger.info("User: {}, Role: {}, ID: {}", username, role, userId);
        logger.info("Adding headers: X-User-Id={}, X-User-Username={}, X-User-Role={}", userId, username, role);

        // Ajouter les informations de l'utilisateur aux headers pour les microservices
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", String.valueOf(userId))
            .header("X-User-Username", username)
            .header("X-User-Role", role)
            .build();

        logger.info("Headers added successfully, forwarding to microservice");
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1; // Haute priorité
    }
}
