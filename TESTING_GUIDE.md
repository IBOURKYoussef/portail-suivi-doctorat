# Guide de Test - Sécurité Centralisée

## 📋 Prérequis

- Tous les services doivent être démarrés
- Curl ou un outil similaire (Postman, HTTPie)
- jq (optionnel, pour parser le JSON)

## 🚀 Tests Complets

### 1. Test de l'Infrastructure

#### Vérifier Discovery Server
```bash
curl http://localhost:8761/
# Devrait afficher la page Eureka avec les services enregistrés
```

#### Vérifier API Gateway
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

#### Vérifier l'enregistrement des services
```bash
curl http://localhost:8761/eureka/apps
# Devrait lister: USER-SERVICE, DEFENSE-SERVICE, REGISTRATION-SERVICE, NOTIFICATION-SERVICE
```

---

### 2. Tests d'Authentification

#### Test 1: Inscription - Doctorant
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant1@example.com",
    "password": "Doctorant123!",
    "email": "doctorant1@example.com",
    "firstName": "Ahmed",
    "lastName": "BENNANI",
    "phone": "+212600000001",
    "role": "DOCTORANT",
    "studentId": "CNE12345678"
  }'

# Expected: 201 Created avec les détails de l'utilisateur
```

#### Test 2: Inscription - Directeur de Thèse
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "directeur1@example.com",
    "password": "Directeur123!",
    "email": "directeur1@example.com",
    "firstName": "Mohammed",
    "lastName": "ALAMI",
    "phone": "+212600000002",
    "role": "DIRECTEUR_THESE",
    "laboratoire": "Laboratoire IA",
    "grade": "Professeur"
  }'

# Expected: 201 Created
```

#### Test 3: Inscription - Administrateur
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "Admin123!",
    "email": "admin@example.com",
    "firstName": "Fatima",
    "lastName": "ZAHRA",
    "phone": "+212600000003",
    "role": "ADMIN"
  }'

# Expected: 201 Created
```

#### Test 4: Connexion - Doctorant
```bash
# Sans jq
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant1@example.com",
    "password": "Doctorant123!"
  }'

# Avec jq (pour extraire le token)
TOKEN_DOCTORANT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant1@example.com",
    "password": "Doctorant123!"
  }' | jq -r '.token')

echo "Token Doctorant: $TOKEN_DOCTORANT"

# Expected: Token JWT
```

#### Test 5: Connexion - Directeur
```bash
TOKEN_DIRECTEUR=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "directeur1@example.com",
    "password": "Directeur123!"
  }' | jq -r '.token')

echo "Token Directeur: $TOKEN_DIRECTEUR"
```

#### Test 6: Connexion - Admin
```bash
TOKEN_ADMIN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "Admin123!"
  }' | jq -r '.token')

echo "Token Admin: $TOKEN_ADMIN"
```

#### Test 7: Connexion avec mauvais credentials
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant1@example.com",
    "password": "WrongPassword"
  }'

# Expected: 401 Unauthorized
```

---

### 3. Tests de Validation JWT

#### Test 8: Accès sans token
```bash
curl -X GET http://localhost:8080/defense/my

# Expected: 401 Unauthorized
```

#### Test 9: Accès avec token invalide
```bash
curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer invalid.token.here"

# Expected: 401 Unauthorized
```

#### Test 10: Accès avec token valide
```bash
curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN_DOCTORANT"

# Expected: 200 OK (liste des défenses du doctorant)
```

---

### 4. Tests de Contrôle d'Accès Basé sur les Rôles

#### Test 11: DOCTORANT crée une défense ✓
```bash
curl -X POST http://localhost:8080/defense/create \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Intelligence Artificielle et Santé",
    "resume": "Cette thèse explore l utilisation du deep learning pour le diagnostic médical...",
    "directeurTheseId": 2,
    "dateProposee": "2026-06-15T14:00:00",
    "lieu": "Amphithéâtre A, FST Fès",
    "specialite": "Informatique",
    "laboratoire": "Lab IA"
  }'

# Expected: 201 Created
# Sauvegarder l'ID de la défense
DEFENSE_ID=1
```

#### Test 12: DOCTORANT consulte ses défenses ✓
```bash
curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN_DOCTORANT"

# Expected: 200 OK avec liste des défenses
```

#### Test 13: DOCTORANT essaie de valider une défense ✗
```bash
curl -X POST "http://localhost:8080/defense/$DEFENSE_ID/validate-prerequisites?approved=true" \
  -H "Authorization: Bearer $TOKEN_DOCTORANT"

# Expected: 403 Forbidden (seul ADMIN peut valider)
```

