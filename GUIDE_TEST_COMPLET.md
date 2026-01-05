# 🧪 GUIDE DE TEST COMPLET - DEFENSE SERVICE

## 🔧 PROBLÈME RÉSOLU

### ❌ Problème Initial
```
org.springframework.security.authorization.AuthorizationDeniedException: Access Denied
```

### ✅ Cause Identifiée
Le **HeaderAuthenticationFilter** ajoutait le préfixe `ROLE_` aux rôles, mais `@PreAuthorize("hasRole('ADMIN')")` ajoute **déjà** ce préfixe automatiquement.

**Résultat** : Double préfixe `ROLE_ROLE_ADMIN` → Access Denied

### ✅ Solution Appliquée
Suppression du préfixe `ROLE_` dans les 3 HeaderAuthenticationFilter :
- ✅ defense-service/config/HeaderAuthenticationFilter.java
- ✅ registration-service/config/HeaderAuthenticationFilter.java
- ✅ notification-service/config/HeaderAuthenticationFilter.java

**Avant** :
```java
SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
```

**Après** :
```java
// NE PAS ajouter le préfixe ROLE_ car hasRole() le fait automatiquement
SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
```

---

## 📋 CHECKLIST DE TEST

### ✅ Étape 1 : Authentification (2 min)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 1.1 | POST /auth/register | - | 201 Created + Token JWT |
| 1.2 | POST /auth/login | - | 200 OK + Token JWT |
| 1.3 | Créer ADMIN | - | Token auto-sauvegardé |
| 1.4 | Créer DOCTORANT | - | Token auto-sauvegardé |
| 1.5 | Créer CANDIDAT | - | Token auto-sauvegardé |

**Vérification** : Les tokens doivent être sauvegardés automatiquement dans les variables Postman.

---

### ✅ Étape 2 : User Service (ADMIN) (1 min)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 2.1 | GET /api/users | ADMIN | 200 OK + Liste des utilisateurs |
| 2.2 | GET /api/users/1 | ADMIN | 200 OK + Détails utilisateur |
| 2.3 | PUT /api/users/1 | ADMIN | 200 OK + Utilisateur modifié |
| 2.4 | GET /api/users | DOCTORANT | **403 Forbidden** ✅ |
| 2.5 | GET /api/users | CANDIDAT | **403 Forbidden** ✅ |

**Vérification** : Seul ADMIN peut accéder à /api/users.

---

### ✅ Étape 3 : Defense Service - Workflow Complet (10 min)

#### 3.1 Création de Demande (DOCTORANT)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.1.1 | POST /api/defenses | DOCTORANT | 201 Created + Defense ID |
| 3.1.2 | GET /api/defenses/my | DOCTORANT | 200 OK + Mes soutenances |
| 3.1.3 | GET /api/defenses/1 | DOCTORANT | 200 OK + Détails soutenance |
| 3.1.4 | POST /api/defenses | CANDIDAT | **403 Forbidden** ✅ |

**Body de test** :
```json
{
    "titre": "Intelligence Artificielle et Deep Learning",
    "resumeFr": "Cette thèse explore les techniques avancées de deep learning pour la reconnaissance d'images médicales",
    "resumeEn": "This thesis explores advanced deep learning techniques for medical image recognition",
    "directeurTheseId": 1,
    "codirecteurIds": [],
    "thesisStartDate": "2021-09-01",
    "nbPublications": 5,
    "nbConferences": 3,
    "nbTrainingHours": 300
}
```

#### 3.2 Validation Admin (ADMIN)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.2.1 | GET /api/defenses/admin/pending | ADMIN | 200 OK + Demandes en attente |
| 3.2.2 | POST /api/defenses/1/validate-prerequisites | ADMIN | 200 OK + Status PREREQUISITES_VALIDATED |
| 3.2.3 | POST /api/defenses/1/authorize | ADMIN | 200 OK + Status AUTHORIZED |
| 3.2.4 | POST /api/defenses/1/validate-prerequisites | DOCTORANT | **403 Forbidden** ✅ |

