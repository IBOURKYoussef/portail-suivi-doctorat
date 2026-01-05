# ✅ Checklist de Validation des Tests

## 📋 Préparation Avant Tests

### Environnement
- [ ] Java 17+ installé et configuré
- [ ] Maven 3.8+ installé
- [ ] Docker Desktop démarré (si utilisation Docker)
- [ ] PostgreSQL démarré (ou H2 configuré)
- [ ] Port 8080, 8081, 8082, 8083, 8084, 8761 libres
- [ ] PowerShell 5.1+ ou Bash disponible
- [ ] cURL ou Invoke-WebRequest fonctionnel

### Services
- [ ] Discovery Server compilé (`mvnw clean install` dans discovery-server/)
- [ ] Config Server compilé
- [ ] API Gateway compilé
- [ ] User Service compilé
- [ ] Defense Service compilé
- [ ] Registration Service compilé
- [ ] Notification Service compilé

---

## 🚀 Démarrage des Services

### Option A: Docker Compose
- [ ] Fichier `docker-compose.yml` présent à la racine
- [ ] Images Docker construites: `docker-compose build`
- [ ] Services démarrés: `docker-compose up -d`
- [ ] Vérification logs: `docker-compose logs -f`
- [ ] Attendre 60 secondes pour l'initialisation

### Option B: Démarrage Manuel
- [ ] Terminal 1: `cd discovery-server && .\mvnw spring-boot:run`
- [ ] Attendre "Started EurekaServerApplication" (30 sec)
- [ ] Terminal 2: `cd config-server && .\mvnw spring-boot:run`
- [ ] Attendre "Started ConfigServerApplication" (20 sec)
- [ ] Terminal 3: `cd api-gateway && .\mvnw spring-boot:run`
- [ ] Attendre "Started ApiGatewayApplication" (30 sec)
- [ ] Terminal 4: `cd user-service && .\mvnw spring-boot:run`
- [ ] Attendre "Started UserServiceApplication" (25 sec)
- [ ] Terminal 5: `cd defense-service && .\mvnw spring-boot:run`
- [ ] Attendre "Started DefenseServiceApplication" (25 sec)
- [ ] Terminal 6: `cd registration-service && .\mvnw spring-boot:run`
- [ ] Attendre "Started RegistrationServiceApplication" (25 sec)
- [ ] Terminal 7: `cd notification-service && .\mvnw spring-boot:run`
- [ ] Attendre "Started NotificationServiceApplication" (20 sec)

---

## 🔍 Vérification Santé des Services

### Health Checks Manuels
```powershell
# Copier-coller ces commandes une par une
Invoke-WebRequest http://localhost:8761/actuator/health  # Discovery
Invoke-WebRequest http://localhost:8888/actuator/health  # Config Server
Invoke-WebRequest http://localhost:8080/actuator/health  # API Gateway
Invoke-WebRequest http://localhost:8081/actuator/health  # User Service
Invoke-WebRequest http://localhost:8082/actuator/health  # Registration Service
Invoke-WebRequest http://localhost:8083/actuator/health  # Defense Service
Invoke-WebRequest http://localhost:8084/actuator/health  # Notification Service
```

### Résultat Attendu pour Chaque Service
- [ ] Status Code: **200 OK**
- [ ] Response Body: `{"status":"UP"}`
- [ ] Temps de réponse: < 1 seconde

### Eureka Dashboard
- [ ] Ouvrir: http://localhost:8761
- [ ] Voir dans "Instances currently registered with Eureka":
  - [ ] **API-GATEWAY** - Status: UP
  - [ ] **USER-SERVICE** - Status: UP
  - [ ] **DEFENSE-SERVICE** - Status: UP
  - [ ] **REGISTRATION-SERVICE** - Status: UP
  - [ ] **NOTIFICATION-SERVICE** - Status: UP

---

## 🧪 Exécution des Tests

### Lancement du Script
- [ ] Naviguer vers le dossier racine: `cd "d:\project microservices\microservices-doctorat-app"`
- [ ] Donner les droits d'exécution (si nécessaire): `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`
- [ ] Exécuter: `.\test-all-endpoints.ps1`
- [ ] Observer la sortie en temps réel

### Tests Catégorie 1: Infrastructure (2 tests)
- [ ] ✅ Test 1.1: API Gateway Health Check (200 OK)
- [ ] ✅ Test 1.2: Discovery Server Health Check (200 OK)

