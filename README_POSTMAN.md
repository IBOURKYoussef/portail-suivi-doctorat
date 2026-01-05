# 📦 FICHIERS POSTMAN - APPLICATION DOCTORAT

## 🎯 FICHIERS DISPONIBLES

| Fichier | Description |
|---------|-------------|
| **Postman-Collection-Complete.json** | Collection complète avec tous les endpoints (50+ requêtes) |
| **Postman-Environment-Local.json** | Variables d'environnement (base_url, tokens, etc.) |
| **GUIDE_RAPIDE_POSTMAN.md** | Guide rapide (5 min) pour démarrer |
| **GUIDE_POSTMAN.md** | Documentation complète et détaillée |

---

## ⚡ DÉMARRAGE RAPIDE (3 ÉTAPES)

### 1️⃣ Importer dans Postman

Ouvrez Postman → **Import** → Glissez ces 2 fichiers :
- ✅ `Postman-Collection-Complete.json`
- ✅ `Postman-Environment-Local.json`

### 2️⃣ Activer l'environnement

En haut à droite de Postman, sélectionnez :
- **"Doctorat App - Local"**

### 3️⃣ Créer un utilisateur ADMIN

Dans la collection :
1. Ouvrez **`1. Authentication`** → **`Register ADMIN`**
2. Cliquez **Send**
3. ✅ Le token est automatiquement sauvegardé !

**Vous êtes prêt à tester !** 🎉

---

## 📚 STRUCTURE DE LA COLLECTION

```
Application Doctorat - Tests Complets
│
├── 1. Authentication (4 requêtes)
│   ├── Register ADMIN ⭐ (Commencer ici)
│   ├── Register DOCTORANT
│   ├── Register CANDIDAT
│   └── Login
│
├── 2. User Service (3 requêtes) - ADMIN uniquement
│   ├── Get All Users
│   ├── Get User By ID
│   └── Update User
│
├── 3. Defense Service (13 requêtes)
│   ├── Create Defense Request (DOCTORANT)
│   ├── Get My Defenses (DOCTORANT)
│   ├── Validate Prerequisites (ADMIN)
│   ├── Authorize Defense (ADMIN)
│   ├── Record Result (ADMIN/DIRECTEUR_THESE)
│   ├── Propose Jury (DIRECTEUR_THESE)
│   ├── Validate Jury (ADMIN)
│   └── ...
│
├── 4. Registration Service (5 requêtes)
│   ├── Create Campaign (ADMIN)
│   ├── Get All Campaigns
│   ├── Update Campaign (ADMIN)
│   └── ...
│
├── 5. Notification Service (3 requêtes)
│   ├── Send Notification (ADMIN/ADMINISTRATIF)
│   ├── Get User Notifications
│   └── Mark as Read
│
└── 6. Health Checks (4 requêtes)
    ├── Gateway Health
    ├── User Service Health
    ├── Defense Service Health
    └── Eureka Dashboard
```

---

## 🔑 CARACTÉRISTIQUES PRINCIPALES

### ✅ Authentification Automatique
- Le token JWT est **automatiquement extrait et sauvegardé** après Register/Login
- Toutes les requêtes utilisent `{{token}}` automatiquement
- **Pas besoin de copier-coller le token !**

### ✅ Variables Dynamiques
- `{{base_url}}` = http://localhost:8080
- `{{token}}` = Token JWT actuel (auto-sauvegardé)
- `{{user_id}}` = ID utilisateur (auto-sauvegardé)
- `{{token_doctorant}}` = Token pour rôle DOCTORANT
- `{{token_candidat}}` = Token pour rôle CANDIDAT

