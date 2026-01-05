# Interface Candidat - Guide de démarrage

## Composants créés pour l'interface Candidat

### 1. CandidatDashboardComponent
**Chemin**: `frontend-angular/src/app/features/candidat/candidat-dashboard/candidat-dashboard.component.ts`

**Fonctionnalités**:
- Affichage de 4 cartes de statistiques (candidatures totales, en attente, acceptées, documents)
- 6 cartes d'actions rapides (Mes Candidatures, Mes Soutenances, Mes Documents, Programmes, Avancement, Aide)
- Liste des candidatures récentes avec statuts
- Barre de progression du profil

### 2. MesCandidaturesComponent
**Chemin**: `frontend-angular/src/app/features/candidat/mes-candidatures/mes-candidatures.component.ts`

**Fonctionnalités**:
- Liste complète de toutes les candidatures du candidat
- Recherche par nom de programme
- Filtrage par statut (En attente, Acceptée, Rejetée, Annulée)
- Actions: Voir détails, Annuler candidature
- Bouton de création de nouvelle candidature

### 3. NouvelleCandidatureComponent
**Chemin**: `frontend-angular/src/app/features/candidat/nouvelle-candidature/nouvelle-candidature.component.ts`

**Fonctionnalités**:
- Formulaire en 4 étapes (Material Stepper):
  - **Étape 1**: Sélection du programme, domaine de recherche, année académique
  - **Étape 2**: Informations personnelles (formation, établissement, mention)
  - **Étape 3**: Motivation et projet de recherche
  - **Étape 4**: Récapitulatif et soumission
- Validation de formulaire à chaque étape
- Soumission vers le backend via RegistrationService

### 4. CandidatMesDocumentsComponent
**Chemin**: `frontend-angular/src/app/features/candidat/candidat-mes-documents/candidat-mes-documents.component.ts`

**Fonctionnalités**:
- 4 onglets de gestion de documents:
  - CV et Diplômes
  - Lettres de motivation
  - Relevés de notes
  - Autres documents
- Actions: Upload, Voir, Télécharger, Supprimer
- Affichage du statut de vérification (Vérifié, En attente, Rejeté)

## Routes configurées

Les routes suivantes ont été ajoutées dans `app.routes.ts`:

```typescript
{
  path: 'candidat',
  canActivate: [roleGuard([UserRole.CANDIDAT])],
  children: [
    { path: 'dashboard' },                  // Dashboard principal
    { path: 'mes-candidatures' },           // Liste des candidatures
    { path: 'nouvelle-candidature' },       // Créer nouvelle candidature
    { path: 'mes-documents' },              // Gestion documents
    { path: '', redirectTo: 'dashboard' }
  ]
}
```

## Backend - Endpoint ajouté

**Registration Service** - RegistrationController:
```java
@GetMapping("/candidate/{candidateId}")
public ResponseEntity<List<RegistrationResponse>> getRegistrationsByCandidate(@PathVariable Long candidateId)
```

Cet endpoint permet de récupérer toutes les candidatures d'un candidat spécifique.

## Démarrage de l'application

### Prérequis
- Java 17+
- Node.js 18+
- Docker (pour PostgreSQL, Kafka, etc.)
- Maven

### 1. Démarrer les services backend

#### Option A: Via Docker Compose
```powershell
cd "d:\project microservices\microservices-doctorat-app"
docker-compose up -d
```

#### Option B: Manuellement

**Eureka Discovery Server** (Port 8761):
```powershell
cd "d:\project microservices\microservices-doctorat-app\discovery-server"
./mvnw spring-boot:run
```

**Config Server** (Port 8888):
```powershell
cd "d:\project microservices\microservices-doctorat-app\config-server"
./mvnw spring-boot:run
```

**User Service** (Port 8081):
```powershell
cd "d:\project microservices\microservices-doctorat-app\user-service"
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

**Defense Service** (Port 8083):
```powershell
cd "d:\project microservices\microservices-doctorat-app\defense-service"
java -jar target/defense-service-0.0.1-SNAPSHOT.jar
```

**Registration Service** (Port 8084):
```powershell
cd "d:\project microservices\microservices-doctorat-app\registration-service"
java -jar target/registration-service-0.0.1-SNAPSHOT.jar
```

**API Gateway** (Port 8080):
```powershell
cd "d:\project microservices\microservices-doctorat-app\api-gateway"
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### 2. Démarrer l'application Angular

```powershell
cd "d:\project microservices\microservices-doctorat-app\frontend-angular"
npm start
```

L'application sera accessible sur: http://localhost:4200

## Test de l'interface Candidat