#### Test 14: ADMIN valide les prérequis ✓
```bash
curl -X POST "http://localhost:8080/defense/$DEFENSE_ID/validate-prerequisites?approved=true&comment=Tous les prérequis sont remplis" \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# Expected: 200 OK
```

#### Test 15: DIRECTEUR compose le jury ✓
```bash
curl -X POST http://localhost:8080/defense/$DEFENSE_ID/jury \
  -H "Authorization: Bearer $TOKEN_DIRECTEUR" \
  -H "Content-Type: application/json" \
  -d '{
    "president": {
      "nom": "Prof. Mohammed BENNANI",
      "grade": "Professeur",
      "etablissement": "FST Fès",
      "email": "bennani@fst.ac.ma"
    },
    "rapporteurs": [
      {
        "nom": "Prof. Fatima ZAHRA",
        "grade": "Professeur",
        "etablissement": "ENSIAS Rabat",
        "email": "zahra@ensias.ac.ma"
      }
    ]
  }'

# Expected: 200 OK
```

#### Test 16: DOCTORANT essaie de composer le jury ✗
```bash
curl -X POST http://localhost:8080/defense/$DEFENSE_ID/jury \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -H "Content-Type: application/json" \
  -d '{}'

# Expected: 403 Forbidden
```

#### Test 17: ADMIN autorise la soutenance ✓
```bash
curl -X POST http://localhost:8080/defense/$DEFENSE_ID/authorize \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateFinale": "2026-06-15T14:00:00",
    "lieu": "Amphithéâtre A",
    "autorisationNumber": "AUT-2026-001"
  }'

# Expected: 200 OK
```

---

### 5. Tests des Inscriptions

#### Test 18: ADMIN crée une campagne ✓
```bash
curl -X POST http://localhost:8080/registration/campaigns \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Campagne Doctorat 2025-2026",
    "description": "Campagne d inscription pour l année universitaire 2025-2026",
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-03-31T23:59:59",
    "maxCandidates": 100,
    "status": "OPEN"
  }'

# Expected: 201 Created
# Sauvegarder l'ID de la campagne
CAMPAIGN_ID=1
```

#### Test 19: CANDIDAT soumet une candidature ✓
```bash
# D'abord créer un compte CANDIDAT
TOKEN_CANDIDAT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "candidat@example.com",
    "password": "Candidat123!"
  }' | jq -r '.token')

curl -X POST http://localhost:8080/registration/apply \
  -H "Authorization: Bearer $TOKEN_CANDIDAT" \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": '$CAMPAIGN_ID',
    "sujetThese": "Intelligence Artificielle et Santé",
    "domaineRecherche": "IA",
    "directeurThese": "Dr. Ahmed ALAMI",
    "laboratoire": "Lab IA",
    "motivations": "Je suis passionné par l IA..."
  }'

# Expected: 201 Created
```

#### Test 20: DOCTORANT essaie de créer une campagne ✗
```bash
curl -X POST http://localhost:8080/registration/campaigns \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Campagne Test",
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-03-31T23:59:59"
  }'

# Expected: 403 Forbidden
```

---

### 6. Tests des Notifications

#### Test 21: ADMIN envoie une notification ✓
```bash
curl -X POST http://localhost:8080/notification/send \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientIds": [1, 2],
    "type": "EMAIL",
    "subject": "Rappel important",
    "message": "Ceci est un message de test",
    "priority": "HIGH"
  }'

# Expected: 201 Created
```

#### Test 22: DOCTORANT essaie d'envoyer une notification ✗
```bash
curl -X POST http://localhost:8080/notification/send \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientIds": [1],
    "type": "EMAIL",
    "subject": "Test",
    "message": "Test"
  }'

# Expected: 403 Forbidden
```

#### Test 23: Utilisateur consulte ses notifications ✓
```bash
curl -X GET http://localhost:8080/notification/my \
  -H "Authorization: Bearer $TOKEN_DOCTORANT"

# Expected: 200 OK avec liste des notifications
```

---

### 7. Tests de Propagation des Headers

#### Test 24: Vérifier que les headers X-User-* sont propagés
```bash
# Dans le microservice Defense, ajouter temporairement un log:
# log.info("X-User-Id: {}", request.getHeader("X-User-Id"));
# log.info("X-User-Role: {}", request.getHeader("X-User-Role"));

curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -v

# Vérifier dans les logs du Defense Service:
# X-User-Id: 1
# X-User-Role: DOCTORANT
```

---

### 8. Tests de Performance

#### Test 25: Tester la latence
```bash
# Test simple
time curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN_DOCTORANT"

# Test avec Apache Bench (100 requêtes, 10 concurrentes)
ab -n 100 -c 10 -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  http://localhost:8080/defense/my

# Expected: Temps de réponse < 200ms
```

---

### 9. Tests de Sécurité

