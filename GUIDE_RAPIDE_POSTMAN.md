# 🎯 GUIDE RAPIDE POSTMAN

## 📥 1. IMPORT (1 minute)

1. Ouvrez **Postman**
2. Cliquez sur **Import** (en haut à gauche)
3. Glissez-déposez ces 2 fichiers :
   - ✅ `Postman-Collection-Complete.json` (la collection)
   - ✅ `Postman-Environment-Local.json` (l'environnement)
4. En haut à droite, sélectionnez **"Doctorat App - Local"** dans le menu déroulant

---

## 🔐 2. CRÉER UN UTILISATEUR ADMIN (30 secondes)

1. Dans la collection, ouvrez :  
   **`1. Authentication`** → **`Register ADMIN`**

2. Cliquez sur **Send**

3. ✅ **Résultat attendu** :
   ```json
   {
       "accessToken": "eyJhbGc...",
       "user": {
           "id": 1,
           "username": "admin_postman",
           "role": "ADMIN"
       }
   }
   ```

4. 🎉 **Le token est automatiquement sauvegardé !**  
   Vous voyez dans la Console : `Token saved: eyJhbGc...`

---

## ✅ 3. TESTER LES SERVICES (2 minutes)

Le token est **automatiquement injecté** dans toutes les requêtes.

### Test 1 : User Service
- Ouvrez : **`2. User Service`** → **`Get All Users`**
- Cliquez **Send**
- ✅ `200 OK` - Liste des utilisateurs

### Test 2 : Defense Service  
- Ouvrez : **`3. Defense Service`** → **`Create Defense Request`**
- Cliquez **Send**
- ✅ `201 Created` - Soutenance créée avec un ID

### Test 3 : Registration Service
- Ouvrez : **`4. Registration Service`** → **`Create Campaign`**
- Cliquez **Send**
- ✅ `201 Created` - Campagne créée

### Test 4 : Notification Service
- Ouvrez : **`5. Notification Service`** → **`Send Notification`**
- Cliquez **Send**
- ✅ `200 OK` - Notification envoyée

---

## 🔄 WORKFLOW COMPLET : Soutenance de A à Z

Exécutez dans l'ordre :

1. **Register ADMIN** → Token sauvegardé ✅
2. **Create Defense Request** → ID: 1 créé ✅
3. **Get Defense By ID** → Status: SUBMITTED
4. **Validate Prerequisites** → Status: PREREQUISITES_VALIDATED
5. **Authorize Defense** → Status: AUTHORIZED (date fixée)
6. **Propose Jury** → Jury ajouté
7. **Validate Jury** → Jury validé
8. **Record Result** → Status: COMPLETED

---

## 🧪 TESTER AVEC DIFFÉRENTS RÔLES

### DOCTORANT
1. Exécutez **`Register DOCTORANT`**
2. Token sauvegardé dans `{{token_doctorant}}`
3. Testez :
   - ✅ **Create Defense Request** → 201 OK
   - ❌ **Get All Users** → 403 Forbidden (normal)

### CANDIDAT
1. Exécutez **`Register CANDIDAT`**
2. Token sauvegardé dans `{{token_candidat}}`
3. Testez :
   - ✅ **Get All Campaigns** → 200 OK
   - ❌ **Create Campaign** → 403 Forbidden (normal)

---

## ❌ CODES D'ERREUR

| Code | Signification | Solution |
|------|---------------|----------|
| **401** | Token invalide | Refaire **Register ADMIN** |
| **403** | Rôle insuffisant | Utiliser un compte ADMIN |
| **404** | Ressource inexistante | Vérifier l'ID dans l'URL |
| **500** | Erreur serveur | Voir les logs du service |

---

## 🔧 DÉPANNAGE EXPRESS

### ❌ Toutes les requêtes renvoient 401

**Cause** : Token expiré ou invalide

**Solution** :
1. Refaire **`Register ADMIN`**
2. Le nouveau token sera automatiquement sauvegardé
3. Retester

### ❌ Token non sauvegardé automatiquement

**Solution** :
1. Cliquez sur l'œil 👁️ en haut à droite
2. Vérifiez `token` dans les variables
3. Si vide, refaire **Register ADMIN**

### ❌ 403 Forbidden

**Cause** : Votre rôle n'a pas accès

**Solution** : Utiliser **Register ADMIN** pour avoir tous les droits

---

## 💡 ASTUCES

### Variables automatiques
- `{{token}}` → Token JWT (auto-sauvegardé)
- `{{user_id}}` → ID utilisateur (auto-sauvegardé)
- `{{base_url}}` → http://localhost:8080

### Modifier les données de test
Cliquez sur une requête → **Body** → Modifiez le JSON

### Vérifier l'état des services
**`6. Health Checks`** → Testez tous les endpoints health
- ✅ Tous doivent retourner `"status": "UP"`

---

## ✅ CHECKLIST

Avant de tester :
- [ ] Services démarrés (via `restart-all.ps1`)
- [ ] Collection importée dans Postman
- [ ] Environnement "Doctorat App - Local" sélectionné
- [ ] **Register ADMIN** exécuté avec succès (201)
- [ ] Token visible dans l'œil 👁️ (en haut à droite)

Tests de base :
- [ ] GET /api/users → 200 OK
- [ ] POST /api/defenses → 201 Created
- [ ] POST /api/registration/campaigns → 201 Created
- [ ] POST /api/notifications → 200 OK

Tests de sécurité :
- [ ] DOCTORANT ne peut pas accéder à GET /api/users (403)
- [ ] CANDIDAT ne peut pas créer de campagne (403)
- [ ] Sans token → 401 Unauthorized

---

**Temps total** : 5-10 minutes pour tout tester  
**Documentation complète** : Voir `GUIDE_POSTMAN.md`

🎯 **Vous êtes prêt à tester l'application !**