**Paramètres validate-prerequisites** :
- `approved=true`
- `comment=Tous les prérequis sont validés`

**Paramètres authorize** :
- `authorized=true`
- `defenseDate=2025-03-15T14:00:00`
- `location=Faculté des Sciences`
- `room=Amphithéâtre A`
- `comment=Soutenance autorisée`

#### 3.3 Gestion du Jury (DIRECTEUR_THESE)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.3.1 | POST /api/defenses/1/jury | DIRECTEUR_THESE | 201 Created |
| 3.3.2 | GET /api/defenses/1/jury/members | Tous | 200 OK + Liste membres jury |
| 3.3.3 | PUT /api/defenses/1/jury/validate | ADMIN | 200 OK |
| 3.3.4 | POST /api/defenses/1/jury | DOCTORANT | **403 Forbidden** ✅ |

**Body proposition jury** :
```json
{
    "members": [
        {
            "name": "Prof. Alami",
            "affiliation": "Université Mohammed V",
            "role": "PRESIDENT"
        },
        {
            "name": "Prof. Bennani",
            "affiliation": "Université Hassan II",
            "role": "EXAMINATEUR"
        },
        {
            "name": "Prof. Cherkaoui",
            "affiliation": "ENSIAS",
            "role": "RAPPORTEUR"
        }
    ]
}
```

#### 3.4 Rapports Rapporteurs (DIRECTEUR_THESE)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.4.1 | POST /api/rapporteurs/1/report | DIRECTEUR_THESE | 200 OK |
| 3.4.2 | GET /api/rapporteurs/defense/1 | Tous | 200 OK + Liste rapporteurs |
| 3.4.3 | POST /api/rapporteurs/1/report | DOCTORANT | **403 Forbidden** ✅ |

**Body rapport** :
```json
{
    "content": "Le candidat a démontré une excellente maîtrise du sujet. Les contributions scientifiques sont significatives et bien documentées. Je recommande l'autorisation de soutenance.",
    "recommendation": "APPROVED",
    "observations": "Quelques corrections mineures à apporter dans le chapitre 3"
}
```

#### 3.5 Résultat Final (ADMIN)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.5.1 | POST /api/defenses/1/result | ADMIN | 200 OK + Status COMPLETED |
| 3.5.2 | POST /api/defenses/1/result | DIRECTEUR_THESE | 200 OK ✅ |
| 3.5.3 | POST /api/defenses/1/result | DOCTORANT | **403 Forbidden** ✅ |

**Paramètres** :
- `result=ADMIS`
- `remarks=Excellente présentation`
- `mention=TRES_HONORABLE`

#### 3.6 Consultation et Statistiques

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 3.6.1 | GET /api/defenses/scheduled | Tous | 200 OK + Soutenances planifiées |
| 3.6.2 | GET /api/defenses/statistics | ADMIN | 200 OK + Statistiques |
| 3.6.3 | GET /api/defenses/director/pending | DIRECTEUR_THESE | 200 OK + Demandes |
| 3.6.4 | GET /api/defenses/statistics | DOCTORANT | **403 Forbidden** ✅ |

**Paramètres scheduled** :
- `start=2025-01-01T00:00:00`
- `end=2025-12-31T23:59:59`

---

### ✅ Étape 4 : Registration Service (5 min)

#### 4.1 Gestion Campagnes (ADMIN)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 4.1.1 | POST /api/registration/campaigns | ADMIN | 201 Created + Campaign ID |
| 4.1.2 | GET /api/registration/campaigns | Tous | 200 OK + Liste campagnes |
| 4.1.3 | GET /api/registration/campaigns/1 | Tous | 200 OK + Détails campagne |
| 4.1.4 | PUT /api/registration/campaigns/1 | ADMIN | 200 OK + Campagne modifiée |
| 4.1.5 | GET /api/registration/campaigns/active | Tous | 200 OK + Campagnes actives |
| 4.1.6 | POST /api/registration/campaigns | DOCTORANT | **403 Forbidden** ✅ |
| 4.1.7 | PUT /api/registration/campaigns/1 | CANDIDAT | **403 Forbidden** ✅ |

