# Documentation des APIs - Application Doctorat

## 📋 Table des Matières

1. [Authentification](#authentification)
2. [Gestion des Utilisateurs](#gestion-des-utilisateurs)
3. [Inscriptions](#inscriptions)
4. [Défenses](#défenses)
5. [Notifications](#notifications)
6. [Exemples Complets](#exemples-complets)

---

## 🔐 Authentification

Base URL : `http://localhost:8080/auth`

### Inscription

**POST** `/auth/register`

Créer un nouveau compte utilisateur.

**Headers** :
```
Content-Type: application/json
```

**Body** :
```json
{
  "username": "jean.dupont@example.com",
  "password": "Password123!",
  "email": "jean.dupont@example.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "phone": "+212600000000",
  "role": "DOCTORANT",
  "studentId": "CNE12345678",
  "laboratoire": null,
  "grade": null
}
```

**Réponse (201 Created)** :
```json
{
  "id": 1,
  "username": "jean.dupont@example.com",
  "email": "jean.dupont@example.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "role": "DOCTORANT",
  "enabled": true,
  "createdAt": "2025-12-25T10:00:00"
}
```

### Connexion

**POST** `/auth/login`

Se connecter et obtenir un token JWT.

**Headers** :
```
Content-Type: application/json
```

**Body** :
```json
{
  "username": "jean.dupont@example.com",
  "password": "Password123!"
}
```

**Réponse (200 OK)** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "jean.dupont@example.com",
    "email": "jean.dupont@example.com",
    "firstName": "Jean",
    "lastName": "Dupont",
    "role": "DOCTORANT"
  }
}
```

---

## 👥 Gestion des Utilisateurs

Base URL : `http://localhost:8080/users`

**Note** : Tous les endpoints nécessitent un token JWT dans le header `Authorization: Bearer <token>`

### Récupérer un utilisateur par ID

**GET** `/users/{id}`

**Headers** :
```
Authorization: Bearer <token>
```

**Réponse (200 OK)** :
```json
{
  "id": 1,
  "username": "jean.dupont@example.com",
  "email": "jean.dupont@example.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "phone": "+212600000000",
  "role": "DOCTORANT",
  "enabled": true,
  "studentId": "CNE12345678",
  "createdAt": "2025-12-25T10:00:00"
}
```

### Récupérer tous les utilisateurs

**GET** `/users`

**Rôle requis** : `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
```

**Paramètres de requête** :
- `page` (optionnel) : Numéro de page (défaut: 0)
- `size` (optionnel) : Taille de page (défaut: 20)
- `role` (optionnel) : Filtrer par rôle

**Exemple** :
```
GET /users?page=0&size=10&role=DOCTORANT
```

**Réponse (200 OK)** :
```json
{
  "content": [
    {
      "id": 1,
      "username": "jean.dupont@example.com",
      "firstName": "Jean",
      "lastName": "Dupont",
      "role": "DOCTORANT"
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

### Mettre à jour un utilisateur

**PUT** `/users/{id}`

**Rôle requis** : `ADMIN` ou l'utilisateur lui-même

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "firstName": "Jean-Pierre",
  "lastName": "Dupont",
  "phone": "+212611111111",
  "laboratoire": "Lab IA"
}
```

---

## 📝 Inscriptions

Base URL : `http://localhost:8080/registration`

### Créer une campagne d'inscription

**POST** `/registration/campaigns`

**Rôle requis** : `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "name": "Campagne Doctorat 2025-2026",
  "description": "Campagne d'inscription pour l'année universitaire 2025-2026",
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-03-31T23:59:59",
  "maxCandidates": 100,
  "status": "OPEN"
}
```

**Réponse (201 Created)** :
```json
{
  "id": 1,
  "name": "Campagne Doctorat 2025-2026",
  "description": "Campagne d'inscription pour l'année universitaire 2025-2026",
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-03-31T23:59:59",
  "maxCandidates": 100,
  "currentCandidates": 0,
  "status": "OPEN",
  "createdAt": "2025-12-25T10:00:00"
}
```

### Soumettre une candidature

**POST** `/registration/apply`

**Rôle requis** : `CANDIDAT`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "campaignId": 1,
  "sujetThese": "Intelligence Artificielle et Santé",
  "domaineRecherche": "IA",
  "directeurThese": "Dr. Ahmed ALAMI",
  "laboratoire": "Lab IA",
  "motivations": "Je suis passionné par l'IA...",
  "cvUrl": "https://storage.example.com/cv/jean_dupont.pdf",
  "diplomes": [
    {
      "type": "MASTER",
      "domaine": "Informatique",
      "etablissement": "FST Fès",
      "anneeObtention": 2024,
      "mention": "Très Bien"
    }
  ]
}
```

**Réponse (201 Created)** :
```json
{
  "id": 1,
  "campaignId": 1,
  "candidatId": 1,
  "candidatName": "Jean Dupont",
  "sujetThese": "Intelligence Artificielle et Santé",
  "status": "SUBMITTED",
  "submittedAt": "2025-12-25T10:00:00"
}
```

### Valider une candidature

**POST** `/registration/{id}/validate`

**Rôle requis** : `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "approved": true,
  "comment": "Dossier complet et excellent profil"
}
```

---

## 🎓 Défenses

Base URL : `http://localhost:8080/defense`

### Soumettre une demande de soutenance

**POST** `/defense/create`

**Rôle requis** : `DOCTORANT`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "titre": "Apprentissage profond pour le diagnostic médical",
  "resume": "Cette thèse explore l'utilisation du deep learning...",
  "directeurTheseId": 5,
  "coDirecteurTheseId": 6,
  "dateProposee": "2026-06-15T14:00:00",
  "lieu": "Amphithéâtre A, FST Fès",
  "specialite": "Informatique",
  "laboratoire": "Lab IA",
  "fichierThese": "https://storage.example.com/theses/dupont_2026.pdf",
  "prerequis": {
    "publicationsRequises": 3,
    "publicationsSoumises": 3,
    "attestationDirecteur": true,
    "rapportActivite": true
  }
}
```

**Réponse (201 Created)** :
```json
{
  "id": 1,
  "doctorantId": 1,
  "doctorantName": "Jean Dupont",
  "titre": "Apprentissage profond pour le diagnostic médical",
  "status": "PENDING_PREREQUISITES",
  "dateProposee": "2026-06-15T14:00:00",
  "createdAt": "2025-12-25T10:00:00"
}
```

### Mes soutenances

**GET** `/defense/my`

**Rôle requis** : `DOCTORANT`

**Headers** :
```
Authorization: Bearer <token>
```

**Paramètres de requête** :
- `page` (optionnel)
- `size` (optionnel)

**Réponse (200 OK)** :
```json
{
  "content": [
    {
      "id": 1,
      "titre": "Apprentissage profond pour le diagnostic médical",
      "status": "PENDING_PREREQUISITES",
      "dateProposee": "2026-06-15T14:00:00",
      "createdAt": "2025-12-25T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### Valider les prérequis

**POST** `/defense/{id}/validate-prerequisites`

**Rôle requis** : `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
```

**Paramètres de requête** :
- `approved` : true/false
- `comment` (optionnel) : Commentaire

**Exemple** :
```
POST /defense/1/validate-prerequisites?approved=true&comment=Tous les prérequis sont remplis
```

### Autoriser la soutenance

**POST** `/defense/{id}/authorize`

**Rôle requis** : `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "dateFinale": "2026-06-15T14:00:00",
  "lieu": "Amphithéâtre A",
  "autorisationNumber": "AUT-2026-001"
}
```

### Composer un jury

**POST** `/defense/{id}/jury`

**Rôle requis** : `DIRECTEUR_THESE` ou `ADMIN`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "president": {
    "nom": "Prof. Mohammed BENNANI",
    "grade": "Professeur",
    "etablissement": "FST Fès",
    "email": "bennani@fst.ac.ma"
  },
  "rapporteurs": [
    {
      "nom": "Prof. Fatima ZAHRA",
      "grade": "Professeur",
      "etablissement": "ENSIAS Rabat",
      "email": "zahra@ensias.ac.ma"
    }
  ],
  "examinateurs": [
    {
      "nom": "Dr. Hassan ALAMI",
      "grade": "Maître de conférences",
      "etablissement": "FST Fès",
      "email": "alami@fst.ac.ma"
    }
  ]
}
```

---

## 📬 Notifications

Base URL : `http://localhost:8080/notification`

### Envoyer une notification

**POST** `/notification/send`

**Rôle requis** : `ADMIN` ou `ADMINISTRATIF`

**Headers** :
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body** :
```json
{
  "recipientIds": [1, 2, 3],
  "type": "EMAIL",
  "subject": "Rappel : Date limite d'inscription",
  "message": "La date limite d'inscription est le 31 mars 2025.",
  "priority": "HIGH"
}
```

**Réponse (201 Created)** :
```json
{
  "id": 1,
  "recipientCount": 3,
  "type": "EMAIL",
  "status": "SENT",
  "sentAt": "2025-12-25T10:00:00"
}
```

### Mes notifications

**GET** `/notification/my`

**Headers** :
```
Authorization: Bearer <token>
```

**Paramètres de requête** :
- `unreadOnly` (optionnel) : true pour voir uniquement les non lues
- `page` (optionnel)
- `size` (optionnel)

**Réponse (200 OK)** :
```json
{
  "content": [
    {
      "id": 1,
      "subject": "Rappel : Date limite d'inscription",
      "message": "La date limite d'inscription est le 31 mars 2025.",
      "type": "EMAIL",
      "read": false,
      "sentAt": "2025-12-25T10:00:00"
    }
  ],
  "totalElements": 5,
  "unreadCount": 2
}
```

### Marquer comme lue

**PUT** `/notification/{id}/read`

**Headers** :
```
Authorization: Bearer <token>
```

---

## 📘 Exemples Complets

### Scénario 1 : Inscription d'un Doctorant

```bash
# 1. Créer un compte
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jean.dupont@example.com",
    "password": "Password123!",
    "email": "jean.dupont@example.com",
    "firstName": "Jean",
    "lastName": "Dupont",
    "role": "CANDIDAT"
  }'

# 2. Se connecter
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jean.dupont@example.com",
    "password": "Password123!"
  }' | jq -r '.token')

# 3. Voir les campagnes ouvertes
curl -X GET http://localhost:8080/registration/campaigns/open \
  -H "Authorization: Bearer $TOKEN"

# 4. Soumettre une candidature
curl -X POST http://localhost:8080/registration/apply \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": 1,
    "sujetThese": "IA et Santé",
    "domaineRecherche": "IA",
    "directeurThese": "Dr. ALAMI"
  }'
```

### Scénario 2 : Demande de Soutenance

```bash
# 1. Se connecter en tant que doctorant
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant@example.com",
    "password": "Password123!"
  }' | jq -r '.token')

# 2. Soumettre une demande de soutenance
DEFENSE_ID=$(curl -X POST http://localhost:8080/defense/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Ma thèse",
    "resume": "Résumé...",
    "directeurTheseId": 5,
    "dateProposee": "2026-06-15T14:00:00"
  }' | jq -r '.id')

# 3. Vérifier le statut
curl -X GET http://localhost:8080/defense/$DEFENSE_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Scénario 3 : Validation Administrateur

```bash
# 1. Se connecter en tant qu'admin
ADMIN_TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "AdminPass123!"
  }' | jq -r '.token')

# 2. Voir les demandes en attente
curl -X GET http://localhost:8080/defense/admin/pending \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Valider les prérequis
curl -X POST "http://localhost:8080/defense/1/validate-prerequisites?approved=true" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. Autoriser la soutenance
curl -X POST http://localhost:8080/defense/1/authorize \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateFinale": "2026-06-15T14:00:00",
    "lieu": "Amphithéâtre A",
    "autorisationNumber": "AUT-2026-001"
  }'
```

---

## 🔧 Codes de Statut HTTP

| Code | Signification |
|------|---------------|
| 200 | OK - Requête réussie |
| 201 | Created - Ressource créée |
| 204 | No Content - Opération réussie sans contenu |
| 400 | Bad Request - Requête invalide |
| 401 | Unauthorized - Non authentifié |
| 403 | Forbidden - Non autorisé (mauvais rôle) |
| 404 | Not Found - Ressource non trouvée |
| 409 | Conflict - Conflit (ex: email déjà utilisé) |
| 500 | Internal Server Error - Erreur serveur |

---

## 📝 Notes

1. **Tous les datetimes** sont au format ISO 8601 : `YYYY-MM-DDTHH:mm:ss`
2. **La pagination** utilise les paramètres `page` (0-indexed) et `size`
3. **Les tokens JWT** expirent après 24 heures
4. **CORS** est activé pour `http://localhost:4200` (frontend Angular)

---

## 🛠️ Collection Postman

Une collection Postman complète est disponible avec tous les endpoints pré-configurés.

Importer le fichier : `postman/Doctorat-API.postman_collection.json`
