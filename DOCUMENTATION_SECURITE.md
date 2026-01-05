# DOCUMENTATION SÉCURITÉ - APPLICATION DOCTORAT

## 🔒 ARCHITECTURE DE SÉCURITÉ

L'application utilise une architecture de sécurité centralisée avec JWT et autorisation basée sur les rôles (RBAC).

### Principe général
```
Client → API Gateway (JWT Validation + RBAC) → Microservices (Method Security)
```

## 🎯 RÔLES DISPONIBLES

### 1. **ADMIN** (Administrateur système)
- Accès complet à tous les services
- Gestion des utilisateurs
- Création/modification des campagnes
- Validation des soutenances
- Envoi de notifications

### 2. **DIRECTEUR_THESE** (Directeur de thèse)
- Validation des demandes de soutenance
- Gestion du jury
- Consultation des soutenances

### 3. **DOCTORANT** (Étudiant doctorant)
- Création de demandes de soutenance
- Consultation de ses propres soutenances
- Réception de notifications

### 4. **CANDIDAT** (Candidat au doctorat)
- Inscription aux campagnes
- Soumission de candidatures
- Consultation des campagnes ouvertes

### 5. **ADMINISTRATIF** (Personnel administratif)
- Envoi de notifications
- Consultation des données
- Support administratif

## 🛡️ COMPOSANTS DE SÉCURITÉ

### 1. API Gateway

#### JwtAuthenticationFilter
**Responsabilités**:
- Valider le token JWT sur toutes les requêtes
- Extraire les informations (userId, username, role, email)
- Ajouter les headers `X-User-*` pour les microservices
- Rejeter les requêtes avec token invalide (401)

**Endpoints publics** (pas de token requis):
- `/auth/login`
- `/auth/register`
- `/actuator/**`
- `/eureka`

#### RoleBasedAccessFilter
**Responsabilités**:
- Vérifier que l'utilisateur a le rôle requis
- Rejeter les accès non autorisés (403)

**Règles d'accès**:

| Endpoint | Rôles autorisés |
|----------|----------------|
| `/api/users`, `/users` | ADMIN |
| `/api/defense/requests` | DOCTORANT, DIRECTEUR_THESE, ADMIN |
| `/api/defense/approve` | DIRECTEUR_THESE, ADMIN |
| `/api/defense/schedule` | ADMIN |
| `/api/defense/jury` | DIRECTEUR_THESE, ADMIN |
| `/api/registration/campaigns` | ADMIN, CANDIDAT, DOCTORANT |
| `/api/registration/applications` | CANDIDAT, ADMIN |
| `/api/registration/validate` | ADMIN |
| `/api/notification/send` | ADMIN, ADMINISTRATIF |
| `/api/notification/my-notifications` | Tous les rôles authentifiés |

### 2. Microservices

#### HeaderAuthenticationFilter
Présent dans chaque microservice (defense, registration, notification).

**Responsabilités**:
- Extraire les headers `X-User-*` envoyés par le Gateway
- Créer une authentification Spring Security
- Ajouter le rôle avec préfixe `ROLE_` (requis par Spring)
- Placer l'authentification dans le SecurityContext

**Code**:
```java
String role = request.getHeader("X-User-Role");
SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
UsernamePasswordAuthenticationToken authentication = 
    new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
SecurityContextHolder.getContext().setAuthentication(authentication);
```

#### SecurityConfig
Configuration de sécurité pour chaque microservice.

**Principe**:
- Les microservices font **confiance** à l'API Gateway
- Tous les endpoints sont `permitAll()` au niveau HTTP
- La sécurité est appliquée au niveau **méthode** avec `@PreAuthorize`

#### @EnableMethodSecurity
Activé dans chaque microservice pour utiliser les annotations de sécurité.

## 📝 ANNOTATIONS DE SÉCURITÉ

### @PreAuthorize
Utilisée dans les controllers pour restreindre l'accès par rôle.

**Exemples**:

```java
// Accès réservé aux ADMIN
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/campaigns")
public ResponseEntity<?> createCampaign(...) { }

// Accès à plusieurs rôles
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRATIF')")
@PostMapping("/notifications")
public ResponseEntity<?> sendNotification(...) { }

// Accès aux DOCTORANT
@PreAuthorize("hasRole('DOCTORANT')")
@PostMapping("/defenses")
public ResponseEntity<?> submitDefense(...) { }
```

## 🔐 FLUX D'AUTHENTIFICATION

### 1. Inscription/Connexion
```
Client → POST /auth/register ou /auth/login
       → User Service
       → Génération JWT avec claims (userId, role, email)
       → Retour du token au client
```

### 2. Requête protégée
```
Client → GET /api/users
       → Header: Authorization: Bearer {token}
       → API Gateway
       → JwtAuthenticationFilter valide le token
       → Extraction des claims
       → Ajout des headers X-User-*
       → RoleBasedAccessFilter vérifie le rôle
       → Routage vers User Service
       → HeaderAuthenticationFilter extrait les headers
       → SecurityContext créé avec le rôle
       → @PreAuthorize("hasRole('ADMIN')") vérifie
       → Traitement de la requête
```

### 3. Rejet (403 Forbidden)
```
Si le rôle ne correspond pas:
- RoleBasedAccessFilter (Gateway): 403
- @PreAuthorize (Microservice): 403
```

