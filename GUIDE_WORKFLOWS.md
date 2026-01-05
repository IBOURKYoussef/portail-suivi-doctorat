# 🧪 Guide d'Exécution - Tests Postman par Workflows

## 📋 Organisation par Besoins Métier

Cette collection est organisée par **workflows métier** (cas d'usage) plutôt que par services techniques.

### 🎯 Les 7 Workflows

1. **Setup - Authentification** (5 tests) - Créer les utilisateurs
2. **Workflow Soutenance de Thèse** (9 tests) - Cycle complet d'une soutenance
3. **Workflow Candidature Doctorat** (5 tests) - Cycle d'une candidature
4. **Gestion des Utilisateurs** (2 tests) - Consultation des users
5. **Gestion des Notifications** (3 tests) - Consulter/gérer les notifications
6. **Tests Sécurité RBAC** (3 tests) - Vérifier les permissions (403 attendu)
7. **Tests Authentification** (2 tests) - Vérifier les erreurs auth (401 attendu)

**Total : 29 tests** (24 succès + 5 échecs intentionnels)

---

## ✅ ÉTAPE 1 : Importer les fichiers dans Postman

1. Ouvrir Postman
2. Cliquer sur **Import**
3. Importer :
   - `Postman-Collection-Complete.json`
   - `Postman-Environment-Local.json`
4. Sélectionner l'environnement **"Doctorat App - Local"** (dropdown en haut à droite)

---

## ✅ ÉTAPE 2 : Vérifier les services

```powershell
# Vérifier que tous les services sont actifs
8761,8080,8081,8082,8083,8084 | ForEach-Object { 
    $result = Test-NetConnection -ComputerName localhost -Port $_ -WarningAction SilentlyContinue
    if ($result.TcpTestSucceeded) {
        Write-Host "✅ Port $_ : ACTIF" -ForegroundColor Green
    } else {
        Write-Host "❌ Port $_ : INACTIF" -ForegroundColor Red
    }
}
```

---

## 🔐 WORKFLOW 1 : Setup - Authentification

**Dossier Postman** : `1. Setup - Authentification`

**Objectif** : Créer les 4 types d'utilisateurs et récupérer leurs tokens JWT

### ✅ Test 1 : Register ADMIN

**Méthode** : POST  
**URL** : `{{base_url}}/auth/register`  
**Body** :
```json
{
  "username": "admin_test",
  "password": "admin123",
  "email": "admin@doctorat.ma",
  "role": "ADMIN"
}
```

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `admin_token`, `admin_id`

---

### ✅ Test 2 : Register DIRECTEUR_THESE

**Body** :
```json
{
  "username": "directeur_test",
  "password": "directeur123",
  "email": "directeur@doctorat.ma",
  "role": "DIRECTEUR_THESE"
}
```

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `directeur_token`, `directeur_id`

---

### ✅ Test 3 : Register DOCTORANT

**Body** :
```json
{
  "username": "doctorant_test",
  "password": "doctorant123",
  "email": "doctorant@doctorat.ma",
  "role": "DOCTORANT"
}
```

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `doctorant_token`, `doctorant_id`

---

### ✅ Test 4 : Register CANDIDAT

**Body** :
```json
{
  "username": "candidat_test",
  "password": "candidat123",
  "email": "candidat@doctorat.ma",
  "role": "CANDIDAT"
}
```

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `candidat_token`, `candidat_id`

---

### ✅ Test 5 : Login

**Body** :
```json
{
  "username": "admin_test",
  "password": "admin123"
}
```

**Résultat attendu** : `200 OK` avec token JWT

**⚠️ VÉRIFICATION IMPORTANTE** :  
Cliquer sur l'icône 👁️ (environnement) et vérifier que toutes les variables contiennent des valeurs :
- ✅ `admin_token` : eyJhbGci...
- ✅ `admin_id` : 1
- ✅ `directeur_token` : eyJhbGci...
- ✅ `directeur_id` : 2
- ✅ `doctorant_token` : eyJhbGci...
- ✅ `doctorant_id` : 3
- ✅ `candidat_token` : eyJhbGci...
- ✅ `candidat_id` : 4

---

## 🎓 WORKFLOW 2 : Soutenance de Thèse (Cycle Complet)

**Dossier Postman** : `2. Workflow Soutenance de Thèse`

**Objectif** : Tester le cycle complet d'une soutenance de thèse

### Parcours métier :
1. DOCTORANT crée une demande de soutenance
2. ADMIN valide les prérequis
3. ADMIN autorise la soutenance
4. DIRECTEUR propose le jury
5. ADMIN valide la composition du jury
6. ADMIN enregistre le résultat final

---

### ✅ Test 8 : Create Defense Request (DOCTORANT)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses`  
**Authorization** : Bearer `{{doctorant_token}}`  

**Body** :
```json
{
  "thesisTitle": "Architecture Microservices pour la Gestion des Soutenances de Thèse",
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

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `defense_id`  
**Status de la défense** : `PENDING_VERIFICATION`

---

### ✅ Test 9 : Get My Defenses (DOCTORANT)

**Méthode** : GET  
**URL** : `{{base_url}}/api/defenses/my`  
**Authorization** : Bearer `{{doctorant_token}}`

**Résultat attendu** : `200 OK` - Liste contenant la défense créée

---

### ✅ Test 10 : Get All Defenses (ADMIN)

**Méthode** : GET  
**URL** : `{{base_url}}/api/defenses`  
**Authorization** : Bearer `{{admin_token}}`

**Résultat attendu** : `200 OK` - Liste de toutes les défenses

---

### ✅ Test 11 : Get Defense by ID

**Méthode** : GET  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}`  
**Authorization** : Bearer `{{admin_token}}`

**Résultat attendu** : `200 OK` - Détails complets de la défense

---

### ✅ Test 12 : Validate Prerequisites (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/validate-prerequisites`  
**Authorization** : Bearer `{{admin_token}}`

**Body** :
```json
{
  "approved": true,
  "comments": "Tous les prérequis sont remplis : 5 publications, 3 conférences, 300h de formation"
}
```

**Résultat attendu** : `200 OK`  
**Nouveau status** : `PREREQUISITES_VALIDATED`

---

### ✅ Test 13 : Authorize Defense (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/authorize`  
**Authorization** : Bearer `{{admin_token}}`

**Body** :
```json
{
  "date": "2025-09-15T14:00:00",
  "location": "Amphithéâtre A - Bâtiment Principal",
  "comments": "Soutenance autorisée"
}
```

**Résultat attendu** : `200 OK`  
**Nouveau status** : `AUTHORIZED`

---

### ✅ Test 14 : Propose Jury (DIRECTEUR_THESE)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/jury`  
**Authorization** : Bearer `{{directeur_token}}`

**Body** :
```json
{
  "presidentId": {{admin_id}},
  "rapporteurIds": [{{directeur_id}}],
  "examinateurIds": [{{admin_id}}]
}
```

**Résultat attendu** : `200 OK`  
**Nouveau status** : `JURY_PROPOSED`

---

### ✅ Test 15 : Validate Jury (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/jury/validate`  
**Authorization** : Bearer `{{admin_token}}`

**Body** :
```json
{
  "approved": true,
  "comments": "Composition du jury validée"
}
```

**Résultat attendu** : `200 OK`  
**Nouveau status** : `JURY_VALIDATED`

---

### ✅ Test 16 : Record Final Result (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/result`  
**Authorization** : Bearer `{{admin_token}}`

**Body** :
```json
{
  "result": "PASSED",
  "mention": "TRES_HONORABLE",
  "comments": "Excellente soutenance, travail de qualité exceptionnelle"
}
```

**Résultat attendu** : `200 OK`  
**Nouveau status** : `COMPLETED` ✅

---

## 📝 WORKFLOW 3 : Candidature Doctorat (Cycle Complet)

**Dossier Postman** : `3. Workflow Candidature Doctorat`

**Objectif** : Tester le processus de candidature au doctorat

### Parcours métier :
1. ADMIN crée une campagne de recrutement
2. CANDIDAT soumet sa candidature
3. DIRECTEUR évalue la candidature
4. ADMIN approuve définitivement

---

### ✅ Test 17 : Create Campaign (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/registration/campaigns`  
**Authorization** : Bearer `{{admin_token}}`

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

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `campaign_id`

---

### ✅ Test 18 : Get All Campaigns

**Méthode** : GET  
**URL** : `{{base_url}}/api/registration/campaigns`  
**Authorization** : Bearer `{{candidat_token}}`

**Résultat attendu** : `200 OK` - Liste des campagnes actives

---

### ✅ Test 19 : Submit Registration (CANDIDAT)

**Méthode** : POST  
**URL** : `{{base_url}}/api/registration/applications`  
**Authorization** : Bearer `{{candidat_token}}`

**Body** :
```json
{
  "campaignId": {{campaign_id}},
  "researchField": "Intelligence Artificielle",
  "proposedDirectorId": {{directeur_id}},
  "motivationLetter": "Je souhaite poursuivre mes études doctorales dans le domaine de l'intelligence artificielle car...",
  "cvUrl": "https://example.com/cv/candidat_cv.pdf"
}
```

**Résultat attendu** : `201 Created`  
**Auto-sauvegarde** : `application_id`

---

### ✅ Test 20 : Review Application (DIRECTEUR_THESE)

**Méthode** : POST  
**URL** : `{{base_url}}/api/registration/applications/{{application_id}}/review`  
**Authorization** : Bearer `{{directeur_token}}`

**Body** :
```json
{
  "approved": true,
  "comments": "Bon profil, expérience pertinente en intelligence artificielle"
}
```

**Résultat attendu** : `200 OK`

---

### ✅ Test 21 : Approve Application (ADMIN)

**Méthode** : POST  
**URL** : `{{base_url}}/api/registration/applications/{{application_id}}/approve`  
**Authorization** : Bearer `{{admin_token}}`

**Body** :
```json
{
  "approved": true,
  "comments": "Candidature approuvée après examen du dossier"
}
```

**Résultat attendu** : `200 OK`

---

## 👥 WORKFLOW 4 : Gestion des Utilisateurs

**Dossier Postman** : `4. Gestion des Utilisateurs`

---

### ✅ Test 22 : Get All Users (ADMIN)

**Méthode** : GET  
**URL** : `{{base_url}}/api/users`  
**Authorization** : Bearer `{{admin_token}}`

**Résultat attendu** : `200 OK` - Liste des 4 utilisateurs créés

---

### ✅ Test 23 : Get User by ID

**Méthode** : GET  
**URL** : `{{base_url}}/api/users/{{admin_id}}`  
**Authorization** : Bearer `{{admin_token}}`

**Résultat attendu** : `200 OK` - Détails de l'utilisateur ADMIN

---

## 🔔 WORKFLOW 5 : Gestion des Notifications

**Dossier Postman** : `5. Gestion des Notifications`

---

### ✅ Test 24 : Get My Notifications

**Méthode** : GET  
**URL** : `{{base_url}}/api/notifications/my`  
**Authorization** : Bearer `{{doctorant_token}}`

**Résultat attendu** : `200 OK` - Liste des notifications du DOCTORANT

---

### ✅ Test 25 : Get Unread Count

**Méthode** : GET  
**URL** : `{{base_url}}/api/notifications/unread-count`  
**Authorization** : Bearer `{{doctorant_token}}`

**Résultat attendu** : `200 OK` - Nombre de notifications non lues

---

### ✅ Test 26 : Mark Notification as Read

**Méthode** : PUT  
**URL** : `{{base_url}}/api/notifications/1/read`  
**Authorization** : Bearer `{{doctorant_token}}`

**Résultat attendu** : `200 OK` (ou 404 si pas de notification avec cet ID)

---

## 🔒 WORKFLOW 6 : Tests Sécurité RBAC

**Dossier Postman** : `6. Tests Sécurité RBAC`

**Objectif** : Vérifier que le contrôle d'accès fonctionne correctement

---

### ❌ Test 27 : CANDIDAT Create Defense (403 Expected)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses`  
**Authorization** : Bearer `{{candidat_token}}`

**Résultat attendu** : `403 Forbidden` ⛔  
**Raison** : Seul DOCTORANT peut créer une défense

---

### ❌ Test 28 : DOCTORANT Validate Prerequisites (403 Expected)

**Méthode** : POST  
**URL** : `{{base_url}}/api/defenses/{{defense_id}}/validate-prerequisites`  
**Authorization** : Bearer `{{doctorant_token}}`

**Résultat attendu** : `403 Forbidden` ⛔  
**Raison** : Seul ADMIN peut valider

---

### ❌ Test 29 : CANDIDAT Get All Users (403 Expected)

**Méthode** : GET  
**URL** : `{{base_url}}/api/users`  
**Authorization** : Bearer `{{candidat_token}}`

**Résultat attendu** : `403 Forbidden` ⛔  
**Raison** : Seul ADMIN peut lire tous les utilisateurs

---

## 🚫 WORKFLOW 7 : Tests Authentification

**Dossier Postman** : `7. Tests Authentification`

---

### ❌ Test 30 : No Token (401 Expected)

**Méthode** : GET  
**URL** : `{{base_url}}/api/defenses`  
**Authorization** : *AUCUNE*

**Résultat attendu** : `401 Unauthorized` ⛔

---

### ❌ Test 31 : Invalid Token (401 Expected)

**Méthode** : GET  
**URL** : `{{base_url}}/api/defenses`  
**Authorization** : Bearer invalid_token_xyz123

**Résultat attendu** : `401 Unauthorized` ⛔

---

## 📊 Résumé des Résultats Attendus

| Workflow | Tests | Succès | Erreurs |
|----------|-------|--------|---------|
| 1. Setup - Authentification | 5 | 5 ✅ | - |
| 2. Soutenance de Thèse | 9 | 9 ✅ | - |
| 3. Candidature Doctorat | 5 | 5 ✅ | - |
| 4. Gestion Utilisateurs | 2 | 2 ✅ | - |
| 5. Gestion Notifications | 3 | 3 ✅ | - |
| 6. Tests RBAC | 3 | - | 3 ⛔ (intentionnel) |
| 7. Tests Auth | 2 | - | 2 ⛔ (intentionnel) |
| **TOTAL** | **29** | **24** | **5** |

---

## ✅ Checklist Finale

- [ ] J'ai importé les 2 fichiers JSON dans Postman
- [ ] J'ai sélectionné l'environnement "Doctorat App - Local"
- [ ] Tous les services sont actifs (8761, 8080-8084)
- [ ] J'ai exécuté les 5 tests d'authentification EN PREMIER
- [ ] J'ai vérifié que les variables d'environnement sont remplies (👁️)
- [ ] J'ai suivi l'ordre des workflows (1 → 2 → 3 → 4 → 5 → 6 → 7)
- [ ] Les 24 tests de succès retournent 200/201
- [ ] Les 5 tests d'erreur retournent 401/403 comme prévu

---

## 🛠️ Troubleshooting

### ❌ Erreur 403 Forbidden

**Cause** : Variable `{{xxx_token}}` est vide  
**Solution** : Exécuter les tests d'authentification (Workflow 1)

### ❌ Erreur 401 Unauthorized

**Cause** : Token expiré ou invalide  
**Solution** : Refaire Register + Login pour obtenir un nouveau token

### ❌ Erreur 404 Not Found

**Cause** : Variable `{{defense_id}}` ou `{{campaign_id}}` est vide  
**Solution** : Exécuter les tests de création (Test 8, Test 17)

### ❌ Service ne répond pas

**Cause** : Service arrêté  
**Solution** : Vérifier avec `Test-NetConnection` et redémarrer si nécessaire

---

## 📞 Support

Si vous rencontrez des problèmes :

1. Vérifier que TOUS les services sont actifs
2. Vérifier que les variables d'environnement sont remplies (icône 👁️)
3. Respecter l'ordre d'exécution des workflows
4. Consulter les logs des services pour les erreurs détaillées

**Logs des services** :
```powershell
# Voir les logs du Defense Service par exemple
docker logs defense-service
```
