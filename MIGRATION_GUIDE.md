# Guide de Migration vers la Sécurité Centralisée

## Vue d'ensemble

Ce guide explique comment migrer votre code existant pour utiliser la nouvelle architecture de sécurité centralisée.

## Changements Principaux

### 1. API Gateway devient le point de sécurité central
- Toutes les authentifications JWT se font au niveau de l'API Gateway
- Les microservices reçoivent les informations utilisateur via les headers HTTP
- Plus besoin de valider le JWT dans chaque microservice

### 2. Nouveaux Headers HTTP
L'API Gateway ajoute automatiquement ces headers à chaque requête :
- `X-User-Id` : L'ID de l'utilisateur
- `X-User-Username` : Le nom d'utilisateur
- `X-User-Role` : Le rôle de l'utilisateur

### 3. Simplification des Microservices
Les microservices n'ont plus besoin de :
- Configurer JWT
- Valider les tokens
- Décoder les tokens

## Migration Étape par Étape

### Étape 1 : Modifier les Contrôleurs

#### Avant :
```java
@PostMapping("/create")
@PreAuthorize("hasRole('DOCTORANT')")
public ResponseEntity<?> create(
    @RequestBody DefenseRequest request,
    Authentication authentication) {
    
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    // ...
}
```

#### Après :
```java
@PostMapping("/create")
@PreAuthorize("hasRole('DOCTORANT')")
public ResponseEntity<?> create(
    @RequestBody DefenseRequest request,
    @RequestHeader("X-User-Id") Long userId) {
    
    // userId est directement disponible depuis les headers
    // ...
}
```

### Étape 2 : Utiliser UserContext

#### Créer UserContext dans chaque microservice

```java
@Component
public class UserContext {
    public Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        return userId != null ? Long.parseLong(userId) : null;
    }
    
    public String getUserRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }
    
    public boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(getUserRole(request));
    }
}
```

#### Utiliser UserContext dans les Services

```java
@Service
public class DefenseService {
    @Autowired
    private UserContext userContext;
    
    public DefenseResponse createDefense(DefenseRequest request, HttpServletRequest httpRequest) {
        Long userId = userContext.getUserId(httpRequest);
        String role = userContext.getUserRole(httpRequest);
        
        // Logique métier
    }
}
```

### Étape 3 : Simplifier la Configuration de Sécurité

#### Avant (dans chaque microservice) :
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configuration JWT complexe
    // JwtAuthenticationFilter
    // JwtTokenProvider
    // etc.
}
```

#### Après :
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

### Étape 4 : Supprimer les Classes JWT Obsolètes

Dans chaque microservice (sauf user-service), vous pouvez supprimer :
- `JwtUtil.java` ou `JwtTokenProvider.java`
- `JwtAuthenticationFilter.java`
- Configuration JWT dans `application.yml`

### Étape 5 : Mettre à jour les Feign Clients

Si vous utilisez Feign pour la communication inter-services, créez un intercepteur pour propager les headers :

```java
@Component
public class FeignClientInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) 
            RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Propager les headers utilisateur
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Username");
            String role = request.getHeader("X-User-Role");
            
            if (userId != null) template.header("X-User-Id", userId);
            if (username != null) template.header("X-User-Username", username);
            if (role != null) template.header("X-User-Role", role);
        }
    }
}
```

### Étape 6 : Mettre à jour le Frontend

#### Avant :
```typescript
// Requête directe vers le microservice
this.http.get('http://localhost:8081/api/defenses')
```

#### Après :
```typescript
// Toutes les requêtes passent par l'API Gateway
this.http.get('http://localhost:8080/defense')
```

#### Configuration du Service Angular

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';
  
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          // Stocker le token
          localStorage.setItem('token', response.token);
        })
      );
  }
  
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
```

#### Intercepteur HTTP

```typescript
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req);
  }
}
```

## Checklist de Migration

### API Gateway
- [x] Ajouter dépendances Spring Security et JWT
- [x] Créer JwtUtil
- [x] Créer JwtAuthenticationFilter
- [x] Créer RoleBasedAccessFilter
- [x] Configurer SecurityConfig
- [x] Configurer les routes dans application.yml

### User Service
- [ ] Garder la configuration JWT existante (génération de tokens)
- [ ] S'assurer que les endpoints `/auth/login` et `/auth/register` fonctionnent

### Autres Microservices (Defense, Registration, Notification)
- [x] Créer SecurityConfig simplifié
- [x] Créer UserContext
- [ ] Modifier les contrôleurs pour utiliser les headers HTTP
- [ ] Supprimer les classes JWT obsolètes
- [ ] Ajouter @PreAuthorize sur les endpoints sensibles
- [ ] Créer FeignClientInterceptor si nécessaire

### Frontend Angular
- [ ] Modifier les URLs pour pointer vers l'API Gateway
- [ ] Créer un intercepteur JWT
- [ ] Tester tous les flux d'authentification

## Tests

### Test 1 : Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"doctorant@example.com","password":"password"}'
```

### Test 2 : Accès à un endpoint protégé
```bash
curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Test 3 : Accès refusé (mauvais rôle)
```bash
# Avec un token DOCTORANT, essayer d'accéder à un endpoint ADMIN
curl -X POST http://localhost:8080/defense/schedule \
  -H "Authorization: Bearer DOCTORANT_TOKEN"
  
# Devrait retourner 403 Forbidden
```

## Dépannage

### Problème : 401 Unauthorized
**Causes possibles :**
- Token manquant dans le header Authorization
- Token invalide ou expiré
- Secret JWT différent entre user-service et api-gateway

**Solution :**
- Vérifier que le token est présent : `Authorization: Bearer <token>`
- Vérifier que le secret JWT est identique dans user-service et api-gateway

### Problème : 403 Forbidden
**Causes possibles :**
- L'utilisateur n'a pas le rôle requis
- Les headers X-User-* ne sont pas propagés

**Solution :**
- Vérifier le rôle de l'utilisateur dans le token
- Vérifier que JwtAuthenticationFilter ajoute bien les headers

### Problème : Headers X-User-* absents dans les microservices
**Causes possibles :**
- JwtAuthenticationFilter ne fonctionne pas
- Problème de routage dans l'API Gateway

**Solution :**
- Vérifier les logs de l'API Gateway
- Ajouter des logs dans JwtAuthenticationFilter pour déboguer

## Avantages de la Nouvelle Architecture

1. ✅ **Centralisation** : La sécurité est gérée en un seul endroit
2. ✅ **Simplicité** : Les microservices sont plus simples
3. ✅ **Maintenance** : Plus facile de modifier la logique de sécurité
4. ✅ **Performance** : Validation JWT une seule fois au Gateway
5. ✅ **Cohérence** : Tous les services utilisent la même logique
6. ✅ **Évolutivité** : Facile d'ajouter de nouveaux services

## Prochaines Améliorations

1. Implémenter le refresh token
2. Ajouter un système de blacklist pour les tokens révoqués
3. Implémenter l'authentification OAuth2
4. Ajouter la limitation de débit (rate limiting)
5. Implémenter l'audit trail des accès
