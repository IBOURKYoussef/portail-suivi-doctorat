# Configuration de la Sécurité Centralisée

## Architecture de Sécurité

La sécurité de l'application est **centralisée dans l'API Gateway** pour simplifier la gestion et garantir une cohérence entre tous les microservices.

### Flux d'Authentification

```
Client → API Gateway → Validation JWT → Microservice
                ↓
         Ajout des headers:
         - X-User-Id
         - X-User-Username
         - X-User-Role
```

## Rôles Définis

Les rôles suivants sont définis dans l'enum `UserRole` :

1. **CANDIDAT** - Candidat au doctorat
2. **DOCTORANT** - Doctorant inscrit
3. **DIRECTEUR_THESE** - Directeur de thèse
4. **ADMINISTRATIF** - Personnel administratif
5. **ADMIN** - Administrateur système

## Configuration JWT

### Variables d'environnement (application.yml de API Gateway)

```yaml
jwt:
  secret: myVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong
  expiration: 86400000  # 24 heures en millisecondes
```

⚠️ **Important** : Changez le secret en production !

## Règles d'Accès par Endpoint

Les règles d'accès sont définies dans `RoleBasedAccessFilter` :

| Endpoint | Rôles Autorisés |
|----------|----------------|
| `/defense/create` | DOCTORANT |
| `/defense/submit` | DOCTORANT |
| `/defense/approve` | DIRECTEUR_THESE, ADMIN |
| `/defense/schedule` | ADMIN |
| `/defense/delete` | ADMIN |
| `/defense/jury` | DIRECTEUR_THESE, ADMIN |
| `/defense/rapporteur` | ADMIN |
| `/registration/campaigns` | ADMIN |
| `/registration/validate` | ADMIN |
| `/notification/send` | ADMIN, ADMINISTRATIF |
| `/users/admin` | ADMIN |

## Comment Utiliser dans les Microservices

### 1. Ajouter la dépendance Spring Security au pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 2. Créer la configuration de sécurité

Chaque microservice doit avoir une classe `SecurityConfig` (déjà créée pour defense-service, registration-service, notification-service).

### 3. Utiliser les annotations @PreAuthorize

```java
@GetMapping("/my-defenses")
@PreAuthorize("hasRole('DOCTORANT')")
public ResponseEntity<?> getMyDefenses(@RequestHeader("X-User-Id") Long userId) {
    // ...
}
```

### 4. Extraire les informations utilisateur depuis les headers

```java
@PostMapping("/create")
public ResponseEntity<?> create(
    @RequestHeader("X-User-Id") Long userId,
    @RequestHeader("X-User-Username") String username,
    @RequestHeader("X-User-Role") String role,
    @RequestBody DefenseRequest request) {
    
    // Utiliser userId pour les opérations
}
```

## Endpoints Publics (Pas d'authentification requise)

Les endpoints suivants sont accessibles sans authentification :

- `/auth/login` - Connexion
- `/auth/register` - Inscription
- `/actuator/**` - Monitoring
- `/eureka/**` - Discovery Server

## Comment Tester

### 1. Obtenir un token JWT

```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123"
}
```

Réponse :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "user@example.com",
    "role": "DOCTORANT"
  }
}
```

### 2. Utiliser le token pour accéder aux endpoints protégés

```bash
GET http://localhost:8080/defense/my
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

## Gestion des Erreurs

### 401 Unauthorized
- Token manquant
- Token invalide
- Token expiré

### 403 Forbidden
- L'utilisateur n'a pas le rôle requis pour accéder à l'endpoint

## Sécurité des Microservices

### Communication Inter-Services

Les microservices communiquent entre eux via Feign Client. Les headers `X-User-*` sont automatiquement propagés.

### Validation des Headers

Chaque microservice vérifie que les headers `X-User-*` sont présents pour les endpoints protégés. Si ces headers sont absents, l'accès est refusé.

## Bibliothèque Commune (common-security)

Une bibliothèque commune a été créée pour faciliter la validation des rôles dans les microservices :

### UserContextHolder

```java
@Autowired
private UserContextHolder userContextHolder;

public void someMethod(HttpServletRequest request) {
    Long userId = userContextHolder.getUserId(request);
    String username = userContextHolder.getUsername(request);
    String role = userContextHolder.getUserRole(request);
    
    if (userContextHolder.isAdmin(request)) {
        // Logique admin
    }
}
```

## Configuration CORS

CORS est configuré dans l'API Gateway pour autoriser les requêtes depuis le frontend Angular :

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: 
              - "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
              - PATCH
            allowedHeaders: "*"
            allowCredentials: true
```

## Recommandations de Sécurité

1. ✅ Utilisez HTTPS en production
2. ✅ Changez le secret JWT en production
3. ✅ Utilisez des tokens avec expiration courte (24h max)
4. ✅ Implémentez un système de refresh token
5. ✅ Loggez toutes les tentatives d'accès non autorisées
6. ✅ Utilisez des variables d'environnement pour les secrets
7. ✅ Implémentez un rate limiting sur l'API Gateway
8. ✅ Validez toujours les entrées utilisateur

## Prochaines Étapes

1. Implémenter le refresh token
2. Ajouter la gestion des sessions invalidées
3. Ajouter l'authentification à deux facteurs (2FA)
4. Implémenter l'audit trail pour les actions sensibles
5. Ajouter la limitation de débit (rate limiting)