**Body création campagne** :
```json
{
    "name": "Campagne Doctorat 2025-2026",
    "description": "Inscription pour le cycle doctoral 2025-2026. Spécialités: Informatique, Mathématiques, Physique",
    "startDate": "2025-01-15T00:00:00",
    "endDate": "2025-06-30T23:59:59",
    "maxCandidates": 100,
    "type": "DOCTORAT"
}
```

---

### ✅ Étape 5 : Notification Service (3 min)

#### 5.1 Envoi Notifications (ADMIN/ADMINISTRATIF)

| Test | Endpoint | Rôle | Résultat Attendu |
|------|----------|------|------------------|
| 5.1.1 | POST /api/notifications | ADMIN | 201 Created |
| 5.1.2 | GET /api/notifications/user/1 | Tous | 200 OK + Mes notifications |
| 5.1.3 | PUT /api/notifications/1/read | Tous | 200 OK |
| 5.1.4 | POST /api/notifications | DOCTORANT | **403 Forbidden** ✅ |
| 5.1.5 | POST /api/notifications | CANDIDAT | **403 Forbidden** ✅ |

**Body notification** :
```json
{
    "userId": 1,
    "title": "Validation de votre demande",
    "message": "Votre demande de soutenance a été validée avec succès. La date sera communiquée prochainement.",
    "type": "INFO",
    "channel": "EMAIL"
}
```

---

### ✅ Étape 6 : Health Checks (1 min)

| Test | Endpoint | Résultat Attendu |
|------|----------|------------------|
| 6.1 | GET http://localhost:8080/actuator/health | UP |
| 6.2 | GET http://localhost:8081/actuator/health | UP |
| 6.3 | GET http://localhost:8082/actuator/health | UP |
| 6.4 | GET http://localhost:8083/actuator/health | UP |
| 6.5 | GET http://localhost:8084/actuator/health | UP |
| 6.6 | GET http://localhost:8761 | Eureka Dashboard |

---

## 🎯 RÉSULTATS ATTENDUS PAR RÔLE

### 🔑 ADMIN (Accès Complet)
- ✅ Tous les endpoints User Service
- ✅ Validation prérequis
- ✅ Autorisation soutenance
- ✅ Validation jury
- ✅ Enregistrement résultat
- ✅ Statistiques
- ✅ Création/modification campagnes
- ✅ Envoi notifications

### 👨‍🏫 DIRECTEUR_THESE
- ❌ User Service (403)
- ✅ Consultation soutenances
- ✅ Proposition jury
- ✅ Soumission rapport rapporteur
- ✅ Enregistrement résultat (autorisé)
- ❌ Création campagnes (403)
- ✅ Consultation notifications

### 🎓 DOCTORANT
- ❌ User Service (403)
- ✅ Création demande soutenance
- ✅ Consultation mes soutenances
- ❌ Validation prérequis (403)
- ❌ Autorisation soutenance (403)
- ❌ Proposition jury (403)
- ✅ Consultation campagnes
- ✅ Consultation notifications

### 🆕 CANDIDAT
- ❌ User Service (403)
- ❌ Création demande soutenance (403)
- ✅ Consultation campagnes actives
- ✅ Consultation notifications
- ❌ Envoi notifications (403)

### 📝 ADMINISTRATIF
- ❌ User Service (403)
- ❌ Gestion soutenances (403)
- ✅ Consultation campagnes
- ✅ Envoi notifications
- ✅ Consultation notifications

---

## 🚀 ORDRE DE TEST RECOMMANDÉ

### Phase 1 : Setup (2 min)
1. Importer Postman-Collection-Complete.json
2. Importer Postman-Environment-Local.json
3. Sélectionner environnement "Doctorat App - Local"
4. Vérifier Health Checks

