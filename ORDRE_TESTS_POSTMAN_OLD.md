# 🧪 Guide d'Exécution Séquentiel - Tests Postman

## 📋 Organisation par Workflows Métier

**Collection organisée par cas d'usage** plutôt que par services techniques.

**29 tests** répartis en **7 workflows** :
1. **Setup - Authentification** (5 tests)
2. **Workflow Soutenance de Thèse** (9 tests)  
3. **Workflow Candidature Doctorat** (5 tests)
4. **Gestion des Utilisateurs** (2 tests)
5. **Gestion des Notifications** (3 tests)
6. **Tests Sécurité RBAC** (3 tests)
7. **Tests Authentification** (2 tests)

---

## ✅ ÉTAPE 1 : Importer les fichiers dans Postman

1. Ouvrir Postman
2. Cliquer sur **Import** (en haut à gauche)
3. Sélectionner les 2 fichiers :
   - `Postman-Collection-Complete.json`
   - `Postman-Environment-Local.json`
4. Sélectionner l'environnement **"Doctorat App - Local"** (dropdown en haut à droite)

---

### ✅ ÉTAPE 2 : Vérifier que les services sont actifs

Avant de commencer, vérifier que tous les services tournent :

```powershell
# PowerShell - Vérifier les ports
8761,8080,8081,8082,8083,8084 | ForEach-Object { 
    $result = Test-NetConnection -ComputerName localhost -Port $_ -WarningAction SilentlyContinue
    if ($result.TcpTestSucceeded) {
        Write-Host "✅ Port $_ : ACTIF" -ForegroundColor Green
    } else {
        Write-Host "❌ Port $_ : INACTIF" -ForegroundColor Red
    }
}
```

**Services attendus** :
- ✅ 8761 : Discovery Server (Eureka)
- ✅ 8080 : API Gateway
- ✅ 8081 : User Service
- ✅ 8082 : Registration Service
- ✅ 8083 : Defense Service
- ✅ 8084 : Notification Service

---

### ✅ ÉTAPE 3 : Exécution Séquentielle des Tests

## 🔐 WORKFLOW 1 : Setup - Authentification

**Objectif** : Créer les 4 types d'utilisateurs et obtenir leurs tokens JWT

### Test 1 : Register ADMIN
```
Dossier : 1. Setup - Authentification
Requête : Register ADMIN
Méthode : POST
URL : http://localhost:8080/auth/register
```

**Body** :
```json
{
  "username": "admin_test",
  "password": "admin123",
  "email": "admin@doctorat.ma",
  "role": "ADMIN"
}
```

**Résultat attendu** : 
- Status : `201 Created`
- Token sauvegardé automatiquement dans `admin_token`
- User ID sauvegardé dans `admin_id`

**Vérification** :
- Aller dans Environments → Variables
- Voir `admin_token` rempli
- Voir `admin_id` (exemple : 1)

---

### Test 2 : Register DIRECTEUR_THESE
```
Dossier : 1. Setup - Authentification
Requête : Register DIRECTEUR_THESE
```

**Body** :
```json
{
  "username": "directeur_test",
  "password": "directeur123",
  "email": "directeur@doctorat.ma",
  "role": "DIRECTEUR_THESE"
}
```

**Résultat attendu** : 
- Status : `201 Created`
- `directeur_token` et `directeur_id` sauvegardés

---

### Test 3 : Register DOCTORANT
```
Dossier : 1. Setup - Authentification
Requête : Register DOCTORANT
```

**Body** :
```json
{
  "username": "doctorant_test",
  "password": "doctorant123",
  "email": "doctorant@doctorat.ma",
  "role": "DOCTORANT"
}
```

**Résultat attendu** : 
- Status : `201 Created`
- `doctorant_token` et `doctorant_id` sauvegardés

---

### Test 4 : Register CANDIDAT
```
Dossier : 1. Setup - Authentification
Requête : Register CANDIDAT
```

**Body** :
```json
{
  "username": "candidat_test",
  "password": "candidat123",
  "email": "candidat@doctorat.ma",
  "role": "CANDIDAT"
}
```