### 1. Connexion
- URL: http://localhost:4200/auth/login
- Utilisez un compte avec le rôle `CANDIDAT`
- Après connexion, vous serez redirigé vers `/candidat/dashboard`

### 2. Navigation
- **Dashboard**: Vue d'ensemble avec statistiques et actions rapides
- **Mes Candidatures**: Liste et gestion de vos candidatures
- **Nouvelle Candidature**: Formulaire en 4 étapes pour soumettre une candidature
- **Mes Documents**: Upload et gestion de documents

### 3. Créer une candidature
1. Cliquez sur "Nouvelle candidature" depuis le dashboard
2. Sélectionnez le programme et remplissez les informations
3. Complétez les 4 étapes du formulaire
4. Vérifiez le récapitulatif et soumettez

## Structure des données

### Registration (Candidature)
```typescript
interface Registration {
  id: number;
  candidateId: number;
  candidateName: string;
  programName: string;
  researchField: string;
  academicYear: string;
  status: RegistrationStatus;
  submittedAt: Date;
  // ... autres champs
}
```

### RegistrationStatus
```typescript
enum RegistrationStatus {
  PENDING = 'PENDING',      // En attente
  ACCEPTED = 'ACCEPTED',    // Acceptée
  REJECTED = 'REJECTED',    // Rejetée
  CANCELLED = 'CANCELLED'   // Annulée
}
```

## API Endpoints utilisés

### Registration Service (via API Gateway)
- `POST /api/registrations` - Créer une candidature
- `GET /api/registrations/candidate/{candidateId}` - Récupérer candidatures d'un candidat
- `GET /api/registrations/{id}` - Détails d'une candidature
- `GET /api/registrations/my` - Mes candidatures (via JWT)

### User Service (via API Gateway)
- `GET /api/users/directors` - Liste des directeurs (pour soutenances)
- `GET /api/users/{id}` - Détails d'un utilisateur

### Defense Service (via API Gateway)
- `GET /api/defenses` - Liste des soutenances
- `GET /api/defenses/doctorant/{doctorantId}` - Soutenances d'un doctorant
- `POST /api/defenses` - Créer une demande de soutenance

## Dépannage

### Problème: "Failed to load resource: 403 Forbidden"
**Solution**: Vérifiez que le token JWT contient le bon rôle `CANDIDAT` et que les endpoints ont les bonnes annotations de sécurité.

### Problème: "Cannot GET /api/registrations/candidate/{id}"
**Solution**: 
1. Vérifiez que le registration-service est démarré
2. Compilez le service: `./mvnw clean package -DskipTests`
3. Redémarrez le service

### Problème: "Navigation failed"
**Solution**: 
1. Vérifiez que toutes les routes sont bien définies dans `app.routes.ts`
2. Vérifiez que l'AuthService redirige bien vers `/candidat/dashboard` pour le rôle CANDIDAT
3. Vérifiez les guards de routes

### Problème: Pas de données affichées
**Solution**:
1. Ouvrez la console du navigateur (F12)
2. Vérifiez les erreurs dans l'onglet Network
3. Vérifiez que l'API Gateway route correctement vers le registration-service
4. Vérifiez les logs du backend

## Améliorations futures

### Fonctionnalités à ajouter:
1. **Upload de documents réel**: Intégration avec un service de stockage (S3, MinIO)
2. **Notifications**: Alertes pour changements de statut de candidature
3. **Messagerie**: Communication avec les directeurs de thèse
4. **Calendrier**: Suivi des dates limites et rendez-vous
5. **Export PDF**: Génération de documents PDF pour candidatures
6. **Multi-langue**: Support anglais/français

### Optimisations techniques:
1. **Pagination**: Ajouter pagination pour grandes listes de candidatures
2. **Cache**: Mise en cache des données statiques (programmes, etc.)
3. **Lazy loading**: Améliorer le chargement des images et documents
4. **PWA**: Transformer en Progressive Web App pour mode hors-ligne

## Support

Pour toute question ou problème:
- Consultez les logs du backend: `logs/` dans chaque service
- Vérifiez la console du navigateur (F12)
- Consultez la documentation de l'API Gateway

## Notes importantes

1. **Sécurité**: Tous les endpoints sont protégés par JWT et role-based access control
2. **Validation**: Les formulaires ont une validation côté client ET serveur
3. **Responsive**: Toutes les interfaces sont responsive (mobile, tablette, desktop)
4. **Material Design**: Utilisation cohérente de Angular Material
5. **État**: Gestion d'état via RxJS et BehaviorSubject

---

**Dernière mise à jour**: 29 décembre 2024
**Version**: 1.0.0
