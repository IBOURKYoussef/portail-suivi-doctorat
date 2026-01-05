# 📋 RÉCAPITULATIF - FICHIERS POSTMAN

## 📦 3 FICHIERS À UTILISER

```
📁 microservices-doctorat-app/
│
├── 📄 Postman-Collection-Complete.json    ⭐ FICHIER PRINCIPAL
│   └── 50+ requêtes organisées par service
│
├── 📄 Postman-Environment-Local.json      🔧 CONFIGURATION
│   └── Variables (base_url, tokens...)
│
└── 📄 GUIDE_RAPIDE_POSTMAN.md             📖 MODE D'EMPLOI
    └── Instructions pas à pas (5 min)
```

---

## ⚡ UTILISATION EN 3 CLICS

### 1. IMPORTER (30 secondes)
```
Postman → Import → Glisser les 2 fichiers .json
```

### 2. ACTIVER L'ENVIRONNEMENT (5 secondes)
```
Menu déroulant en haut à droite → "Doctorat App - Local"
```

### 3. TESTER (1 minute)
```
1. Authentication → Register ADMIN → Send
2. User Service → Get All Users → Send
3. Defense Service → Create Defense Request → Send
✅ Tout fonctionne !
```

---

## 🎯 CE QUE VOUS POUVEZ TESTER

### ✅ Authentification
- Créer des utilisateurs (ADMIN, DOCTORANT, CANDIDAT)
- Se connecter
- Token JWT automatiquement sauvegardé

### ✅ User Service (ADMIN)
- Lister tous les utilisateurs
- Consulter un utilisateur
- Modifier un utilisateur

### ✅ Defense Service
- **DOCTORANT** : Créer une demande de soutenance
- **ADMIN** : Valider les prérequis
- **ADMIN** : Autoriser la soutenance (fixer date/lieu)
- **DIRECTEUR_THESE** : Proposer un jury
- **ADMIN** : Valider le jury
- **ADMIN** : Enregistrer le résultat final
- Consulter les statistiques

### ✅ Registration Service
- **ADMIN** : Créer une campagne d'inscription
- **TOUS** : Consulter les campagnes actives
- **ADMIN** : Modifier une campagne

### ✅ Notification Service
- **ADMIN** : Envoyer une notification
- **TOUS** : Consulter ses notifications
- Marquer comme lu

### ✅ Health Checks
- Vérifier l'état de tous les services
- Consulter Eureka Dashboard

---

## 🔐 SÉCURITÉ TESTÉE

La collection permet de tester la sécurité RBAC :

| Rôle | Accès User Service | Accès Defense | Accès Campaigns | Accès Notifications |
|------|-------------------|---------------|-----------------|---------------------|
| **ADMIN** | ✅ Complet | ✅ Complet | ✅ Complet | ✅ Complet |
| **DIRECTEUR_THESE** | ❌ 403 | ✅ Validation | ❌ 403 | ✅ Consultation |
| **DOCTORANT** | ❌ 403 | ✅ Création/Consultation | ✅ Consultation | ✅ Consultation |
| **CANDIDAT** | ❌ 403 | ❌ 403 | ✅ Consultation | ✅ Consultation |
| **ADMINISTRATIF** | ❌ 403 | ❌ 403 | ❌ 403 | ✅ Envoi |

---

## 💡 FONCTIONNALITÉS AUTOMATIQUES

### 🎁 Token Auto-Sauvegardé
Après chaque Register/Login, le token JWT est **automatiquement extrait** et sauvegardé dans les variables. Plus besoin de copier-coller !

### 🎁 Headers Automatiques
Toutes les requêtes ont automatiquement :
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

### 🎁 Variables Dynamiques
```
{{base_url}}     → http://localhost:8080
{{token}}        → Token JWT actuel
{{user_id}}      → ID de l'utilisateur connecté
{{token_admin}}  → Token ADMIN
{{token_doctorant}} → Token DOCTORANT
{{token_candidat}}  → Token CANDIDAT
```

### 🎁 Scripts de Test Intégrés
Chaque requête d'authentification contient un script qui :
1. Vérifie le code de réponse (200/201)
2. Extrait le token JWT
3. Le sauvegarde automatiquement
4. Affiche un message de confirmation

---

## 📊 EXEMPLES DE RÉPONSES

