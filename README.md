<<<<<<< HEAD
# 🎓 Application de Gestion Doctorale - Microservices

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Angular](https://img.shields.io/badge/Angular-18-red.svg)](https://angular.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

Application microservices complète pour la gestion du cycle de vie d'un doctorant, de l'inscription à la soutenance de thèse. L'application utilise une **architecture de sécurité centralisée** avec JWT pour garantir une authentification et une autorisation cohérentes.

### ✨ Fonctionnalités Principales

- 🔐 **Authentification centralisée** avec JWT
- 👥 **Gestion des utilisateurs** et des rôles
- 📝 **Inscriptions** au doctorat avec campagnes
- 🎯 **Gestion des soutenances** de thèse
- 👨‍🏫 **Composition des jurys**
- 📧 **Notifications** automatiques
- 📊 **Monitoring** avec Actuator

---

## 🏗️ Architecture

L'application est composée de plusieurs microservices communiquant via l'API Gateway :

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend Angular                         │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   API Gateway (8080)                         │
│  • Authentification JWT centralisée                          │
│  • Routage vers les microservices                            │
│  • Gestion des rôles et permissions                          │
└──────┬────────┬────────┬─────────┬────────────────────────┘
       │        │        │         │
       ▼        ▼        ▼         ▼
   ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
   │User  │ │Def.  │ │Reg.  │ │Notif.│
   │8081  │ │8083  │ │8082  │ │8084  │
   └──────┘ └──────┘ └──────┘ └──────┘
```

### Microservices

| Service | Port | Description |
|---------|------|-------------|
| **Discovery Server** | 8761 | Eureka - Service Discovery |
| **Config Server** | 8888 | Configuration centralisée |
| **API Gateway** | 8080 | Point d'entrée unique + Sécurité |
| **User Service** | 8081 | Gestion des utilisateurs |
| **Registration Service** | 8082 | Inscriptions au doctorat |
| **Defense Service** | 8083 | Gestion des soutenances |
| **Notification Service** | 8084 | Notifications et alertes |
| **Frontend Angular** | 4200 | Interface utilisateur |

---

## 🔐 Sécurité

L'application utilise une **architecture de sécurité centralisée** dans l'API Gateway avec JWT.

### Rôles Disponibles

| Rôle | Description | Accès Principal |
|------|-------------|-----------------|
| **CANDIDAT** | Candidat au doctorat | Inscription |
| **DOCTORANT** | Doctorant inscrit | Soumettre soutenance |
| **DIRECTEUR_THESE** | Directeur de thèse | Valider travaux, composer jury |
| **ADMINISTRATIF** | Personnel administratif | Notifications |
| **ADMIN** | Administrateur | Tous les accès |

### Flux d'Authentification

1. L'utilisateur se connecte via `/auth/login`
2. Le User Service génère un JWT Token
3. Le client envoie le token dans le header `Authorization: Bearer <token>`
4. L'API Gateway valide le token et extrait les informations
5. L'API Gateway ajoute les headers `X-User-Id`, `X-User-Username`, `X-User-Role`
6. Les microservices utilisent ces headers pour la logique métier

📖 **Documentation complète** : [SECURITY.md](./SECURITY.md)

---

## 🚀 Installation et Démarrage

### Prérequis

- Java 17+
- Maven 3.8+
- Node.js 18+ (pour le frontend)
- PostgreSQL 14+ (ou H2 en dev)
- Docker (optionnel)

### 1. Cloner le Repository

```bash
git clone https://github.com/votre-repo/microservices-doctorat-app.git
cd microservices-doctorat-app
```

### 2. Configuration

#### Variables d'Environnement (optionnel)

```bash
# JWT Configuration
export JWT_SECRET="myVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong"
export JWT_EXPIRATION=86400000

# Database (si vous utilisez PostgreSQL)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=doctorat_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 3. Démarrer les Services

#### Option A : Démarrage Manuel

```bash
# 1. Discovery Server
cd discovery-server
./mvnw spring-boot:run &

# 2. Config Server
cd ../config-server
./mvnw spring-boot:run &

# 3. API Gateway
cd ../api-gateway
./mvnw spring-boot:run &

# 4. Microservices
cd ../user-service
./mvnw spring-boot:run &

cd ../defense-service
./mvnw spring-boot:run &

cd ../registration-service
./mvnw spring-boot:run &

cd ../notification-service
./mvnw spring-boot:run &

# 5. Frontend (optionnel)
cd ../frontend-angular
npm install
ng serve
```

#### Option B : Avec Docker Compose

```bash
docker-compose up -d
```

### 4. Vérifier que tout fonctionne

```bash
# Vérifier Eureka
curl http://localhost:8761/

# Vérifier API Gateway
curl http://localhost:8080/actuator/health

# Vérifier les services enregistrés
curl http://localhost:8761/eureka/apps
```

---

## 📚 Documentation

### Documentation Complète

| Document | Description |
|----------|-------------|
| [SECURITY.md](./SECURITY.md) | **Configuration de sécurité** détaillée |
| [ARCHITECTURE_SECURITY.md](./ARCHITECTURE_SECURITY.md) | **Vue d'ensemble** de l'architecture |
| [ARCHITECTURE_DIAGRAM.md](./ARCHITECTURE_DIAGRAM.md) | **Diagrammes** détaillés |
| [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) | **Guide de migration** vers la sécurité centralisée |
| [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) | **Documentation des APIs** complète |
| [TESTING_GUIDE.md](./TESTING_GUIDE.md) | **Guide de test** avec exemples |
| [SUMMARY.md](./SUMMARY.md) | **Résumé** des changements |

### Démarrage Rapide

#### 1. S'inscrire

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant@example.com",
    "password": "Doctorant123!",
    "email": "doctorant@example.com",
    "firstName": "Ahmed",
    "lastName": "BENNANI",
    "role": "DOCTORANT",
    "studentId": "CNE12345678"
  }'