### Tests Catégorie 2: Authentification (10 tests)
- [ ] ✅ Test 2.1: Inscription Admin (201 Created)
- [ ] ✅ Test 2.2: Connexion Admin (200 OK + JWT Token)
- [ ] ✅ Test 2.3: Inscription Doctorant (201 Created)
- [ ] ✅ Test 2.4: Connexion Doctorant (200 OK + JWT Token)
- [ ] ✅ Test 2.5: Inscription Directeur (201 Created)
- [ ] ✅ Test 2.6: Connexion Directeur (200 OK + JWT Token)
- [ ] ✅ Test 2.7: Inscription Candidat (201 Created)
- [ ] ✅ Test 2.8: Connexion Candidat (200 OK + JWT Token)
- [ ] ✅ Test 2.9: Rejet mauvais credentials (401 Unauthorized)
- [ ] ✅ Test 2.10: Rejet email existant (400 Bad Request)

**Tokens JWT Générés:**
- [ ] Token Admin: `eyJhbGciOiJI...` (commençant par eyJ)
- [ ] Token Doctorant: `eyJhbGciOiJI...`
- [ ] Token Directeur: `eyJhbGciOiJI...`
- [ ] Token Candidat: `eyJhbGciOiJI...`

### Tests Catégorie 3: Sécurité JWT (3 tests)
- [ ] ✅ Test 3.1: Rejet sans token (401 Unauthorized)
- [ ] ✅ Test 3.2: Rejet token invalide (401 Unauthorized)
- [ ] ✅ Test 3.3: Accès avec token valide (200 OK)

### Tests Catégorie 4: Contrôle d'Accès RBAC (4 tests)
- [ ] ✅ Test 4.1: DOCTORANT → /defense/my (200 OK)
- [ ] ✅ Test 4.2: CANDIDAT → /defense/my (403 Forbidden)
- [ ] ✅ Test 4.3: ADMIN → /defense/admin/pending (200 OK)
- [ ] ✅ Test 4.4: DOCTORANT → /defense/admin/pending (403 Forbidden)

### Tests Catégorie 5: User Service (4 tests)
- [ ] ✅ Test 5.1: GET /users/{id} (200 OK)
- [ ] ✅ Test 5.2: GET /users/username/{username} (200 OK)
- [ ] ✅ Test 5.3: GET /users/directors (200 OK)
- [ ] ✅ Test 5.4: GET /users/me (200 OK)

### Tests Catégorie 6: Registration Service (6 tests)
- [ ] ✅ Test 6.1: POST /registration/campaigns (ADMIN) (201 Created)
- [ ] ✅ Test 6.2: POST /registration/campaigns (DOCTORANT) (403 Forbidden)
- [ ] ⚠️ Test 6.3: POST /registration (201 Created ou 400 Bad Request)
- [ ] ✅ Test 6.4: GET /registration/my (200 OK)
- [ ] ✅ Test 6.5: GET /registration/campaigns (200 OK)

### Tests Catégorie 7: Defense Service (12 tests)
- [ ] ⚠️ Test 7.1: POST /defense (DOCTORANT) (201 Created ou 500 Error)
- [ ] ✅ Test 7.2: POST /defense (CANDIDAT) (403 Forbidden)
- [ ] ✅ Test 7.3: GET /defense/my (200 OK)
- [ ] 📋 Test 7.4: GET /defense/{id} (dépend de 7.1)
- [ ] 📋 Test 7.5: POST /defense/{id}/validate-prerequisites (ADMIN) (dépend de 7.1)
- [ ] 📋 Test 7.6: DOCTORANT → validate-prerequisites (403 Forbidden)
- [ ] ✅ Test 7.7: GET /defense/statistics (200 OK)
- [ ] ✅ Test 7.8: GET /defense/admin/pending (200 OK)
- [ ] ✅ Test 7.9: GET /defense/director/pending (200 OK)
- [ ] ✅ Test 7.10: GET /defense/scheduled (200 OK)

### Tests Catégorie 8: Performance (1 test)
- [ ] ✅ Test 8.1: Latence endpoint simple (< 500ms)

---

## 📊 Résultats Attendus

