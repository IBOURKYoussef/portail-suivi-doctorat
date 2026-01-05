# 🧪 Guide de Test - Nouvelles Fonctionnalités

## 📋 Prérequis

1. ✅ Tous les services compilés
2. ✅ Collection Postman mise à jour
3. ✅ Environnement Postman sélectionné
4. ✅ Répertoires d'upload créés

---

## 🚀 Ordre d'Exécution des Tests

### PHASE 1 : Setup Initial (OBLIGATOIRE)

#### 1. Créer les répertoires d'upload
```powershell
mkdir uploads\defense-service
mkdir uploads\registration-service
```

#### 2. Démarrer les services
- Config Server (8888)
- Discovery Server (8761)
- API Gateway (8080)
- User Service (8081)
- Registration Service (8084)
- Defense Service (8083)
- Notification Service (8085)

#### 3. Exécuter les Register (Folder 1)
- Register ADMIN
- Register DIRECTEUR_THESE
- Register DOCTORANT
- Register CANDIDAT

**✅ Vérifier** : Toutes les variables d'environnement sont remplies

---

### PHASE 2 : Tests Workflow Defense + Documents

#### Test 1 : Créer une Soutenance
```
Dossier : 2. Workflow Soutenance de Thèse
Requête : Create Defense Request (DOCTORANT)
```
**Résultat attendu** : `201 Created` + `defense_id` sauvegardé

---

#### Test 2 : Upload Manuscrit de Thèse
```
Dossier : 2.6 Gestion des Documents
Requête : Upload Document (DOCTORANT)
```

**Configuration requise** :
1. Sélectionner un fichier PDF dans FormData
2. Vérifier les paramètres :
   - `type` : MANUSCRIPT
   - `entityId` : {{defense_id}}
   - `description` : "Manuscrit de thèse - Version finale"

**Résultat attendu** : `201 Created` + Métadonnées du document

**Exemple de réponse** :
```json
{
  "id": 1,
  "fileName": "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf",
  "originalFileName": "These_Microservices.pdf",
  "fileSize": 2456789,
  "contentType": "application/pdf",
  "type": "MANUSCRIPT",
  "entityId": 1,
  "uploadedBy": 3,
  "uploadedAt": "2025-12-28T17:15:00",
  "description": "Manuscrit de thèse - Version finale",
  "downloadUrl": "/api/documents/1/download"
}
```

---

#### Test 3 : Upload Rapport Anti-Plagiat
```
Dossier : 2.6 Gestion des Documents
Requête : Upload Document (DOCTORANT)
```

**Modifier les paramètres** :
- `type` : PLAGIARISM_REPORT
- `description` : "Rapport anti-plagiat"

**Résultat attendu** : `201 Created`

---

#### Test 4 : Vérifier Documents Requis
```
Dossier : 2.6 Gestion des Documents
Requête : Check Required Documents
```

**URL** : `/api/documents/entity/{{defense_id}}/validate?types=MANUSCRIPT&types=PLAGIARISM_REPORT`

**Résultat attendu** : `200 OK` + `true` (car les 2 documents sont uploadés)

---

#### Test 5 : Lister Tous les Documents de la Soutenance
```
Dossier : 2.6 Gestion des Documents
Requête : Get Documents by Entity
```

**Résultat attendu** : `200 OK` + Liste des 2 documents

---

#### Test 6 : Lister Mes Documents
```
Dossier : 2.6 Gestion des Documents
Requête : Get My Documents
```

**Résultat attendu** : `200 OK` + Tous les documents uploadés par le doctorant

---

#### Test 7 : Download Document
```
Dossier : 2.6 Gestion des Documents
Requête : Download Document
```

**Modifier l'URL** : Remplacer `1` par l'ID réel d'un document

**Résultat attendu** : Téléchargement du fichier PDF

---

### PHASE 3 : Tests Workflow Rapporteurs

#### Test 8 : Valider Prérequis (ADMIN)
```
Dossier : 2. Workflow Soutenance de Thèse
Requête : Validate Prerequisites (ADMIN)
```

**URL** : `/api/defenses/{{defense_id}}/validate-prerequisites?approved=true&comment=Dossier complet`

**Résultat attendu** : `200 OK` + Status → `PREREQUISITES_VALIDATED`

---

#### Test 9 : Soumettre Rapport de Rapporteur
```
Dossier : 2.5 Rapporteurs & Jury
Requête : Submit Rapporteur Report (DIRECTEUR_THESE)
```

**⚠️ Note** : Vous devez d'abord avoir des rapporteurs assignés. Pour cela :

1. Le directeur doit proposer un jury (non inclus dans collection actuellement)
2. Les rapporteurs doivent être désignés
3. Ensuite ils peuvent soumettre leurs rapports

**Body** :
```json
{
  "report": "Le candidat démontre une excellente maîtrise du sujet...",
  "reportFilePath": "/documents/rapporteur_report_1.pdf",
  "opinion": "FAVORABLE"
}
```

**Résultat attendu** : `200 OK`

---

#### Test 10 : Lister Rapporteurs
```
Dossier : 2.5 Rapporteurs & Jury
Requête : Get Rapporteurs by Defense
```

**Résultat attendu** : `200 OK` + Liste des rapporteurs avec leurs rapports

---

### PHASE 4 : Tests Statistiques

