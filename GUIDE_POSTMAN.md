# GUIDE D'UTILISATION POSTMAN - APPLICATION DOCTORAT

## 📥 IMPORT DE LA COLLECTION

1. Ouvrez Postman
2. Cliquez sur **Import** (en haut à gauche)
3. Sélectionnez le fichier `Doctorat-App-Postman-Collection.json`
4. La collection "Doctorat Application - Tests Complets" apparaît dans votre sidebar

## 🚀 DÉMARRAGE DES SERVICES

Avant de tester, assurez-vous que tous les services sont démarrés dans cet ordre :

### 1. Démarrer Eureka Discovery Server
```powershell
cd d:\project microservices\microservices-doctorat-app\discovery-server
.\mvnw spring-boot:run
```
**Attendre 30 secondes**
**URL**: http://localhost:8761

### 2. Démarrer User-Service
```powershell
cd d:\project microservices\microservices-doctorat-app\user-service
.\mvnw spring-boot:run
```
**Attendre 40 secondes**
**Port**: 8081

### 3. Démarrer API Gateway
```powershell
cd d:\project microservices\microservices-doctorat-app\api-gateway
.\mvnw spring-boot:run
```
**Attendre 45 secondes**
**Port**: 8080

### 4. (Optionnel) Démarrer Defense Service
```powershell
cd d:\project microservices\microservices-doctorat-app\defense-service
.\mvnw spring-boot:run
```
**Port**: 8083

### 5. (Optionnel) Démarrer Registration Service
```powershell
cd d:\project microservices\microservices-doctorat-app\registration-service
.\mvnw spring-boot:run
```
**Port**: 8082

### 6. (Optionnel) Démarrer Notification Service
```powershell
cd d:\project microservices\microservices-doctorat-app\notification-service
.\mvnw spring-boot:run
```
**Port**: 8084

## ✅ VÉRIFICATION DE L'INFRASTRUCTURE

### Test 1: Health Check API Gateway
```http
GET http://localhost:8080/actuator/health
```
**Résultat attendu**: `200 OK` avec `{"status":"UP"}`

### Test 2: Health Check Eureka
```http
GET http://localhost:8761/actuator/health
```
**Résultat attendu**: `200 OK`

### Test 3: Services Enregistrés
```http
GET http://localhost:8761/eureka/apps
```
**Résultat attendu**: XML listant tous les services enregistrés

## 🔐 TESTS D'AUTHENTIFICATION

### Workflow Recommandé

#### 1. **Inscription Admin** (Folder: 2. Authentification)
- Requête: `POST /auth/register`
- Body:
```json
{
    "username": "admin_test",
    "password": "Admin123!",
    "email": "admin@doctorat.ma",
    "role": "ADMIN"
}
```
- **Résultat**: Token JWT sauvegardé automatiquement dans `{{jwt_token}}`
- **Note**: Après succès, le token est utilisé automatiquement pour toutes les requêtes suivantes

#### 2. **Connexion** (Folder: 2. Authentification)
Si l'utilisateur existe déjà:
- Requête: `POST /auth/login`
- Body:
```json
{
    "username": "admin_test",
    "password": "Admin123!"
}
```

#### 3. **Inscription Doctorant** (Optionnel)
```json
{
    "username": "doctorant_test",
    "password": "Doctorant123!",
    "email": "doctorant@doctorat.ma",
    "role": "DOCTORANT",
    "firstName": "Ahmed",
    "lastName": "Bennani",
    "studentId": "DOC2025001"
}
```

#### 4. **Inscription Directeur de Thèse** (Optionnel)
```json
{
    "username": "directeur_test",
    "password": "Directeur123!",
    "email": "directeur@doctorat.ma",
    "role": "DIRECTEUR_THESE",
    "firstName": "Mohammed",
    "lastName": "Alaoui",
    "laboratoire": "LaboIA",
    "grade": "Professeur"
}
```

## 👥 TESTS USER SERVICE

**Important**: Ces endpoints nécessitent un token JWT valide

### 1. Liste tous les utilisateurs
```http
GET http://localhost:8080/api/users
Authorization: Bearer {{jwt_token}}
```
**Résultat**: Array de tous les utilisateurs

### 2. Obtenir un utilisateur par ID
```http
GET http://localhost:8080/api/users/{{user_id}}
Authorization: Bearer {{jwt_token}}
```

### 3. Mettre à jour un utilisateur
```http
PUT http://localhost:8080/api/users/{{user_id}}
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "firstName": "Ahmed",
    "lastName": "Updated",
    "phone": "+212612345678"
}
```

## 🎓 TESTS DEFENSE SERVICE

### 1. Créer une demande de soutenance
```http
POST http://localhost:8080/api/defense/requests
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "title": "Intelligence Artificielle et Big Data",
    "description": "Thèse sur l'application de l'IA dans le Big Data",
    "proposedDate": "2025-06-15T10:00:00",
    "location": "Amphithéâtre A",
    "juryMembers": [
        {
            "name": "Prof. Hassan",
            "email": "hassan@univ.ma",
            "role": "Président"
        }
    ]
}
```

### 2. Liste des soutenances
```http
GET http://localhost:8080/api/defense/requests
Authorization: Bearer {{jwt_token}}
```

### 3. Approuver une soutenance
```http
PUT http://localhost:8080/api/defense/requests/1/status
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "status": "APPROVED",
    "comments": "Demande approuvée"
}
```

## 📝 TESTS REGISTRATION SERVICE