### Scénario Idéal (100%)
```
═══════════════════════════════════════════════════════
  RÉSULTATS FINAUX
═══════════════════════════════════════════════════════
  Total Tests    : 45
  ✓ Réussis      : 45
  ✗ Échoués      : 0
  Taux de réussite: 100%
═══════════════════════════════════════════════════════

🎉 TOUS LES TESTS SONT RÉUSSIS ! 🎉
```

- [ ] **Taux de réussite: 100%**
- [ ] Aucune erreur 500 Internal Server Error
- [ ] Tous les tokens JWT générés correctement
- [ ] RBAC fonctionne comme attendu

### Scénario Réaliste (95%+)
```
═══════════════════════════════════════════════════════
  RÉSULTATS FINAUX
═══════════════════════════════════════════════════════
  Total Tests    : 45
  ✓ Réussis      : 43
  ✗ Échoués      : 2
  Taux de réussite: 95.56%
═══════════════════════════════════════════════════════

⚠️  Certains tests ont échoué. Vérifiez les logs ci-dessus.
```

- [ ] **Taux de réussite: ≥ 95%**
- [ ] Tests échoués: Test 6.3 (POST /registration) et Test 7.1 (POST /defense)
- [ ] Cause probable: Propagation header X-User-Id

---

## 🐛 Diagnostic si Tests Échouent

### Problème: Tests 6.3 et 7.1 échouent (400/500 Error)

#### Symptômes
```
✗ FAILED: POST /registration (Expected: 201, Got: 400)
Response: {"message": "directeurTheseId: Le directeur est requis"}

✗ FAILED: POST /defense (Expected: 201, Got: 500)
Response: {"error": "User not found with id: null"}
```

#### Checklist de Diagnostic
- [ ] Vérifier logs API Gateway: `docker-compose logs api-gateway | Select-String "X-User"`
- [ ] Vérifier logs User Service: `docker-compose logs user-service | Select-String "userId"`
- [ ] Inspecter JWT Token:
  ```powershell
  $token = "eyJhbGciOiJI..."  # Token obtenu du Test 2.4
  $parts = $token.Split('.')
  $payload = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($parts[1] + "=="))
  $payload | ConvertFrom-Json
  ```
- [ ] Vérifier présence du claim `userId` dans le JWT
- [ ] Tester manuellement propagation header:
  ```powershell
  curl -H "Authorization: Bearer $token" http://localhost:8080/users/me -v
  # Chercher dans la réponse: X-User-Id header
  ```

#### Solution
- [ ] Ouvrir [api-gateway/src/main/java/ma/inscription/apigateway/security/JwtAuthenticationFilter.java](api-gateway/src/main/java/ma/inscription/apigateway/security/JwtAuthenticationFilter.java)
- [ ] Ligne 42-48: Ajouter validation `userId != null`
- [ ] Recompiler: `mvn clean install`
- [ ] Redémarrer API Gateway
- [ ] Re-tester

### Problème: Service non accessible (Connection Refused)

#### Checklist
- [ ] Vérifier port occupé: `netstat -ano | findstr "8080"`
- [ ] Tuer process si nécessaire: `taskkill /PID <PID> /F`
- [ ] Vérifier logs du service: `docker-compose logs <service-name>`
- [ ] Vérifier enregistrement Eureka: http://localhost:8761
- [ ] Attendre 30 secondes supplémentaires
- [ ] Redémarrer le service spécifique

### Problème: Token Expired

