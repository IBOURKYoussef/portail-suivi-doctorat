# 🚀 Guide d'Exécution des Tests

## Prérequis

### 1. Services Démarrés

Assurez-vous que tous les services sont démarrés :

```powershell
# Vérifier les ports
netstat -ano | findstr "8080 8081 8082 8083 8084 8761"
```

Ports requis :
- ✅ 8080 - API Gateway
- ✅ 8081 - User Service
- ✅ 8082 - Registration Service
- ✅ 8083 - Defense Service
- ✅ 8084 - Notification Service
- ✅ 8761 - Eureka Discovery Server

### 2. Démarrer les Services

#### Option A: Docker Compose (Recommandé)

```powershell
cd "d:\project microservices\microservices-doctorat-app"
docker-compose up -d
```

#### Option B: Démarrage Manuel

```powershell
# Terminal 1 - Discovery Server
cd discovery-server
.\mvnw spring-boot:run

# Terminal 2 - Config Server
cd config-server
.\mvnw spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway
.\mvnw spring-boot:run

# Terminal 4 - User Service
cd user-service
.\mvnw spring-boot:run

# Terminal 5 - Registration Service
cd registration-service
.\mvnw spring-boot:run

# Terminal 6 - Defense Service
cd defense-service
.\mvnw spring-boot:run

# Terminal 7 - Notification Service
cd notification-service
.\mvnw spring-boot:run
```

Attendez 30-60 secondes pour que tous les services s'enregistrent dans Eureka.

### 3. Vérifier la Santé des Services

```powershell
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Defense Service
curl http://localhost:8083/actuator/health

# Registration Service
curl http://localhost:8082/actuator/health

# Eureka
curl http://localhost:8761/actuator/health
```

---

## 🧪 Exécution des Tests

### Option 1: PowerShell (Windows)

```powershell
cd "d:\project microservices\microservices-doctorat-app"

# Donner les droits d'exécution (si nécessaire)
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# Exécuter les tests
.\test-all-endpoints.ps1
```

**Sortie attendue:**
```
═══════════════════════════════════════════════════════
   Tests Automatisés - Application Doctorat
   Date: 2025-12-25 14:30:15
═══════════════════════════════════════════════════════

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1. Tests d'Infrastructure
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Test 1.1: Vérification de l'API Gateway
✓ PASSED: API Gateway Health Check (Status: 200)
...
```

### Option 2: Bash (Git Bash / WSL)

```bash
cd "/d/project microservices/microservices-doctorat-app"

# Donner les droits d'exécution
chmod +x test-all-endpoints.sh

# Exécuter les tests
./test-all-endpoints.sh
```

### Option 3: Tests Individuels avec cURL

#### Test Authentification

```powershell
# Inscription
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{
        "username": "test@example.com",
        "password": "Test123!",
        "email": "test@example.com",
        "firstName": "Test",
        "lastName": "User",
        "role": "DOCTORANT"
    }'
$response | ConvertTo-Json

# Connexion
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{
        "username": "test@example.com",
        "password": "Test123!"
    }'
$token = $loginResponse.token
Write-Host "Token: $token"

# Utiliser le token
$headers = @{
    "Authorization" = "Bearer $token"
}
$profile = Invoke-RestMethod -Uri "http://localhost:8080/users/me" `
    -Method GET `
    -Headers $headers
$profile | ConvertTo-Json
```

---

## 📊 Interprétation des Résultats

### Codes de Status HTTP

| Code | Signification | Exemple |
|------|---------------|---------|
| 200 | OK - Succès | GET retourne des données |
| 201 | Created - Ressource créée | POST réussi |
| 204 | No Content - Succès sans retour | DELETE réussi |
| 400 | Bad Request - Données invalides | Validation échouée |
| 401 | Unauthorized - Non authentifié | Token manquant/invalide |
| 403 | Forbidden - Non autorisé | Rôle insuffisant |
| 404 | Not Found - Ressource introuvable | ID inexistant |
| 500 | Internal Server Error | Bug serveur |

### Résultats Attendus

#### ✅ Tests qui DOIVENT réussir (Status attendu)