#### Test 11 : Obtenir Statistiques
```
Dossier : 2.5 Rapporteurs & Jury
Requête : Get Defense Statistics (ADMIN)
```

**Résultat attendu** : `200 OK` + JSON des statistiques

**Exemple** :
```json
{
  "total": 5,
  "submitted": 2,
  "authorized": 1,
  "scheduled": 1,
  "completed": 1,
  "cancelled": 0
}
```

---

### PHASE 5 : Tests Rappels Automatiques

Les rappels s'exécutent automatiquement selon les planifications :

#### Rappel 1 : Campagnes Proches de Clôture
**Cron** : 9h chaque jour

**Test manuel** :
1. Créer une campagne se terminant dans 2 jours
2. Attendre 9h le lendemain
3. Vérifier les logs du Notification Service

**Logs attendus** :
```
=== VÉRIFICATION DES CAMPAGNES PROCHES DE CLÔTURE ===
Campagne 'Doctorat 2025' se termine dans 2 jour(s)
```

---

#### Rappel 2 : Documents Manquants
**Cron** : 10h chaque jour

**Test manuel** :
1. Créer une soutenance SANS uploader les documents requis
2. Attendre 10h
3. Vérifier les notifications

**Logs attendus** :
```
=== VÉRIFICATION DES DOCUMENTS MANQUANTS ===
Rappel documents manquants envoyé à l'utilisateur 3
```

---

#### Rappel 3 : Soutenances à Venir
**Cron** : 8h chaque jour

**Test manuel** :
1. Créer et plannifier une soutenance pour dans 3 jours
2. Attendre 8h
3. Vérifier les notifications

**Logs attendus** :
```
=== RAPPEL DES SOUTENANCES À VENIR ===
Rappel de soutenance envoyé pour la thèse: Architecture Microservices...
```

---

### PHASE 6 : Tests de Suppression

#### Test 12 : Supprimer un Document
```
Dossier : 2.6 Gestion des Documents
Requête : Delete Document
```

**Modifier l'URL** : Utiliser l'ID d'un document créé par le doctorant

**Résultat attendu** : `204 No Content`

**Vérification** :
```
GET /api/documents/entity/{{defense_id}}
```
Le document ne doit plus apparaître dans la liste (soft delete : `active=false`)

---

## 🐛 Résolution de Problèmes

### Erreur : "Impossible de créer le répertoire de stockage"

**Solution** :
```powershell
mkdir uploads\defense-service
mkdir uploads\registration-service
```

Vérifier les permissions d'écriture.

---

### Erreur : "Le fichier est trop volumineux"

**Cause** : Fichier > 10MB

**Solution** : Modifier dans `application.yml` :
```yaml
app:
  document:
    max-file-size: 20971520  # 20MB
```

---

### Erreur : "Type de fichier non autorisé"

**Cause** : Format non supporté

**Formats acceptés** :
- PDF : `application/pdf`
- Images : `image/*`
- Word : `application/msword`, `.docx`

---

### Erreur 404 sur /api/documents

**Cause** : Service non redémarré

**Solution** : Redémarrer Defense Service ou Registration Service

---

### Rappels ne s'exécutent pas

**Vérifications** :
1. `@EnableScheduling` présent dans `NotificationServiceApplication`
2. Service Notification redémarré
3. Crons configurés correctement
4. Logs activés : `ma.spring.notificationservice: DEBUG`

---

## 📊 Résultats Attendus

### Documents
- ✅ Upload réussi avec métadonnées complètes
- ✅ Download fonctionne et retourne le bon fichier
- ✅ Soft delete ne supprime pas physiquement
- ✅ Vérification des documents requis fonctionne

### Rapporteurs
- ✅ Soumission de rapport met à jour le statut
- ✅ Quand tous les rapports sont soumis, status change automatiquement
- ✅ Validation majoritaire fonctionne (≥50% favorable)

### Statistiques
- ✅ Compte exact par status
- ✅ Total cohérent

### Rappels
- ✅ Exécution automatique selon crons
- ✅ Emails envoyés aux bons utilisateurs
- ✅ Messages personnalisés et clairs

---

## ✅ Checklist Complète

- [ ] Services compilés sans erreurs
- [ ] Répertoires d'upload créés
- [ ] Collection Postman re-importée
- [ ] Variables d'environnement remplies
- [ ] Upload document réussi
- [ ] Download document réussi
- [ ] Vérification documents requis OK
- [ ] Liste documents par entité OK
- [ ] Suppression document OK
- [ ] Statistiques affichées
- [ ] Rapporteur peut soumettre rapport
- [ ] Rappels planifiés activés

---

## 🎯 Points de Validation Importants

### 1. Sécurité
- ✅ Seul le propriétaire peut supprimer ses documents
- ✅ Header `X-User-Id` requis pour upload/delete
- ✅ Authorization Bearer token requis partout

### 2. Performance
- ✅ Fichiers stockés localement (pas en BDD)
- ✅ Métadonnées indexées pour recherche rapide
- ✅ Soft delete pour historique

### 3. Fiabilité
- ✅ Noms de fichiers uniques (UUID)
- ✅ Validation taille et format
- ✅ Gestion erreurs avec messages clairs

---

**Date** : 28 décembre 2025  
**Version de test** : 2.0
