# 🆕 Nouvelles Fonctionnalités Implémentées

## 📋 Vue d'ensemble

Ce document détaille les nouvelles fonctionnalités ajoutées au système de gestion des soutenances de thèse.

---

## 1. 📁 Gestion des Documents

### Description
Système complet de gestion de documents permettant l'upload, le download et la gestion des fichiers liés aux soutenances et candidatures.

### Services concernés
- **Defense Service** : Documents de soutenance (manuscrit, rapports, présentations)
- **Registration Service** : Documents de candidature (CV, diplômes, lettres)

### Endpoints Defense Service

#### Upload Document
```http
POST /api/documents/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}
X-User-Id: {userId}

FormData:
- file: [fichier]
- type: MANUSCRIPT | PLAGIARISM_REPORT | RAPPORTEUR_REPORT | etc.
- entityId: {defenseId}
- description: "Description optionnelle"
```

#### Download Document
```http
GET /api/documents/{id}/download
Authorization: Bearer {token}
X-User-Id: {userId}
```

#### Lister Documents par Entité
```http
GET /api/documents/entity/{defenseId}
Authorization: Bearer {token}
```

#### Mes Documents
```http
GET /api/documents/my
Authorization: Bearer {token}
X-User-Id: {userId}
```

#### Vérifier Documents Requis
```http
GET /api/documents/entity/{defenseId}/validate?types=MANUSCRIPT&types=PLAGIARISM_REPORT
Authorization: Bearer {token}
```

#### Supprimer Document
```http
DELETE /api/documents/{id}
Authorization: Bearer {token}
X-User-Id: {userId}
```

### Types de Documents - Defense Service

| Type | Description |
|------|-------------|
| `MANUSCRIPT` | Manuscrit de thèse |
| `PLAGIARISM_REPORT` | Rapport anti-plagiat |
| `PUBLICATIONS_REPORT` | Rapport des publications |
| `TRAINING_CERTIFICATES` | Attestations de formation |
| `AUTHORIZATION_REQUEST` | Demande d'autorisation |
| `RAPPORTEUR_REPORT` | Rapport de rapporteur |
| `DEFENSE_PV` | Procès-verbal |
| `THESIS_PDF` | PDF de la thèse |
| `JURY_REPORT` | Rapport de jury |
| `DEFENSE_PRESENTATION` | Présentation |
| `SUPPORTING_DOCUMENT` | Document justificatif |
| `CV` | Curriculum Vitae |
| `OTHER` | Autre |

### Types de Documents - Registration Service

| Type | Description |
|------|-------------|
| `CV` | Curriculum Vitae |
| `MOTIVATION_LETTER` | Lettre de motivation |
| `DIPLOMA` | Diplôme |
| `TRANSCRIPT` | Relevé de notes |
| `RESEARCH_PROJECT` | Projet de recherche |
| `RECOMMENDATION_LETTER` | Lettre de recommandation |
| `ID_CARD` | Carte d'identité |
| `BIRTH_CERTIFICATE` | Acte de naissance |
| `OTHER` | Autre |

### Configuration

Fichier `application.yml` :
```yaml
app:
  document:
    upload-dir: ./uploads/defense-service  # Répertoire de stockage
    max-file-size: 10485760  # 10MB max
```