| Test | Status | Commentaire |
|------|--------|-------------|
| Infrastructure Health Checks | 200 | Services opérationnels |
| Inscription nouveaux utilisateurs | 201 | Création réussie |
| Connexion avec bons credentials | 200 | Token JWT retourné |
| Accès à ses propres ressources | 200 | DOCTORANT → /defense/my |
| ADMIN accède endpoints admin | 200 | /defense/admin/* |

#### ✅ Tests qui DOIVENT échouer (Refus attendu)

| Test | Status | Commentaire |
|------|--------|-------------|
| Connexion mauvais password | 401 | Authentification refusée |
| Accès sans token | 401 | Redirection vers login |
| CANDIDAT → /defense/my | 403 | Rôle insuffisant |
| DOCTORANT → /defense/admin/* | 403 | Privilèges requis |
| Inscription email existant | 400 | Conflit unique constraint |

### Fichier de Résultats

Les résultats sont sauvegardés dans `test-results-YYYYMMDD-HHMMSS.json` :

```json
{
  "timestamp": "2025-12-25T14:30:15.123Z",
  "total_tests": 45,
  "passed": 43,
  "failed": 2,
  "pass_rate": 95.56,
  "tokens": {
    "admin": "eyJhbGciOiJIUzI1NiIs...",
    "doctorant": "eyJhbGciOiJIUzI1NiIs...",
    "directeur": "eyJhbGciOiJIUzI1NiIs...",
    "candidat": "eyJhbGciOiJIUzI1NiIs..."
  },
  "created_ids": {
    "campaign_id": "1",
    "registration_id": "1",
    "defense_id": "1"
  }
}
```

---

## 🐛 Dépannage

### Problème: "Connexion refusée"

**Symptôme:**
```
Invoke-WebRequest : Unable to connect to the remote server
```

**Solution:**
```powershell
# Vérifier que le service tourne
netstat -ano | findstr "8080"

# Redémarrer API Gateway
cd api-gateway
.\mvnw spring-boot:run
```

### Problème: "Services non enregistrés dans Eureka"

**Symptôme:**
```
503 Service Unavailable
```

**Solution:**
```powershell
# Vérifier Eureka Dashboard
Start-Process "http://localhost:8761"

# Attendre 30 secondes et réessayer
Start-Sleep -Seconds 30
```

### Problème: "JWT Token Expired"

**Symptôme:**
```
401 Unauthorized
Response: {"error": "Token expired"}
```

**Solution:**
```powershell
# Reconnecter pour obtenir un nouveau token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username": "test@example.com", "password": "Test123!"}'
$token = $loginResponse.token
```

### Problème: "Base de données H2 pleine"

**Symptôme:**
```
500 Internal Server Error
SQLException: Database is full
```

**Solution:**
```powershell
# Nettoyer la base H2
Remove-Item -Path ".\user-service\data\*.db" -Force
Remove-Item -Path ".\defense-service\data\*.db" -Force
Remove-Item -Path ".\registration-service\data\*.db" -Force

# Redémarrer les services
```

---

## 📈 Tests Supplémentaires

### Test de Charge avec Apache Bench

```powershell
# Installer Apache Bench (si nécessaire)
# Télécharger depuis: https://httpd.apache.org/download.cgi

# Test de charge login
ab -n 1000 -c 10 -p login.json -T application/json http://localhost:8080/auth/login

# Contenu de login.json:
# {"username": "test@example.com", "password": "Test123!"}
```

### Test de Sécurité avec OWASP ZAP

```powershell
# Installer OWASP ZAP
# https://www.zaproxy.org/download/

# Scanner automatique
zap.bat -cmd -quickurl http://localhost:8080 -quickout report.html
```

### Tests de Pénétration JWT

```powershell
# Test 1: Token sans signature
$invalidToken = "eyJhbGciOiJub25lIn0.eyJ1c2VybmFtZSI6ImFkbWluIn0."
Invoke-RestMethod -Uri "http://localhost:8080/users/me" `
    -Headers @{"Authorization" = "Bearer $invalidToken"}
# Attendu: 401 Unauthorized

# Test 2: Token modifié
$modifiedToken = $token -replace "DOCTORANT", "ADMIN"
Invoke-RestMethod -Uri "http://localhost:8080/users/me" `
    -Headers @{"Authorization" = "Bearer $modifiedToken"}
# Attendu: 401 Unauthorized (signature invalide)
```

---

## 📝 Checklist Avant Production

- [ ] ✅ Tous les tests passent (100%)
- [ ] ✅ Pas d'erreurs 500 dans les logs
- [ ] ✅ Tokens JWT expiration configurée (24h)
- [ ] ✅ RBAC validé pour tous les rôles
- [ ] ✅ Headers X-User-* correctement propagés
- [ ] ✅ Base de données PostgreSQL (pas H2)
- [ ] ✅ HTTPS activé sur API Gateway
- [ ] ✅ Rate limiting configuré
- [ ] ✅ Logs centralisés (ELK Stack)
- [ ] ✅ Monitoring (Prometheus + Grafana)
- [ ] ✅ Backups automatiques configurés
- [ ] ✅ Documentation API (Swagger) accessible
- [ ] ✅ Variables d'environnement sécurisées
- [ ] ✅ Secrets externalisés (Vault)
- [ ] ✅ Tests de charge réussis (> 1000 req/s)

---

## 🎯 Objectifs de Performance

| Métrique | Objectif | Critique |
|----------|----------|----------|
| Latence P95 | < 500ms | < 1000ms |
| Latence P99 | < 1000ms | < 2000ms |
| Throughput | > 1000 req/s | > 500 req/s |
| Taux d'erreur | < 0.1% | < 1% |
| Disponibilité | > 99.9% | > 99% |
| CPU Usage | < 70% | < 90% |
| Memory Usage | < 80% | < 95% |

---

## 📞 Support

En cas de problème avec les tests :

1. **Vérifier les logs:**
   ```powershell
   # Logs API Gateway
   Get-Content .\api-gateway\logs\spring.log -Tail 50
   
   # Logs Defense Service
   Get-Content .\defense-service\logs\spring.log -Tail 50
   ```

2. **Consulter la documentation:**
   - [SECURITY.md](./SECURITY.md) - Architecture sécurité
   - [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Guide tests détaillé
   - [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Documentation API

3. **Vérifier Eureka Dashboard:**
   - URL: http://localhost:8761
   - Tous les services doivent être "UP"

4. **Nettoyer et redémarrer:**
   ```powershell
   # Arrêter tous les services
   docker-compose down
   
   # Nettoyer les builds
   .\mvnw clean
   
   # Reconstruire et redémarrer
   .\mvnw install
   docker-compose up -d
   ```

---

**Bonne chance avec vos tests ! 🚀**
