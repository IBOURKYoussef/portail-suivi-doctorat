# 🧪 GUIDE DE TEST - APPLICATION DOCTORAT

## ✅ ÉTAT DES SERVICES

Tous les services sont **ACTIFS** et **enregistrés dans Eureka** :

| Service | Port | Status | Eureka |
|---------|------|--------|--------|
| Discovery Server | 8761 | ✅ | - |
| API Gateway | 8080 | ✅ | ✅ |
| User Service | 8081 | ✅ | ✅ |
| Registration Service | 8082 | ✅ | ✅ |
| Defense Service | 8083 | ✅ | ✅ |
| Notification Service | 8084 | ✅ | ✅ |

## 🔑 ÉTAPE 1 : S'AUTHENTIFIER

### Créer un utilisateur (si nécessaire)
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "test_doctorant",
  "password": "password123",
  "email": "doctorant@test.com",
  "role": "DOCTORANT"
}
```

### Se connecter
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin_test",
  "password": "password123"
}
```

**Réponse attendue** : `200 OK`
```json
{
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "admin_test",
    "role": "ADMIN"
  }
}
```

📝 **IMPORTANT** : Copiez le `accessToken` et utilisez-le dans toutes les requêtes suivantes.

## 🧪 ÉTAPE 2 : TESTER LES ENDPOINTS

### A. User Service (ADMIN uniquement)

#### Lister tous les utilisateurs
```http
GET http://localhost:8080/api/users
Authorization: Bearer {votre_token}
```

**Rôles autorisés** : `ADMIN`  
**Réponse attendue** : `200 OK` avec liste d'utilisateurs  
**Si non-ADMIN** : `403 Forbidden`

---

### B. Defense Service

#### 1. Créer une demande de soutenance (DOCTORANT)
```http
POST http://localhost:8080/api/defenses
Authorization: Bearer {token_doctorant}
Content-Type: application/json

{
  "titre": "Intelligence Artificielle et Apprentissage Automatique",
  "resumeFr": "Étude sur les réseaux de neurones",
  "resumeEn": "Study on neural networks",
  "directeurTheseId": 2,
  "codirecteurIds": [3],
  "thesisStartDate": "2021-09-01",
  "nbPublications": 3,
  "nbConferences": 2,
  "nbTrainingHours": 250
}
```

**Rôles autorisés** : `DOCTORANT`  
**Réponse attendue** : `201 Created`

#### 2. Consulter mes soutenances
```http
GET http://localhost:8080/api/defenses/my
Authorization: Bearer {token_doctorant}
```

**Rôles autorisés** : `DOCTORANT`  
**Réponse attendue** : `200 OK` avec page de soutenances

#### 3. Valider les prérequis (ADMIN)
```http
POST http://localhost:8080/api/defenses/1/validate-prerequisites?approved=true&comment=Prérequis OK
Authorization: Bearer {token_admin}
```

**Rôles autorisés** : `ADMIN`  
**Réponse attendue** : `200 OK`

#### 4. Autoriser la soutenance (ADMIN)
```http
POST http://localhost:8080/api/defenses/1/authorize?authorized=true&defenseDate=2025-03-15T14:00:00&location=Salle A&room=101&comment=Autorisé
Authorization: Bearer {token_admin}
```

**Rôles autorisés** : `ADMIN`  
**Réponse attendue** : `200 OK`

---

### C. Registration Service

#### 1. Créer une campagne (ADMIN)
```http
POST http://localhost:8080/api/registration/campaigns
Authorization: Bearer {token_admin}
Content-Type: application/json

{
  "name": "Campagne Doctorat 2025",
  "description": "Inscription pour la session 2025",
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-06-30T23:59:59",
  "maxCandidates": 50,
  "type": "DOCTORAT"
}
```

**Rôles autorisés** : `ADMIN`  
**Réponse attendue** : `201 Created`  
**Si non-ADMIN** : `403 Forbidden`

#### 2. Lister les campagnes
```http
GET http://localhost:8080/api/registration/campaigns
Authorization: Bearer {votre_token}
```

**Rôles autorisés** : `ADMIN`, `CANDIDAT`, `DOCTORANT`  
**Réponse attendue** : `200 OK`

---

### D. Notification Service