### ✅ Succès : Register ADMIN
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pb...",
    "tokenType": "Bearer",
    "user": {
        "id": 1,
        "username": "admin_postman",
        "email": "admin@postman.com",
        "role": "ADMIN",
        "enabled": true
    }
}
```
Console Postman affiche : **"Token saved: eyJhbGc..."**

### ✅ Succès : Get All Users
```json
[
    {
        "id": 1,
        "username": "admin_postman",
        "email": "admin@postman.com",
        "role": "ADMIN",
        "firstName": null,
        "lastName": null,
        "phone": null,
        "enabled": true,
        "createdAt": "2025-12-27T10:30:00"
    },
    {
        "id": 2,
        "username": "doctorant_test",
        "email": "doctorant@test.com",
        "role": "DOCTORANT",
        "enabled": true
    }
]
```

### ✅ Succès : Create Defense
```json
{
    "id": 1,
    "titre": "Intelligence Artificielle et Deep Learning",
    "status": "SUBMITTED",
    "doctorantId": 2,
    "directeurTheseId": 1,
    "nbPublications": 5,
    "nbConferences": 3,
    "nbTrainingHours": 300,
    "createdAt": "2025-12-27T10:35:00"
}
```

### ❌ Erreur : 403 Forbidden (DOCTORANT essaie d'accéder à /api/users)
```json
{
    "timestamp": "2025-12-27T10:40:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Access Denied",
    "path": "/api/users"
}
```
✅ **C'est normal !** La sécurité fonctionne correctement.

### ❌ Erreur : 401 Unauthorized (token invalide)
```json
{
    "timestamp": "2025-12-27T10:45:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid or expired JWT token",
    "path": "/api/defenses"
}
```
**Solution** : Refaire Register/Login pour obtenir un nouveau token.

---

## 🎓 WORKFLOW D'APPRENTISSAGE

### Niveau 1 : Débutant (10 min)
1. Importer la collection
2. Register ADMIN
3. Tester les 4 GET principaux :
   - GET /api/users
   - GET /api/defenses/my
   - GET /api/registration/campaigns
   - GET /api/notifications/user/1

### Niveau 2 : Intermédiaire (20 min)
1. Créer une soutenance complète :
   - Create Defense Request
   - Validate Prerequisites
   - Authorize Defense
2. Créer une campagne
3. Envoyer une notification

### Niveau 3 : Avancé (30 min)
1. Tester avec 3 rôles différents (ADMIN, DOCTORANT, CANDIDAT)
2. Vérifier les 403 Forbidden sur les accès interdits
3. Tester le workflow complet de soutenance (8 étapes)
4. Proposer et valider un jury

---

## 🏆 OBJECTIFS DE TEST

### ✅ Tests Fonctionnels
- [ ] Créer un utilisateur ADMIN
- [ ] Lister tous les utilisateurs
- [ ] Créer une demande de soutenance
- [ ] Valider les prérequis
- [ ] Autoriser une soutenance
- [ ] Créer une campagne
- [ ] Envoyer une notification
- [ ] Consulter ses notifications

### ✅ Tests de Sécurité
- [ ] DOCTORANT ne peut pas accéder à /api/users (403)
- [ ] CANDIDAT ne peut pas créer de campagne (403)
- [ ] Sans token → 401 Unauthorized
- [ ] Token expiré → 401 Unauthorized

### ✅ Tests d'Intégration
- [ ] Workflow soutenance complet (création → résultat)
- [ ] Workflow campagne (création → consultation)
- [ ] Notifications liées aux actions (soutenance validée, etc.)

---

## 📞 AIDE RAPIDE

| Problème | Solution |
|----------|----------|
| 401 Unauthorized | Refaire **Register ADMIN** |
| 403 Forbidden | Utiliser un compte **ADMIN** |
| Token vide | Vérifier l'œil 👁️ en haut à droite |
| Service ne répond pas | Vérifier **Health Checks** |
| 404 Not Found | Vérifier l'URL et l'ID |

---

## 🎯 POUR COMMENCER MAINTENANT

1. **Ouvrez Postman**
2. **Import** → Glissez `Postman-Collection-Complete.json` et `Postman-Environment-Local.json`
3. **Sélectionnez** l'environnement "Doctorat App - Local" (en haut à droite)
4. **Exécutez** : `1. Authentication` → `Register ADMIN` → **Send**
5. **Testez** : `2. User Service` → `Get All Users` → **Send**

✅ **Si vous voyez la liste des utilisateurs, tout fonctionne !**

---

**Temps d'installation** : 1 minute  
**Temps de test basique** : 5 minutes  
**Temps de test complet** : 30 minutes  

**Bonne chance ! 🚀**