**Résultat attendu** : 
- Status : `201 Created`
- `candidat_token` et `candidat_id` sauvegardés

---

### Test 5 : Login (Optionnel)
```
Dossier : 1. Authentication
Requête : Login
```

**Body** :
```json
{
  "username": "admin_test",
  "password": "admin123"
}
```

**Résultat attendu** : `200 OK` avec token

---

## 👥 WORKFLOW 2 : Gestion des Utilisateurs

**Objectif** : Consulter les utilisateurs créés

### Test 6 : Get All Users (ADMIN)
```
Dossier : 4. Gestion des Utilisateurs
Requête : Get All Users (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Résultat attendu** : 
- Status : `200 OK`
- Liste de 4 utilisateurs (ADMIN, DIRECTEUR, DOCTORANT, CANDIDAT)

---

### Test 7 : Get User by ID
```
Dossier : 2. User Management
Requête : Get User by ID
URL : http://localhost:8080/api/users/{{admin_id}}
Authorization : Bearer {{admin_token}}
```

**Résultat attendu** : `200 OK` avec détails de l'ADMIN

---

## 🛡️ PHASE 3 : Workflow de Défense Complet

### Test 8 : Create Defense Request (DOCTORANT)
```
Dossier : 3. Defense Service
Requête : Create Defense Request (DOCTORANT)
Authorization : Bearer {{doctorant_token}}
```

**Body** :
```json
{
  "thesisTitle": "Architecture Microservices pour la Gestion des Soutenances",
  "thesisAbstract": "Cette thèse étudie l'application des patterns microservices...",
  "researchField": "Génie Logiciel",
  "laboratory": "Laboratoire d'Informatique",
  "directorId": {{directeur_id}},
  "publicationsCount": 5,
  "conferencesCount": 3,
  "trainingHours": 300,
  "proposedDate": "2025-09-15T14:00:00",
  "academicYear": 2025
}
```

**Résultat attendu** :
- Status : `201 Created`
- `defense_id` sauvegardé automatiquement
- Status initial : `PENDING_VERIFICATION`

**⚠️ IMPORTANT** : Noter le `defense_id` retourné (exemple : 1)

---

### Test 9 : Get My Defenses (DOCTORANT)
```
Dossier : 3. Defense Service
Requête : Get My Defenses (DOCTORANT)
Authorization : Bearer {{doctorant_token}}
```

**Résultat attendu** :
- Status : `200 OK`
- Tableau avec 1 défense créée
- Status : `PENDING_VERIFICATION`

---

### Test 10 : Get All Defenses (ADMIN)
```
Dossier : 3. Defense Service
Requête : Get All Defenses (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Résultat attendu** :
- Status : `200 OK`
- Liste de toutes les défenses

---

### Test 11 : Get Defense by ID
```
Dossier : 3. Defense Service
Requête : Get Defense by ID
URL : http://localhost:8080/api/defenses/{{defense_id}}
Authorization : Bearer {{admin_token}}
```

**Résultat attendu** : `200 OK` avec détails complets de la défense

---

### Test 12 : Validate Prerequisites (ADMIN)
```
Dossier : 3. Defense Service
Requête : Validate Prerequisites (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "approved": true,
  "comments": "Tous les prérequis sont remplis : 5 publications, 3 conférences, 300h"
}
```

**Résultat attendu** :
- Status : `200 OK`
- Status devient : `PREREQUISITES_VALIDATED`

---

### Test 13 : Authorize Defense (ADMIN)
```
Dossier : 3. Defense Service
Requête : Authorize Defense (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "date": "2025-09-15T14:00:00",
  "location": "Amphithéâtre A - Bâtiment Principal",
  "comments": "Soutenance autorisée"
}
```

**Résultat attendu** :
- Status : `200 OK`
- Status devient : `AUTHORIZED`
- Date et lieu confirmés

---

### Test 14 : Propose Jury (DIRECTEUR_THESE)
```
Dossier : 3. Defense Service
Requête : Propose Jury (DIRECTEUR_THESE)
Authorization : Bearer {{directeur_token}}
```

**Body** :
```json
{
  "presidentId": {{admin_id}},
  "rapporteurIds": [{{directeur_id}}],
  "examinateurIds": [{{admin_id}}]
}
```

**Résultat attendu** :
- Status : `200 OK`
- Status devient : `JURY_PROPOSED`

---

### Test 15 : Validate Jury (ADMIN)
```
Dossier : 3. Defense Service
Requête : Validate Jury (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "approved": true,
  "comments": "Composition du jury validée"
}
```

**Résultat attendu** :
- Status : `200 OK`
- Status devient : `JURY_VALIDATED`

---

### Test 16 : Record Final Result (ADMIN)
```
Dossier : 3. Defense Service
Requête : Record Final Result (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "result": "PASSED",
  "mention": "TRES_HONORABLE",
  "comments": "Excellente soutenance"
}
```

**Résultat attendu** :
- Status : `200 OK`
- Status devient : `COMPLETED`
- Workflow de défense terminé ✅

---

## 📝 PHASE 4 : Service d'Inscription

### Test 17 : Create Campaign (ADMIN)
```
Dossier : 4. Registration Service
Requête : Create Campaign (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "name": "Campagne de Recrutement 2025",
  "startDate": "2025-01-01",
  "endDate": "2025-03-31",
  "maxCandidates": 100,
  "active": true
}
```

**Résultat attendu** :
- Status : `201 Created`
- `campaign_id` sauvegardé

---

### Test 18 : Get All Campaigns
```
Dossier : 4. Registration Service
Requête : Get All Campaigns
Authorization : Bearer {{candidat_token}}
```

**Résultat attendu** : `200 OK` avec liste des campagnes

---

### Test 19 : Submit Registration (CANDIDAT)
```
Dossier : 4. Registration Service
Requête : Submit Registration (CANDIDAT)
Authorization : Bearer {{candidat_token}}
```

**Body** :
```json
{
  "campaignId": {{campaign_id}},
  "researchField": "Intelligence Artificielle",
  "proposedDirectorId": {{directeur_id}},
  "motivationLetter": "Je souhaite poursuivre mes études doctorales en IA...",
  "cvUrl": "https://example.com/cv.pdf"
}
```

**Résultat attendu** :
- Status : `201 Created`
- `application_id` sauvegardé

---

### Test 20 : Review Application (DIRECTEUR_THESE)
```
Dossier : 4. Registration Service
Requête : Review Application (DIRECTEUR_THESE)
Authorization : Bearer {{directeur_token}}
```

**Body** :
```json
{
  "approved": true,
  "comments": "Bon profil, expérience pertinente"
}
```

**Résultat attendu** : `200 OK`

---

### Test 21 : Approve Application (ADMIN)
```
Dossier : 4. Registration Service
Requête : Approve Application (ADMIN)
Authorization : Bearer {{admin_token}}
```

**Body** :
```json
{
  "approved": true,
  "comments": "Candidature approuvée"
}
```

**Résultat attendu** : `200 OK`

---

## 🔔 PHASE 5 : Service de Notifications

### Test 22 : Get My Notifications
```
Dossier : 5. Notification Service
Requête : Get My Notifications
Authorization : Bearer {{doctorant_token}}
```

**Résultat attendu** : `200 OK` avec liste de notifications

---

### Test 23 : Get Unread Count
```
Dossier : 5. Notification Service
Requête : Get Unread Count
Authorization : Bearer {{doctorant_token}}
```

**Résultat attendu** : `200 OK` avec nombre

---

### Test 24 : Mark Notification as Read
```
Dossier : 5. Notification Service
Requête : Mark Notification as Read
Authorization : Bearer {{doctorant_token}}
```

**Résultat attendu** : `200 OK`

---

## 🚫 PHASE 6 : Tests RBAC (Doivent échouer)

### Test 25 : CANDIDAT Create Defense (403 Expected)
```
Dossier : 6. RBAC Tests
Requête : CANDIDAT Create Defense
Authorization : Bearer {{candidat_token}}
```

**Résultat attendu** : `403 Forbidden` ❌
**Raison** : Un CANDIDAT ne peut pas créer de défense

---

### Test 26 : DOCTORANT Validate Prerequisites (403 Expected)
```
Dossier : 6. RBAC Tests
Requête : DOCTORANT Validate Prerequisites
Authorization : Bearer {{doctorant_token}}
```

**Résultat attendu** : `403 Forbidden` ❌
**Raison** : Seul ADMIN peut valider les prérequis

---

### Test 27 : CANDIDAT Get All Users (403 Expected)
```
Dossier : 6. RBAC Tests
Requête : CANDIDAT Get All Users
Authorization : Bearer {{candidat_token}}
```

**Résultat attendu** : `403 Forbidden` ❌
**Raison** : Seul ADMIN peut lister tous les utilisateurs

---

## ❌ PHASE 7 : Tests d'Erreurs

### Test 28 : No Token (401 Expected)
```
Dossier : 7. Error Tests
Requête : No Token
Authorization : AUCUNE
```

**Résultat attendu** : `401 Unauthorized` ❌

---

### Test 29 : Invalid Token (401 Expected)
```
Dossier : 7. Error Tests
Requête : Invalid Token
Authorization : Bearer invalid_token_here
```

**Résultat attendu** : `401 Unauthorized` ❌

---

## 📊 Résumé des Résultats Attendus

| Phase | Tests | Succès | Échecs | Total |
|-------|-------|--------|--------|-------|
| 1. Authentication | 1-5 | 5 | 0 | 5 |
| 2. User Management | 6-7 | 2 | 0 | 2 |
| 3. Defense Service | 8-16 | 9 | 0 | 9 |
| 4. Registration Service | 17-21 | 5 | 0 | 5 |
| 5. Notification Service | 22-24 | 3 | 0 | 3 |
| 6. RBAC Tests | 25-27 | 0 | 3 | 3 |
| 7. Error Tests | 28-29 | 0 | 2 | 2 |
| **TOTAL** | | **24** | **5** | **29** |

**Success Rate Expected** : 24/29 = 82.8% (les 5 échecs sont intentionnels)

---

## 🎯 Checklist de Validation

Après tous les tests, vérifier :

- [ ] ✅ 4 utilisateurs créés (ADMIN, DIRECTEUR, DOCTORANT, CANDIDAT)
- [ ] ✅ 1 défense créée avec workflow complet
- [ ] ✅ Status de la défense : COMPLETED
- [ ] ✅ 1 campagne créée
- [ ] ✅ 1 candidature soumise et approuvée
- [ ] ✅ Notifications générées
- [ ] ✅ RBAC fonctionne (403 pour permissions insuffisantes)
- [ ] ✅ Authentication fonctionne (401 sans token)

---

## 🐛 Dépannage

### Problème : 503 Service Unavailable
**Cause** : Service pas encore enregistré dans Eureka  
**Solution** : Attendre 30-60 secondes après le démarrage

### Problème : 401 Unauthorized
**Cause** : Token invalide ou expiré  
**Solution** : Refaire Register/Login pour obtenir un nouveau token

### Problème : 403 Forbidden
**Cause** : Permissions insuffisantes  
**Solution** : Utiliser le bon token (admin_token pour les opérations ADMIN)

### Problème : Variables non sauvegardées
**Cause** : Script de test pas exécuté  
**Solution** : Vérifier que les scripts sont activés dans Postman Settings

---

## ✅ Commandes PowerShell Utiles

```powershell
# Redémarrer tous les services
.\restart-all.ps1

# Vérifier les logs d'un service
Get-Content defense-service\defense-service.log -Tail 50 -Wait

# Tester une route directement
curl http://localhost:8080/auth/register -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"test","password":"test123","email":"test@test.com","role":"ADMIN"}'
```

---

**🎉 Bon test ! Suivez l'ordre séquentiel pour valider tous les endpoints.**