### Phase 2 : Authentification (2 min)
1. Register ADMIN → Token auto-sauvegardé
2. Register DOCTORANT → Token auto-sauvegardé  
3. Register CANDIDAT → Token auto-sauvegardé
4. Login ADMIN → Vérifier token

### Phase 3 : Tests Basiques (3 min)
1. GET /api/users (ADMIN) → 200 OK ✅
2. GET /api/users (DOCTORANT) → 403 Forbidden ✅
3. GET /api/registration/campaigns (Tous) → 200 OK ✅

### Phase 4 : Workflow Soutenance Complet (10 min)
1. Créer demande (DOCTORANT)
2. Valider prérequis (ADMIN)
3. Autoriser soutenance (ADMIN)
4. Proposer jury (DIRECTEUR_THESE ou ADMIN)
5. Valider jury (ADMIN)
6. Soumettre rapport (DIRECTEUR_THESE)
7. Enregistrer résultat (ADMIN)
8. Consulter statistiques (ADMIN)

### Phase 5 : Tests Négatifs (5 min)
Vérifier tous les 403 Forbidden pour chaque rôle

### Phase 6 : Tests Avancés (5 min)
1. Campagnes multiples
2. Notifications multiples
3. Soutenances planifiées
4. Rapporteurs multiples

---

## 📊 CRITÈRES DE SUCCÈS

### ✅ Authentification
- [x] Register retourne 201 + Token JWT
- [x] Login retourne 200 + Token JWT
- [x] Token auto-sauvegardé dans variables

### ✅ Sécurité RBAC
- [x] ADMIN accède à tous les endpoints
- [x] DOCTORANT créé des soutenances
- [x] DIRECTEUR_THESE propose des jurys
- [x] CANDIDAT limité aux campagnes
- [x] 403 Forbidden sur accès interdits

### ✅ Workflow Soutenance
- [x] Création → SUBMITTED
- [x] Validation prérequis → PREREQUISITES_VALIDATED
- [x] Autorisation → AUTHORIZED
- [x] Résultat → COMPLETED
- [x] Statistiques accessibles

### ✅ Services Techniques
- [x] Tous les ports actifs (8080-8084, 8761)
- [x] Tous les services enregistrés dans Eureka
- [x] Health checks retournent UP
- [x] Aucune erreur 500 Internal Server Error

---

## 🐛 DÉBOGAGE

### Problème : 401 Unauthorized
**Cause** : Token invalide ou expiré  
**Solution** : Refaire Register ADMIN

### Problème : 403 Forbidden
**Cause** : Rôle insuffisant (NORMAL si testé avec bon rôle)  
**Solution** : Utiliser le bon token (ADMIN, DOCTORANT, etc.)

### Problème : 404 Not Found
**Cause** : Endpoint inexistant ou ID invalide  
**Solution** : Vérifier l'URL et l'ID

### Problème : 500 Internal Server Error
**Cause** : Erreur serveur  
**Solution** : Vérifier les logs du service concerné

### Problème : 503 Service Unavailable
**Cause** : Service non démarré ou non enregistré dans Eureka  
**Solution** : Redémarrer le service, vérifier Eureka

---

## 🎓 TEMPS TOTAL ESTIMÉ

- Setup : 2 minutes
- Authentification : 2 minutes
- Tests basiques : 3 minutes
- Workflow complet : 10 minutes
- Tests négatifs : 5 minutes
- Tests avancés : 5 minutes

**TOTAL : ~30 minutes pour tester TOUS les endpoints**

---

## ✅ VALIDATION FINALE

Une fois tous les tests passés :

1. **Export des résultats Postman** (optionnel)
2. **Vérification Eureka Dashboard** : Tous les services enregistrés
3. **Vérification logs** : Aucune erreur critique
4. **Documentation** : Tous les endpoints testés et validés

**🎉 L'application est prête pour la production !**