#### Checklist
- [ ] Vérifier expiration JWT (24h par défaut)
- [ ] Reconnecter pour obtenir nouveau token:
  ```powershell
  $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
      -Method POST -ContentType "application/json" `
      -Body '{"username": "doctorant.test@example.com", "password": "Doctorant123!"}'
  $newToken = $loginResponse.token
  ```

---

## 📁 Fichiers Générés Après Tests

### Fichier de Résultats JSON
- [ ] Fichier créé: `test-results-YYYYMMDD-HHMMSS.json`
- [ ] Contient: total_tests, passed, failed, pass_rate
- [ ] Contient: tokens générés (truncated)
- [ ] Contient: IDs créés (campaign_id, registration_id, defense_id)

### Logs de Sortie
- [ ] Sortie console sauvegardée (Copier-coller dans fichier texte)
- [ ] Tests passés/échoués clairement identifiés
- [ ] Temps d'exécution total: 3-5 minutes

---

## 📈 Métriques de Performance

### Latences Mesurées
- [ ] Authentification (POST /auth/login): **< 300ms**
- [ ] Lecture simple (GET /users/me): **< 100ms**
- [ ] Écriture (POST /defense): **< 500ms**
- [ ] Endpoints admin (GET /defense/statistics): **< 200ms**

### Objectifs de Performance
- [ ] ✅ Latence P95 < 500ms
- [ ] ✅ Latence moyenne < 200ms
- [ ] ✅ Throughput > 100 req/s (non testé automatiquement)
- [ ] ✅ Taux d'erreur < 0.1%

---

## 🎯 Validation Finale

### Sécurité
- [ ] ✅ JWT validation fonctionne (Test 3.1-3.3 passés)
- [ ] ✅ RBAC fonctionne (Test 4.1-4.4 passés)
- [ ] ✅ Pas d'accès non autorisé possible
- [ ] ✅ Headers X-User-* propagés correctement
- [ ] ✅ Tokens expiration configurée

### Fonctionnalités
- [ ] ✅ Authentification robuste (10/10 tests)
- [ ] ✅ Gestion utilisateurs fonctionnelle (4/4 tests)
- [ ] ⚠️ Registration Service (5/6 tests - bug potentiel)
- [ ] ⚠️ Defense Service (11/12 tests - bug potentiel)
- [ ] ✅ Infrastructure stable (2/2 tests)

### Qualité Code
- [ ] Architecture microservices respectée
- [ ] Séparation des responsabilités claire
- [ ] Code bien structuré et lisible
- [ ] Configuration externalisée
- [ ] Logs appropriés

---

## ✅ Checklist Production

### Avant Déploiement
- [ ] **Tous les tests passent à 100%**
- [ ] Base de données PostgreSQL configurée (pas H2)
- [ ] Variables d'environnement sécurisées
- [ ] Secrets externalisés (pas en dur dans le code)
- [ ] HTTPS activé sur API Gateway
- [ ] CORS configuré correctement
- [ ] Rate limiting implémenté
- [ ] Logging centralisé (ELK Stack ou équivalent)
- [ ] Monitoring configuré (Prometheus + Grafana)
- [ ] Alerting en place
- [ ] Backups automatiques planifiés
- [ ] Documentation API à jour (Swagger)
- [ ] Tests de charge réussis (JMeter/Gatling)
- [ ] Tests de pénétration réussis
- [ ] Plan de rollback préparé

### Après Déploiement
- [ ] Smoke tests en production
- [ ] Monitoring actif pendant 24h
- [ ] Logs surveillés pour erreurs
- [ ] Performance validée en conditions réelles
- [ ] Feedback utilisateurs collecté

---

## 📞 Support et Documentation

### Si Vous Êtes Bloqué
1. [ ] Consulter [GUIDE_EXECUTION_TESTS.md](./GUIDE_EXECUTION_TESTS.md) - Section "Dépannage"
2. [ ] Consulter [ANALYSE_TESTS_ENDPOINTS.md](./ANALYSE_TESTS_ENDPOINTS.md) - Section "Problèmes Identifiés"
3. [ ] Vérifier les logs des services concernés
4. [ ] Tester manuellement l'endpoint qui échoue avec cURL

### Documentation Disponible
- [ ] [test-all-endpoints.ps1](./test-all-endpoints.ps1) - Script de test PowerShell
- [ ] [test-all-endpoints.sh](./test-all-endpoints.sh) - Script de test Bash
- [ ] [ANALYSE_TESTS_ENDPOINTS.md](./ANALYSE_TESTS_ENDPOINTS.md) - Analyse complète (600+ lignes)
- [ ] [GUIDE_EXECUTION_TESTS.md](./GUIDE_EXECUTION_TESTS.md) - Guide pratique (400+ lignes)
- [ ] [RECAPITULATIF_TESTS.md](./RECAPITULATIF_TESTS.md) - Résumé exécutif
- [ ] [SECURITY.md](./SECURITY.md) - Architecture sécurité
- [ ] [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Documentation API

---

## 🏁 Conclusion

Cette checklist vous guide étape par étape dans la validation complète de votre application.

**Objectif:** ✅ **100% des tests passés** avant mise en production

**En cas de problème:**
- Les tests 6.3 et 7.1 peuvent échouer (problème de propagation X-User-Id connu)
- Solution documentée dans ANALYSE_TESTS_ENDPOINTS.md
- Score acceptable: ≥ 95% (43/45 tests)

**Bonne chance ! 🚀**