```

#### 2. Se connecter

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctorant@example.com",
    "password": "Doctorant123!"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

#### 3. Créer une demande de soutenance

```bash
curl -X POST http://localhost:8080/defense/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Intelligence Artificielle et Santé",
    "resume": "Cette thèse explore...",
    "directeurTheseId": 2,
    "dateProposee": "2026-06-15T14:00:00"
  }'
```

---

## 🧪 Tests

### Exécuter les Tests Unitaires

```bash
# Tous les services
./mvnw clean test

# Un service spécifique
cd user-service
./mvnw test
```

### Tests de Sécurité

Utilisez le script de test fourni :

```bash
chmod +x test-security.sh
./test-security.sh
```

📖 **Guide complet** : [TESTING_GUIDE.md](./TESTING_GUIDE.md)

---

## 📊 Monitoring

### Endpoints Actuator

Tous les services exposent des endpoints de monitoring :

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Métriques
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

### Prometheus & Grafana (à venir)

Configuration disponible dans `/monitoring`

---

## 🔧 Technologies Utilisées

### Backend

- **Spring Boot 3.5.7** - Framework principal
- **Spring Cloud 2024.0.0** - Microservices
- **Spring Security** - Sécurité
- **JWT (JJWT 0.12.3)** - Authentification
- **PostgreSQL** - Base de données
- **H2** - Base de données en mémoire (dev)
- **Maven** - Gestion de dépendances

### Frontend

- **Angular 18** - Framework frontend
- **TypeScript** - Langage
- **Angular Material** - UI Components

### Infrastructure

- **Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Config** - Configuration centralisée
- **Docker** - Conteneurisation
- **Docker Compose** - Orchestration

---

## 📁 Structure du Projet

```
microservices-doctorat-app/
├── api-gateway/              # Point d'entrée + Sécurité
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── ma/spring/cloud/apigateway/
│   │               ├── config/
│   │               │   └── SecurityConfig.java
│   │               ├── filter/
│   │               │   ├── JwtAuthenticationFilter.java
│   │               │   └── RoleBasedAccessFilter.java
│   │               └── util/
│   │                   └── JwtUtil.java
│   └── pom.xml
│
├── user-service/             # Gestion des utilisateurs
├── defense-service/          # Gestion des soutenances
├── registration-service/     # Inscriptions
├── notification-service/     # Notifications
├── discovery-server/         # Eureka
├── config-server/            # Configuration
├── frontend-angular/         # Interface web
├── common-security/          # Bibliothèque commune
│
├── docker-compose.yml        # Configuration Docker
├── SECURITY.md              # Documentation sécurité
├── API_DOCUMENTATION.md     # Documentation APIs
└── README.md                # Ce fichier
```

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez suivre ces étapes :

1. Fork le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

### Standards de Code

- Suivre les conventions Java/Spring
- Ajouter des tests unitaires
- Documenter les APIs
- Mettre à jour la documentation

---

## 🐛 Bugs et Issues

Si vous trouvez un bug, veuillez créer une issue avec :

- Description du problème
- Étapes pour reproduire
- Comportement attendu vs actuel
- Logs pertinents
- Version de l'application

---

## 📝 Roadmap

### Court Terme (v1.1)
- [ ] Tests d'intégration complets
- [ ] Interface Angular complète
- [ ] Refresh Token
- [ ] Rate Limiting

### Moyen Terme (v2.0)
- [ ] OAuth2 Support (Google, Facebook)
- [ ] Authentification à 2 facteurs (2FA)
- [ ] Audit Trail complet
- [ ] Monitoring avancé (Prometheus/Grafana)

### Long Terme (v3.0)
- [ ] Application mobile (React Native)
- [ ] Signature électronique
- [ ] Intégration avec services externes
- [ ] Intelligence artificielle pour recommandations

---

## 📞 Support

### Documentation
- 📖 Consultez la [documentation complète](./docs)
- 💬 Rejoignez notre [Discord](https://discord.gg/votre-serveur)
- 📧 Email: support@votre-domaine.com

### Liens Utiles
- [Guide de démarrage rapide](./QUICKSTART.md)
- [FAQ](./FAQ.md)
- [Troubleshooting](./TROUBLESHOOTING.md)

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👥 Auteurs

- **Votre Nom** - *Initial work* - [YourGitHub](https://github.com/yourusername)

Voir aussi la liste des [contributeurs](https://github.com/votre-repo/contributors) qui ont participé à ce projet.

---

## 🙏 Remerciements

- Spring Boot et Spring Cloud pour les excellents frameworks
- La communauté open source
- Tous les contributeurs

---

## 📈 Statistiques

![GitHub stars](https://img.shields.io/github/stars/votre-repo/microservices-doctorat-app)
![GitHub forks](https://img.shields.io/github/forks/votre-repo/microservices-doctorat-app)
![GitHub issues](https://img.shields.io/github/issues/votre-repo/microservices-doctorat-app)
![GitHub pull requests](https://img.shields.io/github/issues-pr/votre-repo/microservices-doctorat-app)

---

**Date de création** : 25 décembre 2025  
**Version actuelle** : 1.0.0  
**Dernière mise à jour** : 25 décembre 2025

---

<div align="center">
  <b>⭐ Si ce projet vous a aidé, n'hésitez pas à lui donner une étoile ! ⭐</b>
</div>
=======
# portail-suivi-doctorat
>>>>>>> 6ce757d4999ba41a617273a4b88fa27aebe5c2f5