### 4. Rejet (401 Unauthorized)
```
Si le token est invalide/absent:
- JwtAuthenticationFilter (Gateway): 401
```

## 🧪 TESTS DE SÉCURITÉ

### Test 1: Accès sans token
```http
GET /api/users
```
**Résultat attendu**: 401 Unauthorized

### Test 2: Token invalide
```http
GET /api/users
Authorization: Bearer invalid.token.here
```
**Résultat attendu**: 401 Unauthorized

### Test 3: Rôle insuffisant
```http
GET /api/users
Authorization: Bearer {token_doctorant}
```
**Résultat attendu**: 403 Forbidden (seul ADMIN peut accéder)

### Test 4: Accès autorisé
```http
GET /api/users
Authorization: Bearer {token_admin}
```
**Résultat attendu**: 200 OK avec liste des utilisateurs

## 📋 MATRICE D'AUTORISATION

| Service | Endpoint | ADMIN | DIRECTEUR | DOCTORANT | CANDIDAT | ADMINISTRATIF |
|---------|----------|-------|-----------|-----------|----------|---------------|
| **User Service** |
| Liste users | GET /api/users | ✅ | ❌ | ❌ | ❌ | ❌ |
| Détails user | GET /api/users/{id} | ✅ | ❌ | ❌ | ❌ | ❌ |
| Maj user | PUT /api/users/{id} | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Defense Service** |
| Créer soutenance | POST /api/defenses | ✅ | ❌ | ✅ | ❌ | ❌ |
| Liste soutenances | GET /api/defenses | ✅ | ✅ | ✅ | ❌ | ❌ |
| Approuver | POST /api/defenses/approve | ✅ | ✅ | ❌ | ❌ | ❌ |
| Planifier | POST /api/defenses/schedule | ✅ | ❌ | ❌ | ❌ | ❌ |
| Gérer jury | POST /api/defenses/jury | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Registration Service** |
| Créer campagne | POST /api/campaigns | ✅ | ❌ | ❌ | ❌ | ❌ |
| Liste campagnes | GET /api/campaigns | ✅ | ❌ | ✅ | ✅ | ❌ |
| Candidater | POST /api/applications | ✅ | ❌ | ❌ | ✅ | ❌ |
| Valider candidature | POST /api/applications/validate | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Notification Service** |
| Envoyer notification | POST /api/notifications | ✅ | ❌ | ❌ | ❌ | ✅ |
| Mes notifications | GET /api/notifications/user/{id} | ✅ | ✅ | ✅ | ✅ | ✅ |

## 🔧 CONFIGURATION

### JWT Secret Key
Défini dans:
- `api-gateway/application.yml`
- `user-service/application.yml`

```yaml
jwt:
  secret: myVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong
  expiration: 86400000  # 24 heures
```

### Headers transmis par Gateway
```
X-User-Id: {userId}
X-User-Username: {username}
X-User-Role: {role}
```

### Token JWT Structure
```json
{
  "sub": "username",
  "userId": 123,
  "role": "ADMIN",
  "email": "user@example.com",
  "iat": 1703520000,
  "exp": 1703606400
}
```

## 🚨 SÉCURITÉ EN PRODUCTION

### Recommandations

1. **JWT Secret**: Utilisez une clé forte et unique
   ```bash
   openssl rand -base64 64
   ```

2. **HTTPS**: Toujours utiliser HTTPS en production

3. **Token Expiration**: Ajustez selon vos besoins
   - Court (1h): Plus sécurisé, nécessite refresh
   - Long (24h): Plus pratique, moins sécurisé

4. **Refresh Token**: Implémentez un système de refresh token

5. **Rate Limiting**: Limitez les tentatives de login

6. **Logging**: Loggez tous les accès refusés (403, 401)

7. **Monitoring**: Surveillez les tentatives d'accès suspects

## 📚 BONNES PRATIQUES

### 1. Toujours valider le token dans le Gateway
Ne jamais faire confiance au client.

### 2. Double vérification
- Gateway: RBAC par chemin
- Microservice: @PreAuthorize par méthode

### 3. Principe du moindre privilège
Donnez le minimum de permissions nécessaires.

### 4. Auditabilité
Loggez qui fait quoi et quand.

### 5. Headers sécurisés
Les headers X-User-* ne doivent venir QUE du Gateway.

## 🐛 DÉBOGAGE

### Token invalide
```bash
# Décoder un JWT
echo "eyJhbGc..." | base64 -d
```

### Vérifier les headers
```java
@GetMapping("/debug")
public ResponseEntity<?> debug(HttpServletRequest request) {
    String userId = request.getHeader("X-User-Id");
    String role = request.getHeader("X-User-Role");
    // ...
}
```

### Logs Spring Security
```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

## ✅ CHECKLIST DE SÉCURITÉ

- [x] JWT validé dans le Gateway
- [x] RBAC configuré dans RoleBasedAccessFilter
- [x] HeaderAuthenticationFilter dans chaque microservice
- [x] @EnableMethodSecurity activé partout
- [x] @PreAuthorize sur les endpoints sensibles
- [x] Secret JWT identique Gateway/User-Service
- [x] Token contient userId, role, email
- [x] Headers X-User-* transmis correctement
- [x] SecurityContext créé dans les microservices
- [x] Tests de sécurité en place

---

**Documentation mise à jour**: 25 décembre 2025