### 1. Créer une campagne
```http
POST http://localhost:8080/api/registration/campaigns
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "name": "Campagne Doctorat 2025-2026",
    "description": "Inscription au doctorat pour l'année universitaire 2025-2026",
    "startDate": "2025-01-01",
    "endDate": "2025-03-31",
    "maxApplications": 100
}
```

### 2. Liste des campagnes
```http
GET http://localhost:8080/api/registration/campaigns
Authorization: Bearer {{jwt_token}}
```

### 3. Soumettre une candidature
```http
POST http://localhost:8080/api/registration/applications
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "campaignId": 1,
    "researchProposal": "Ma proposition de recherche porte sur...",
    "motivationLetter": "Je souhaite intégrer ce programme car...",
    "academicBackground": "Master en Informatique avec mention Très Bien"
}
```

## 🔔 TESTS NOTIFICATION SERVICE

### 1. Envoyer une notification
```http
POST http://localhost:8080/api/notification/send
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
    "title": "Nouvelle campagne disponible",
    "message": "Une nouvelle campagne d'inscription est ouverte",
    "type": "INFO",
    "recipientIds": [],
    "sendToAll": true
}
```

### 2. Liste des notifications
```http
GET http://localhost:8080/api/notification/my-notifications
Authorization: Bearer {{jwt_token}}
```

### 3. Marquer comme lu
```http
PUT http://localhost:8080/api/notification/1/read
Authorization: Bearer {{jwt_token}}
```

## 🔒 TESTS DE SÉCURITÉ

### Test 1: Accès sans token (doit échouer 401)
```http
GET http://localhost:8080/api/users
```
**Résultat attendu**: `401 Unauthorized`

### Test 2: Accès avec token invalide (doit échouer 401)
```http
GET http://localhost:8080/api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token
```
**Résultat attendu**: `401 Unauthorized`

## ⚙️ VARIABLES DE COLLECTION

La collection utilise des variables pour faciliter les tests :

- **base_url**: `http://localhost:8080` (API Gateway)
- **jwt_token**: Stocke automatiquement le token JWT après inscription/connexion
- **user_id**: Stocke l'ID de l'utilisateur connecté

### Modifier les variables
1. Cliquez sur la collection dans Postman
2. Onglet **Variables**
3. Modifiez **Current Value** si nécessaire

## 🎯 ORDRE DE TEST RECOMMANDÉ

1. **Infrastructure** (Folder 1)
   - Health Check Gateway
   - Health Check Eureka
   - Services Enregistrés

2. **Authentification** (Folder 2)
   - Inscription Admin → **Sauvegarde automatique du token**
   - Connexion Admin
   - Inscription Doctorant
   - Inscription Directeur

3. **User Service** (Folder 3)
   - Liste utilisateurs
   - Obtenir utilisateur par ID
   - Mettre à jour utilisateur

4. **Defense Service** (Folder 4)
   - Créer demande soutenance
   - Liste soutenances
   - Approuver soutenance

5. **Registration Service** (Folder 5)
   - Créer campagne
   - Liste campagnes
   - Soumettre candidature
   - Liste candidatures

6. **Notification Service** (Folder 6)
   - Envoyer notification
   - Liste notifications
   - Marquer comme lu

7. **Sécurité** (Folder 7)
   - Accès sans token
   - Accès token invalide

## ❗ RÉSOLUTION DES PROBLÈMES

### Erreur 401 Unauthorized
- **Cause**: Token JWT expiré ou invalide
- **Solution**: Refaire une inscription ou connexion (Folder 2)

### Erreur 404 Not Found
- **Cause**: Service non enregistré dans Eureka ou route incorrecte
- **Solution**: 
  1. Vérifier que tous les services sont démarrés
  2. Attendre 30-60 secondes pour l'enregistrement Eureka
  3. Vérifier http://localhost:8761 pour voir les services actifs

### Erreur 503 Service Unavailable
- **Cause**: Service backend non disponible
- **Solution**: Démarrer le service manquant (defense/registration/notification)

### Erreur 400 Bad Request
- **Cause**: Body de la requête invalide
- **Solution**: Vérifier le format JSON et les champs obligatoires

### Erreur 403 Forbidden
- **Cause**: Rôle insuffisant pour l'opération
- **Solution**: Se connecter avec un compte ADMIN

## 📊 CODES DE STATUT

- **200 OK**: Requête réussie
- **201 Created**: Ressource créée avec succès
- **400 Bad Request**: Données invalides
- **401 Unauthorized**: Authentification requise ou token invalide
- **403 Forbidden**: Accès refusé (rôle insuffisant)
- **404 Not Found**: Ressource ou route inexistante
- **503 Service Unavailable**: Service backend indisponible

## 💡 CONSEILS

1. **Toujours commencer par les tests d'infrastructure** pour vérifier que tout fonctionne
2. **Utiliser les scripts de test automatiques** : Les requêtes d'authentification sauvegardent automatiquement le token
3. **Vérifier les variables** : Après l'inscription, vérifiez que `{{jwt_token}}` et `{{user_id}}` sont remplis
4. **Utiliser l'ordre recommandé** : Certains tests dépendent des précédents (ex: mettre à jour un utilisateur nécessite son ID)
5. **Consulter la console Postman** : Les scripts de test affichent des logs utiles

## 📞 SUPPORT

Pour tout problème technique:
- Consulter [RAPPORT_FINAL_TESTS.md](RAPPORT_FINAL_TESTS.md)
- Vérifier les logs des services dans les fenêtres PowerShell
- Examiner les erreurs dans Postman Console (View → Show Postman Console)