### ✅ Tests Pré-configurés
Chaque requête Register/Login a un script de test qui sauvegarde automatiquement :
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.collectionVariables.set("token", jsonData.accessToken);
    pm.environment.set("token", jsonData.accessToken);
}
```

### ✅ Exemples de Données Réalistes
Toutes les requêtes POST ont des exemples de données prêts à l'emploi.

---

## 🧪 SCÉNARIOS DE TEST INCLUS

### Scénario 1 : Gestion Utilisateur (ADMIN)
1. Register ADMIN
2. Get All Users
3. Get User By ID
4. Update User

### Scénario 2 : Workflow Soutenance Complet
1. Register ADMIN
2. Create Defense Request
3. Validate Prerequisites
4. Authorize Defense
5. Propose Jury
6. Validate Jury
7. Record Result
8. Get Defense By ID (vérifier status COMPLETED)

### Scénario 3 : Gestion Campagne
1. Register ADMIN
2. Create Campaign
3. Get All Campaigns
4. Get Active Campaigns
5. Update Campaign

### Scénario 4 : Test Multi-Rôles
1. Register ADMIN → Tester tous les endpoints (✅ Tout OK)
2. Register DOCTORANT → Tester (✅ Defense OK, ❌ Users 403)
3. Register CANDIDAT → Tester (✅ Campaigns OK, ❌ Create 403)

---

## 📊 CODES DE RÉPONSE

| Code | Signification | Action |
|------|---------------|--------|
| **200 OK** | ✅ Succès | Données dans le body |
| **201 Created** | ✅ Ressource créée | ID dans le body |
| **400 Bad Request** | ❌ Données invalides | Vérifier le JSON |
| **401 Unauthorized** | ❌ Token invalide | Refaire Register/Login |
| **403 Forbidden** | ❌ Accès refusé | Rôle insuffisant |
| **404 Not Found** | ❌ Ressource inexistante | Vérifier l'ID |
| **500 Server Error** | ❌ Erreur serveur | Voir logs du service |

---

## 🔧 CONFIGURATION REQUISE

### Services à démarrer (dans l'ordre) :
1. **Eureka Discovery** (8761) - 30 sec
2. **User Service** (8081) - 20 sec
3. **Defense Service** (8083) - 15 sec
4. **Registration Service** (8082) - 15 sec
5. **Notification Service** (8084) - 15 sec
6. **API Gateway** (8080) - 25 sec

**Total** : ~2 minutes de démarrage

**Script automatique disponible** :
```powershell
.\restart-all.ps1
```

---

## 💡 CONSEILS D'UTILISATION

### 🎯 Pour débuter
1. Lisez **GUIDE_RAPIDE_POSTMAN.md** (5 min)
2. Importez les 2 fichiers JSON
3. Exécutez **Register ADMIN**
4. Testez les 4 services principaux

### 🎯 Pour approfondir
- Lisez **GUIDE_POSTMAN.md** (documentation complète)
- Testez tous les scénarios
- Créez des utilisateurs avec différents rôles
- Testez les codes d'erreur (403, 401, etc.)

### 🎯 Pour développer
- Dupliquez les requêtes existantes
- Modifiez les données de test
- Ajoutez vos propres tests automatiques
- Exportez la collection modifiée

---

## 🚨 DÉPANNAGE

### ❌ Import échoue
**Solution** : Vérifiez que les fichiers sont bien au format JSON valide

### ❌ Toutes les requêtes → 401
**Solution** : 
1. Refaire **Register ADMIN**
2. Vérifier que le token est sauvegardé (œil 👁️ en haut à droite)

### ❌ Variables {{token}} vides
**Solution** :
1. Vérifier que l'environnement "Doctorat App - Local" est sélectionné
2. Refaire Register ADMIN
3. Vérifier dans la Console Postman : "Token saved: ..."

### ❌ 403 Forbidden partout
**Solution** : Utiliser Register ADMIN au lieu de DOCTORANT/CANDIDAT

### ❌ Services ne répondent pas
**Solution** :
1. Vérifier que tous les services sont démarrés
2. Tester **6. Health Checks** → Tous doivent être UP
3. Voir http://localhost:8761 (Eureka) → Tous les services enregistrés

---

## 📞 SUPPORT

- **Documentation API** : `DOCUMENTATION_SECURITE.md`
- **Guide complet** : `GUIDE_POSTMAN.md`
- **Guide rapide** : `GUIDE_RAPIDE_POSTMAN.md`
- **Tests manuels** : `GUIDE_TEST.md`

---

## ✅ VALIDATION

Testez cette checklist pour valider l'installation :

- [ ] Collection importée dans Postman
- [ ] Environnement "Doctorat App - Local" actif
- [ ] Register ADMIN → 201 Created
- [ ] Token visible dans l'œil 👁️
- [ ] GET /api/users → 200 OK
- [ ] POST /api/defenses → 201 Created
- [ ] POST /api/registration/campaigns → 201 Created
- [ ] POST /api/notifications → 200 OK
- [ ] DOCTORANT → 403 sur /api/users (sécurité OK)

**Si tout est ✅ : Votre environnement est prêt !** 🎉

---

**Version** : 1.0  
**Date** : 27 décembre 2025  
**Nombre de requêtes** : 50+  
**Temps de test complet** : 10-15 minutes