#### Test 26: Token expiré
```bash
# Créer un token avec expiration très courte (dans UserService)
# Attendre qu'il expire
# Essayer d'utiliser le token expiré

curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer <expired_token>"

# Expected: 401 Unauthorized
```

#### Test 27: Token modifié
```bash
# Modifier manuellement le payload du token
TOKEN_MODIFIED="${TOKEN_DOCTORANT}xxx"

curl -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN_MODIFIED"

# Expected: 401 Unauthorized (signature invalide)
```

#### Test 28: SQL Injection (devrait être bloqué)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com OR 1=1--",
    "password": "anything"
  }'

# Expected: 400 Bad Request ou 401 Unauthorized (pas de SQL injection)
```

---

### 10. Tests de CORS

#### Test 29: Preflight CORS
```bash
curl -X OPTIONS http://localhost:8080/defense/my \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v

# Expected: Headers CORS dans la réponse:
# Access-Control-Allow-Origin: http://localhost:4200
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

#### Test 30: Requête CORS non autorisée
```bash
curl -X GET http://localhost:8080/defense/my \
  -H "Origin: http://malicious-site.com" \
  -H "Authorization: Bearer $TOKEN_DOCTORANT" \
  -v

# Expected: Pas de header Access-Control-Allow-Origin
```

---

## 📊 Script de Test Complet

```bash
#!/bin/bash

# Couleurs pour le terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher le résultat
check_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ PASSED${NC}: $1"
    else
        echo -e "${RED}✗ FAILED${NC}: $1"
    fi
}

echo "============================================"
echo "  Tests de Sécurité - Application Doctorat"
echo "============================================"
echo ""

# Test 1: Infrastructure
echo -e "${YELLOW}Test 1: Vérification de l'infrastructure${NC}"
curl -s http://localhost:8080/actuator/health | grep -q "UP"
check_result "API Gateway UP"

# Test 2: Inscription Doctorant
echo -e "\n${YELLOW}Test 2: Inscription Doctorant${NC}"
RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.doctorant@example.com",
    "password": "Test123!",
    "email": "test.doctorant@example.com",
    "firstName": "Test",
    "lastName": "Doctorant",
    "role": "DOCTORANT"
  }')
[[ "$RESPONSE" == *"201"* ]]
check_result "Inscription Doctorant"

# Test 3: Connexion
echo -e "\n${YELLOW}Test 3: Connexion${NC}"
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test.doctorant@example.com",
    "password": "Test123!"
  }' | jq -r '.token')

[[ -n "$TOKEN" && "$TOKEN" != "null" ]]
check_result "Connexion et récupération du token"

# Test 4: Accès avec token valide
echo -e "\n${YELLOW}Test 4: Accès avec token valide${NC}"
curl -s -w "%{http_code}" -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer $TOKEN" | grep -q "200"
check_result "Accès avec token valide"

# Test 5: Accès sans token
echo -e "\n${YELLOW}Test 5: Accès sans token${NC}"
curl -s -w "%{http_code}" -X GET http://localhost:8080/defense/my | grep -q "401"
check_result "Rejet de requête sans token"

# Test 6: Accès avec token invalide
echo -e "\n${YELLOW}Test 6: Accès avec token invalide${NC}"
curl -s -w "%{http_code}" -X GET http://localhost:8080/defense/my \
  -H "Authorization: Bearer invalid.token" | grep -q "401"
check_result "Rejet de token invalide"

echo ""
echo "============================================"
echo "  Tests terminés"
echo "============================================"
```

Sauvegarder ce script dans `test-security.sh` et exécuter :
```bash
chmod +x test-security.sh
./test-security.sh
```

---

## 📝 Résultats Attendus

### ✅ Tests Réussis
- Tous les tests d'authentification fonctionnent
- Les rôles sont correctement respectés
- Les tokens JWT sont validés
- Les headers X-User-* sont propagés
- CORS fonctionne pour les origines autorisées

### ❌ Cas d'Échec Attendus
- Requêtes sans token → 401
- Requêtes avec token invalide → 401
- Requêtes avec mauvais rôle → 403
- Requêtes CORS non autorisées → Bloquées

---

## 🔍 Monitoring

### Vérifier les Logs

```bash
# API Gateway
tail -f api-gateway/logs/application.log | grep -i "jwt\|auth\|security"

# Defense Service
tail -f defense-service/logs/application.log | grep -i "X-User"

# User Service
tail -f user-service/logs/application.log | grep -i "token\|login"
```

### Métriques Actuator

```bash
# Voir les métriques de sécurité
curl http://localhost:8080/actuator/metrics/http.server.requests \
  | jq '.measurements[] | select(.statistic=="COUNT")'
```

---

**Date de création** : 25 décembre 2025  
**Version** : 1.0.0