#### 1. Envoyer une notification (ADMIN/ADMINISTRATIF)
```http
POST http://localhost:8080/api/notifications
Authorization: Bearer {token_admin}
Content-Type: application/json

{
  "userId": 1,
  "title": "Nouvelle notification",
  "message": "Votre demande a été approuvée",
  "type": "INFO",
  "channel": "EMAIL"
}
```

**Rôles autorisés** : `ADMIN`, `ADMINISTRATIF`  
**Réponse attendue** : `200 OK`  
**Si autre rôle** : `403 Forbidden`

#### 2. Consulter mes notifications
```http
GET http://localhost:8080/api/notifications/user/1
Authorization: Bearer {votre_token}
```

**Rôles autorisés** : Tous les rôles authentifiés  
**Réponse attendue** : `200 OK` avec page de notifications

---

## ❌ CODES D'ERREUR ATTENDUS

| Code | Signification | Cause |
|------|---------------|-------|
| **401 Unauthorized** | Token invalide/absent | Pas de header Authorization ou token expiré |
| **403 Forbidden** | Rôle insuffisant | Vous n'avez pas le rôle requis pour cette action |
| **404 Not Found** | Route inexistante | URL incorrecte ou service non routé |
| **500 Internal Server Error** | Erreur serveur | Problème dans le code backend |
| **503 Service Unavailable** | Service indisponible | Service non enregistré dans Eureka |

## 🎯 SCENARIOS DE TEST PAR RÔLE

### ADMIN (Super Utilisateur)
✅ Peut tout faire :
- Gérer les utilisateurs (`/api/users`)
- Valider les soutenances (`/api/defenses/*/validate-prerequisites`)
- Autoriser les soutenances (`/api/defenses/*/authorize`)
- Créer des campagnes (`/api/registration/campaigns`)
- Envoyer des notifications (`/api/notifications`)

### DIRECTEUR_THESE
✅ Peut :
- Consulter les soutenances en attente (`/api/defenses/director/pending`)
- Enregistrer les résultats (`/api/defenses/*/result`)
- Proposer un jury (`/api/defenses/*/jury`)
- Soumettre un rapport de rapporteur

❌ Ne peut pas :
- Gérer les utilisateurs
- Créer des campagnes

### DOCTORANT
✅ Peut :
- Créer une demande de soutenance (`/api/defenses`)
- Consulter ses soutenances (`/api/defenses/my`)
- Consulter les campagnes
- Recevoir des notifications

❌ Ne peut pas :
- Valider ou autoriser des soutenances
- Créer des campagnes
- Envoyer des notifications

### CANDIDAT
✅ Peut :
- Consulter les campagnes (`/api/registration/campaigns`)
- Candidater (`/api/registration/applications`)
- Recevoir des notifications

❌ Ne peut pas :
- Accéder aux soutenances
- Créer des campagnes

### ADMINISTRATIF
✅ Peut :
- Envoyer des notifications (`/api/notifications`)
- Consulter les notifications

❌ Ne peut pas :
- Gérer les utilisateurs
- Gérer les soutenances
- Créer des campagnes

## 🔧 DÉPANNAGE

### Problème : 401 Unauthorized
**Solution** : Vérifiez que vous avez bien le header `Authorization: Bearer {token}`

### Problème : 403 Forbidden
**Solution** : Votre rôle n'a pas accès. Connectez-vous avec un utilisateur ayant le bon rôle.

### Problème : 503 Service Unavailable
**Solution** : Le service n'est pas enregistré dans Eureka. Redémarrez le service.

### Problème : 404 Not Found
**Solution** : Vérifiez l'URL. Les routes correctes sont :
- `/api/users/**`
- `/api/defenses/**` (pas `/api/defense/requests`)
- `/api/registration/**`
- `/api/notifications/**`

## 📊 VÉRIFICATIONS

### Vérifier Eureka
```
http://localhost:8761
```
Tous les services doivent apparaître dans la liste.

### Vérifier les logs
Consultez les fenêtres PowerShell de chaque service pour voir les logs en temps réel.

### Vérifier la base H2 (Defense Service)
```
http://localhost:8083/h2-console
JDBC URL: jdbc:h2:mem:defensedb
Username: sa
Password: (vide)
```

---

**Date** : 27 décembre 2025  
**Version** : 1.0  
**Status** : ✅ Tous les services opérationnels
