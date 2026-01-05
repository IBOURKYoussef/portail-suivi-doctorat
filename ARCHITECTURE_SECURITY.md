# Architecture de Sécurité - Application Doctorat

## 📋 Vue d'ensemble

Cette application utilise une **architecture de sécurité centralisée** où l'API Gateway gère toute l'authentification et l'autorisation basée sur les rôles.

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
│  (Angular)  │
└──────┬──────┘
       │ JWT Token
       ▼
┌─────────────────────────────────────────┐
│         API Gateway (Port 8080)         │
│  ┌───────────────────────────────────┐  │
│  │  1. Validation JWT                │  │
│  │  2. Extraction User Info          │  │
│  │  3. Vérification Rôles            │  │
│  │  4. Ajout Headers:                │  │
│  │     - X-User-Id                   │  │
│  │     - X-User-Username             │  │
│  │     - X-User-Role                 │  │
│  └───────────────────────────────────┘  │
└──────┬─────┬─────┬──────┬──────────────┘
       │     │     │      │
       ▼     ▼     ▼      ▼
    ┌────┐┌────┐┌────┐┌────┐
    │User││Def.││Reg.││Not.│
    │Srv ││Srv ││Srv ││Srv │
    └────┘└────┘└────┘└────┘
```

## 🔐 Rôles et Permissions

### Rôles Disponibles

| Rôle | Code | Description |
|------|------|-------------|
| Candidat | `CANDIDAT` | Candidat au doctorat |
| Doctorant | `DOCTORANT` | Doctorant inscrit |
| Directeur de Thèse | `DIRECTEUR_THESE` | Directeur de thèse |
| Administratif | `ADMINISTRATIF` | Personnel administratif |
| Administrateur | `ADMIN` | Administrateur système |

### Matrice des Permissions

| Endpoint | CANDIDAT | DOCTORANT | DIRECTEUR_THESE | ADMINISTRATIF | ADMIN |
|----------|----------|-----------|-----------------|---------------|-------|
| **Authentication** |
| `/auth/register` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/auth/login` | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Défenses** |
| `/defense/create` | ❌ | ✅ | ❌ | ❌ | ✅ |
| `/defense/my` | ❌ | ✅ | ❌ | ❌ | ✅ |
| `/defense/approve` | ❌ | ❌ | ✅ | ❌ | ✅ |
| `/defense/schedule` | ❌ | ❌ | ❌ | ❌ | ✅ |
| `/defense/jury` | ❌ | ❌ | ✅ | ❌ | ✅ |
| **Inscriptions** |
| `/registration/apply` | ✅ | ❌ | ❌ | ❌ | ✅ |
| `/registration/my` | ✅ | ✅ | ❌ | ❌ | ✅ |
| `/registration/validate` | ❌ | ❌ | ❌ | ❌ | ✅ |
| `/registration/campaigns` | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Notifications** |
| `/notification/send` | ❌ | ❌ | ❌ | ✅ | ✅ |
| `/notification/my` | ✅ | ✅ | ✅ | ✅ | ✅ |

## 📦 Composants de Sécurité

### 1. API Gateway

#### Fichiers Principaux
- **JwtUtil.java** : Utilitaires pour valider et extraire les informations du JWT
- **JwtAuthenticationFilter.java** : Filtre global qui valide le JWT et ajoute les headers utilisateur
- **RoleBasedAccessFilter.java** : Filtre qui vérifie les permissions basées sur les rôles
- **SecurityConfig.java** : Configuration de sécurité Spring

#### Configuration (application.yml)
```yaml
jwt:
  secret: myVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong
  expiration: 86400000  # 24 heures

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/users/**,/auth/**
        # ... autres routes
```

### 2. User Service

#### Responsabilités
- Génération des tokens JWT lors du login
- Gestion des utilisateurs (CRUD)
- Validation des credentials

#### Endpoints
- `POST /auth/register` : Inscription
- `POST /auth/login` : Connexion (retourne un JWT)
- `GET /api/users/{id}` : Récupérer un utilisateur

### 3. Microservices (Defense, Registration, Notification)

#### Configuration Simplifiée
Chaque microservice a une configuration de sécurité minimale :

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

#### Utilisation des Headers
```java
@PostMapping("/create")
@PreAuthorize("hasRole('DOCTORANT')")
public ResponseEntity<?> create(
    @RequestHeader("X-User-Id") Long userId,
    @RequestHeader("X-User-Role") String role,
    @RequestBody DefenseRequest request) {
    // Logique métier
}
```

