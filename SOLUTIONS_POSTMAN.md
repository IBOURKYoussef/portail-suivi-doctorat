# 🔧 SOLUTIONS AUX PROBLÈMES POSTMAN

## ❌ Problème 1 : Erreur 500 sur `/result`

### Erreur
```
500 Internal Server Error
"An unexpected error occurred"
```

### Cause
Tu utilises `result=PASSED` mais l'enum `DefenseResult` accepte :
- ✅ `ACCEPTED` (Admis)
- ✅ `ACCEPTED_WITH_CORRECTIONS` (Admis avec corrections)
- ✅ `REJECTED` (Ajourné)
- ✅ `POSTPONED` (Reporté)

❌ `PASSED` n'existe pas !

### Solution
✅ **Corrigé dans la collection** : `result=PASSED` → `result=ACCEPTED`

---

## ❌ Problème 2 : Devoir saisir base_url et autres variables

### Symptôme
Les variables `{{base_url}}`, `{{admin_token}}`, `{{defense_id}}` ne sont pas remplacées automatiquement

### Cause
**Tu n'as pas sélectionné l'environnement dans Postman !**

### Solution

#### Étape 1 : Vérifier que l'environnement est importé

1. Clique sur **Environments** (icône ⚙️ en haut à droite)
2. Cherche **"Doctorat App - Local"**
3. Si absent → Ré-importer `Postman-Environment-Local.json`

#### Étape 2 : Sélectionner l'environnement

1. En haut à droite, tu dois voir un **dropdown** avec "No Environment"
2. Clique dessus
3. Sélectionne **"Doctorat App - Local"**

#### Étape 3 : Vérifier les variables

1. Clique sur l'icône **œil 👁️** à côté du dropdown
2. Tu dois voir :
   ```
   base_url: http://localhost:8080
   admin_token: (sera rempli après Register ADMIN)
   defense_id: (sera rempli après Create Defense)
   ```

---

## ✅ Workflow Complet Corrigé

### 1. Configuration Postman (À FAIRE UNE SEULE FOIS)

```
1. Importer Postman-Collection-Complete.json (si pas déjà fait)
2. Importer Postman-Environment-Local.json (si pas déjà fait)
3. Sélectionner "Doctorat App - Local" dans le dropdown en haut à droite ⬅️ IMPORTANT
```

### 2. Exécuter les Tests d'Authentification

**Ordre OBLIGATOIRE** :

1. **Register ADMIN** → Clic "Send" → Vérifie "201 Created"
   - Variables auto-remplies : `admin_token`, `admin_id`

2. **Register DIRECTEUR_THESE** → "Send" → "201 Created"
   - Variables : `directeur_token`, `directeur_id`

3. **Register DOCTORANT** → "Send" → "201 Created"
   - Variables : `doctorant_token`, `doctorant_id`

4. **Register CANDIDAT** → "Send" → "201 Created"
   - Variables : `candidat_token`, `candidat_id`

### 3. Vérification des Variables

Clique sur l'œil 👁️ et vérifie :

```
✅ base_url: http://localhost:8080
✅ admin_token: eyJhbGci... (valeur présente)
✅ admin_id: 1 (ou un nombre)
✅ directeur_token: eyJhbGci... (valeur présente)
✅ directeur_id: 2
✅ doctorant_token: eyJhbGci... (valeur présente)
✅ doctorant_id: 3
✅ candidat_token: eyJhbGci... (valeur présente)
✅ candidat_id: 4
```

Si tout est rempli → Tu peux continuer les tests !

### 4. Workflow Soutenance de Thèse

**Test 8 - Create Defense** (DOCTORANT)
```
POST {{base_url}}/api/defenses
Authorization: Bearer {{doctorant_token}}
Body: JSON avec directorId: {{directeur_id}}
```
→ Status: `SUBMITTED`
→ Variable auto-remplie : `defense_id`

**Test 12 - Validate Prerequisites** (ADMIN)
```
POST {{base_url}}/api/defenses/{{defense_id}}/validate-prerequisites?approved=true&comment=OK
Authorization: Bearer {{admin_token}}
```
→ Status: `PREREQUISITES_VALIDATED`

**Test 13 - Authorize Defense** (ADMIN)
```
POST {{base_url}}/api/defenses/{{defense_id}}/authorize?authorized=true&defenseDate=2025-09-15T14:00:00&location=Amphi&room=A&comment=OK
Authorization: Bearer {{admin_token}}
```
→ Status: `AUTHORIZED`

**Test 16 - Record Result** (ADMIN) ✅ **CORRIGÉ**
```
POST {{base_url}}/api/defenses/{{defense_id}}/result?result=ACCEPTED&mention=TRES_HONORABLE&remarks=Excellent
Authorization: Bearer {{admin_token}}
```
→ Status: `COMPLETED`

---

## 📝 Valeurs Acceptées pour `result`

| Valeur | Description |
|--------|-------------|
| `ACCEPTED` | Admis |
| `ACCEPTED_WITH_CORRECTIONS` | Admis avec corrections |
| `REJECTED` | Ajourné |
| `POSTPONED` | Reporté |

---

## 🎯 Checklist de Dépannage

### Si les variables ne se remplissent pas :

- [ ] J'ai sélectionné "Doctorat App - Local" dans le dropdown
- [ ] J'ai exécuté "Register ADMIN" et reçu 201 Created
- [ ] J'ai cliqué sur l'œil 👁️ et vu que `admin_token` est rempli
- [ ] Les scripts dans l'onglet "Tests" de chaque requête sont présents

### Si j'ai une erreur 500 sur `/result` :

- [ ] J'utilise `result=ACCEPTED` (pas PASSED)
- [ ] La défense est dans le statut `AUTHORIZED` ou `SCHEDULED`
- [ ] Le Defense Service a été recompilé et redémarré avec les corrections

---

## 📸 Capture d'Écran - Où Sélectionner l'Environnement

```
┌─────────────────────────────────────────────────────┐
│ Postman                                              │
│ ┌─────────────────────────────────────────────────┐ │
│ │ Collections  Environments  [No Environment ▼]   │ │ ← ICI !
│ └─────────────────────────────────────────────────┘ │
│                                                      │
│ Clique sur "No Environment" et choisis :            │
│   → "Doctorat App - Local"                          │
└─────────────────────────────────────────────────────┘
```

---

## ✅ Après Configuration

Une fois l'environnement sélectionné, toutes les requêtes utiliseront automatiquement :

- `{{base_url}}` → `http://localhost:8080`
- `{{admin_token}}` → Token JWT de l'admin
- `{{doctorant_token}}` → Token JWT du doctorant
- `{{defense_id}}` → ID de la défense créée
- etc.

**Tu n'auras PLUS à saisir manuellement !** 🎉

---

## 🔄 Actions Immédiates

1. **Ré-importer** `Postman-Collection-Complete.json` (mis à jour avec `ACCEPTED`)
2. **Sélectionner** "Doctorat App - Local" en haut à droite
3. **Exécuter** les 4 Register (ADMIN, DIRECTEUR, DOCTORANT, CANDIDAT)
4. **Vérifier** que les tokens sont dans les variables (œil 👁️)
5. **Tester** le workflow complet !