### Formats Acceptés
- **PDF** : application/pdf
- **Images** : image/*
- **Word** : application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document

---

## 2. 🔔 Rappels Automatiques (ReminderScheduler)

### Description
Service de rappels automatiques pour les échéances importantes.

### Service concerné
**Notification Service**

### Tâches Planifiées

#### 1. Vérification Campagnes Proches de Clôture
**Cron** : `0 0 9 * * ?` (Chaque jour à 9h)

Vérifie les campagnes se terminant dans moins de 3 jours et envoie des rappels aux candidats potentiels.

```java
@Scheduled(cron = "0 0 9 * * ?")
public void checkCampaignEndingSoon()
```

#### 2. Vérification Documents Manquants
**Cron** : `0 0 10 * * ?` (Chaque jour à 10h)

Vérifie les soutenances avec documents manquants et envoie des rappels aux doctorants.

```java
@Scheduled(cron = "0 0 10 * * ?")
public void checkMissingDocuments()
```

Documents requis vérifiés :
- Manuscrit de thèse (MANUSCRIPT)
- Rapport anti-plagiat (PLAGIARISM_REPORT)

#### 3. Rappel Soutenances à Venir
**Cron** : `0 0 8 * * ?` (Chaque jour à 8h)

Envoie des rappels 3 jours avant une soutenance au doctorant ET au directeur.

```java
@Scheduled(cron = "0 0 8 * * ?")
public void remindUpcomingDefenses()
```

#### 4. Nettoyage Anciennes Notifications
**Cron** : `0 0 2 * * MON` (Chaque lundi à 2h)

Nettoie les notifications de plus de 90 jours.

```java
@Scheduled(cron = "0 0 2 * * MON")
public void cleanOldNotifications()
```

### Activation

Annotation dans `NotificationServiceApplication` :
```java
@EnableScheduling
```

Configuration RestTemplate dans `AppConfig` :
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

---

## 3. 📊 Statistiques Defense

### Description
Endpoint pour obtenir des statistiques complètes sur les soutenances.

### Endpoint

```http
GET /api/defenses/statistics
Authorization: Bearer {admin_token}
```

### Réponse
```json
{
  "total": 150,
  "submitted": 20,
  "authorized": 45,
  "scheduled": 15,
  "completed": 65,
  "cancelled": 5
}
```

### Implémentation

```java
public Map<String, Long> getStatistics() {
    Map<String, Long> stats = new HashMap<>();
    stats.put("total", defenseRepository.count());
    stats.put("submitted", defenseRepository.countByStatus(DefenseStatus.SUBMITTED));
    stats.put("authorized", defenseRepository.countByStatus(DefenseStatus.AUTHORIZED));
    stats.put("scheduled", defenseRepository.countByStatus(DefenseStatus.SCHEDULED));
    stats.put("completed", defenseRepository.countByStatus(DefenseStatus.COMPLETED));
    stats.put("cancelled", defenseRepository.countByStatus(DefenseStatus.CANCELLED));
    return stats;
}
```

---

## 4. 👥 Workflow Rapporteurs Complet

### Description
Workflow complet de gestion des rapporteurs avec soumission de rapports et validation.

### Endpoints

#### Soumettre Rapport de Rapporteur
```http
POST /api/rapporteurs/{rapporteurId}/report
Authorization: Bearer {directeur_token}
Content-Type: application/json

{
  "report": "Texte du rapport détaillé...",
  "reportFilePath": "/documents/rapport.pdf",
  "opinion": "FAVORABLE" | "DEFAVORABLE" | "WITH_RESERVES"
}
```

#### Lister Rapporteurs d'une Soutenance
```http
GET /api/rapporteurs/defense/{defenseId}
Authorization: Bearer {token}
```

### Logique Métier

1. **Soumission de Rapport** : Le rapporteur soumet son avis
2. **Vérification Automatique** : Quand tous les rapporteurs ont soumis
3. **Validation Majoritaire** : Si majorité favorable (≥50%)
4. **Changement de Statut** : 
   - Favorable → `REPORTS_RECEIVED`
   - Défavorable → `CANCELLED`

### Repository

Méthodes activées dans `RapporteurRepository` :

```java
@Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId")
long countByDefenseId(@Param("defenseId") Long defenseId);

@Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
       "AND r.reportSubmissionDate IS NOT NULL")
long countSubmittedReportsByDefense(@Param("defenseId") Long defenseId);

@Query("SELECT COUNT(r) FROM Rapporteur r WHERE r.defense.id = :defenseId " +
       "AND r.opinion = 'FAVORABLE'")
long countFavorableOpinionsByDefense(@Param("defenseId") Long defenseId);
```

---

## 📝 Collection Postman Mise à Jour

### Nouveaux Dossiers

#### 2.5 Rapporteurs & Jury
- Submit Rapporteur Report (DIRECTEUR_THESE)
- Get Rapporteurs by Defense
- Get Defense Statistics (ADMIN)

#### 2.6 Gestion des Documents
- Upload Document (DOCTORANT)
- Get Documents by Entity
- Get My Documents
- Download Document
- Check Required Documents
- Delete Document

---

## 🚀 Instructions de Déploiement

### 1. Recompiler les Services

#### Defense Service
```bash
cd defense-service
.\mvnw.cmd clean package -DskipTests
```

#### Registration Service
```bash
cd registration-service
.\mvnw.cmd clean package -DskipTests
```

#### Notification Service
```bash
cd notification-service
.\mvnw.cmd clean package -DskipTests
```

### 2. Créer les Répertoires d'Upload

```bash
mkdir uploads\defense-service
mkdir uploads\registration-service
```

### 3. Redémarrer les Services

Redémarrer tous les services pour prendre en compte les nouvelles fonctionnalités.

### 4. Importer la Collection Postman

Re-importer `Postman-Collection-Complete.json` avec les nouveaux endpoints.

---

## ⚙️ Configuration Requise

### Base de Données
Les nouvelles tables `documents` seront créées automatiquement avec `ddl-auto: create-drop`.

### Kafka (Optionnel)
Le ReminderScheduler fonctionne indépendamment de Kafka pour les rappels.

### Stockage
Les fichiers sont stockés localement dans `./uploads/{service-name}`.

Pour une production avec stockage cloud (S3, Azure Blob), modifier `DocumentService` pour utiliser le SDK approprié.

---

## 🧪 Tests

### Tester l'Upload de Document

1. Créer une soutenance
2. Uploader un document :
```http
POST {{base_url}}/api/documents/upload
```

3. Vérifier les documents :
```http
GET {{base_url}}/api/documents/entity/{{defense_id}}
```

### Tester les Rappels

Les rappels s'exécutent automatiquement selon les crons configurés.

Pour tester manuellement, créer une soutenance avec date dans 3 jours et attendre le cron du matin.

### Tester les Statistiques

```http
GET {{base_url}}/api/defenses/statistics
Authorization: Bearer {{admin_token}}
```

---

## 📚 Documentation API Complète

Tous les endpoints sont documentés dans la collection Postman avec :
- Exemples de requêtes
- Scripts de tests automatiques
- Variables d'environnement
- Descriptions détaillées

---

## ✅ Résumé des Améliorations

| Fonctionnalité | Status | Service |
|---------------|--------|---------|
| Gestion Documents Defense | ✅ Complet | Defense Service |
| Gestion Documents Registration | ✅ Complet | Registration Service |
| Rappels Automatiques | ✅ Complet | Notification Service |
| Statistiques Defense | ✅ Complet | Defense Service |
| Workflow Rapporteurs | ✅ Complet | Defense Service |
| Collection Postman | ✅ Mise à jour | - |

---

## 🔮 Futures Améliorations Possibles

1. **Stockage Cloud** : Intégrer AWS S3 ou Azure Blob Storage
2. **OCR** : Extraction automatique de texte des documents PDF
3. **Validation Automatique** : Vérification automatique des documents requis
4. **Dashboard Admin** : Interface pour visualiser les statistiques
5. **Notifications Push** : Intégrer Firebase pour notifications mobiles
6. **Versioning Documents** : Historique des versions de documents
7. **Signature Électronique** : Signature numérique des rapports
8. **Export Excel** : Export des statistiques en format Excel

---

**Date de mise à jour** : 28 décembre 2025  
**Version** : 2.0