## 🚀 Démarrage

### 1. Démarrer les Services Infrastructure

```bash
# Discovery Server (Eureka)
cd discovery-server
./mvnw spring-boot:run

# Config Server
cd config-server
./mvnw spring-boot:run

# API Gateway
cd api-gateway
./mvnw spring-boot:run
```

### 2. Démarrer les Microservices

```bash
# User Service
cd user-service
./mvnw spring-boot:run

# Defense Service
cd defense-service
./mvnw spring-boot:run

# Registration Service
cd registration-service
./mvnw spring-boot:run

# Notification Service
cd notification-service
./mvnw spring-boot:run
```

### 3. Démarrer le Frontend

```bash
cd frontend-angular
npm install
ng serve
```

### 4. Tester l'Authentification

```bash
# Inscription
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant@example.com",
    "password": "Password123!",
    "email": "doctorant@example.com",
    "firstName": "Jean",
    "lastName": "Dupont",
    "role": "DOCTORANT"
  }'

# Connexion
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant@example.com",
    "password": "Password123!"
  }'

# Réponse attendue
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "doctorant@example.com",
    "role": "DOCTORANT"
  }
}
```

### 5. Accéder aux Endpoints Protégés

```bash
# Utiliser le token reçu
TOKEN="eyJhbGciOiJIUzI1NiIs..."

curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN"
```

## 🔧 Configuration

### Variables d'Environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `JWT_SECRET` | Secret pour signer les JWT | (voir application.yml) |
| `JWT_EXPIRATION` | Durée de validité du token (ms) | 86400000 (24h) |
| `EUREKA_URI` | URL du Discovery Server | http://localhost:8761/eureka |
| `CONFIG_SERVER_URI` | URL du Config Server | http://localhost:8888 |

### Ports par Défaut

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| User Service | 8081 |
| Registration Service | 8082 |
| Defense Service | 8083 |
| Notification Service | 8084 |
| Discovery Server | 8761 |
| Config Server | 8888 |
| Frontend Angular | 4200 |

## 📚 Documentation

- [SECURITY.md](./SECURITY.md) - Guide complet de sécurité
- [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) - Guide de migration
- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Documentation des APIs

## 🐛 Dépannage

### Problème : 401 Unauthorized

**Symptômes** : Toutes les requêtes retournent 401

**Solutions possibles** :
1. Vérifier que le token est présent dans le header : `Authorization: Bearer <token>`
2. Vérifier que le token n'est pas expiré
3. Vérifier que le secret JWT est identique dans user-service et api-gateway

### Problème : 403 Forbidden

**Symptômes** : L'authentification fonctionne mais l'accès est refusé

**Solutions possibles** :
1. Vérifier que l'utilisateur a le bon rôle pour l'endpoint
2. Vérifier les logs de RoleBasedAccessFilter
3. Vérifier que les headers X-User-* sont bien propagés

### Problème : Services ne se voient pas

**Symptômes** : Erreur "No instances available"

**Solutions possibles** :
1. Vérifier que le Discovery Server est démarré
2. Vérifier que tous les services sont enregistrés dans Eureka (http://localhost:8761)
3. Attendre 30 secondes pour que les services s'enregistrent

## 🛡️ Sécurité en Production

### Checklist de Déploiement

- [ ] Changer le secret JWT
- [ ] Utiliser HTTPS
- [ ] Configurer CORS correctement
- [ ] Activer le rate limiting
- [ ] Implémenter le refresh token
- [ ] Configurer les logs d'audit
- [ ] Utiliser des variables d'environnement pour les secrets
- [ ] Activer la surveillance (Prometheus/Grafana)
- [ ] Configurer les alertes de sécurité
- [ ] Tester tous les scénarios d'accès

### Recommandations

1. **Tokens** : Utilisez des tokens de courte durée (1-2h) avec refresh tokens
2. **HTTPS** : Obligatoire en production
3. **Rate Limiting** : Limitez les tentatives de connexion
4. **Monitoring** : Surveillez les tentatives d'accès non autorisées
5. **Logs** : Loggez toutes les actions sensibles

## 📖 Ressources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [OAuth 2.0](https://oauth.net/2/)

## 👥 Support

Pour toute question ou problème :
1. Consultez la documentation dans `/docs`
2. Vérifiez les issues GitHub
3. Contactez l'équipe de développement

## 📝 License

Ce projet est sous licence MIT.
