# Gestion des Comptes Administrateurs

## 🔐 Sécurité

Pour des raisons de sécurité, les comptes **ADMIN** et **ADMINISTRATIF** ne peuvent **PAS** être créés via l'interface publique d'inscription.

---

## 📋 Compte Administrateur Par Défaut

Au premier démarrage du service `user-service`, un compte administrateur par défaut est automatiquement créé :

### Identifiants par défaut :
```
Username: admin
Password: Admin@123
Email: admin@doctorat.ma
```

### ⚠️ IMPORTANT
**Changez immédiatement ces identifiants après la première connexion !**

---

## 🔧 Configuration Personnalisée

Vous pouvez personnaliser les identifiants du compte admin par défaut dans `application.yml` :

```yaml
app:
  admin:
    username: votre_username
    password: votre_password_securise
    email: votre_email@domaine.com
```

Ou via des variables d'environnement :
```bash
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=VotreMotDePasseSecurise123!
APP_ADMIN_EMAIL=admin@votre-domaine.com
```

---

## 👥 Créer de Nouveaux Administrateurs

### Option 1 : Via l'API (recommandé pour les scripts)

Seuls les administrateurs existants peuvent créer de nouveaux comptes ADMIN ou ADMINISTRATIF :

```bash
# Se connecter en tant qu'admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'

# Utiliser le token JWT reçu pour créer un nouveau compte admin
curl -X POST http://localhost:8080/api/users/admin/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  -d '{
    "username": "nouvel_admin",
    "password": "MotDePasseSecurise123!",
    "email": "nouvel_admin@doctorat.ma",
    "firstName": "Prénom",
    "lastName": "Nom",
    "phone": "+212600000000",
    "role": "ADMIN"
  }'
```

### Option 2 : Via l'interface Frontend (à implémenter)

Une interface de gestion des utilisateurs réservée aux ADMIN sera créée dans le module d'administration du frontend.

---

## 🔑 Rôles Disponibles

### Inscription Publique (sans authentification) :
- ✅ **CANDIDAT** - Candidat au doctorat
- ✅ **DOCTORANT** - Doctorant inscrit
- ✅ **DIRECTEUR_THESE** - Directeur de thèse

### Création Restreinte (ADMIN uniquement) :
- 🔒 **ADMINISTRATIF** - Personnel administratif
- 🔒 **ADMIN** - Administrateur système

---

## 📊 Endpoints Administrateurs

### Authentification
```
POST /auth/login              # Connexion (public)
POST /auth/register           # Inscription (public, rôles limités)
GET  /auth/profile            # Profil utilisateur (authentifié)
```

### Gestion des Utilisateurs (ADMIN only)
```
GET  /api/users                    # Lister tous les utilisateurs
GET  /api/users/{id}               # Détails d'un utilisateur
GET  /api/users/username/{username} # Rechercher par username
GET  /api/users/directors          # Lister les directeurs de thèse
POST /api/users/admin/create       # Créer un compte ADMIN/ADMINISTRATIF
```

---

## 🛡️ Bonnes Pratiques de Sécurité

1. **Changez immédiatement** le mot de passe par défaut
2. **Utilisez des mots de passe forts** :
   - Minimum 12 caractères
   - Majuscules + minuscules + chiffres + symboles
   - Exemple : `Adm!n@D0ct0r4t#2025`

3. **Limitez le nombre d'administrateurs** au strict nécessaire

4. **Activez l'audit** des actions administratives (à implémenter)

5. **Utilisez HTTPS** en production

6. **Rotation des mots de passe** tous les 90 jours

7. **Authentification à deux facteurs** (2FA) - à implémenter

---

## 🚀 Démarrage Rapide

### 1. Démarrer le service
```bash
cd user-service
mvn spring-boot:run
```

### 2. Vérifier les logs
```
========================================
Compte administrateur créé avec succès!
Username: admin
Password: Admin@123
Email: admin@doctorat.ma
IMPORTANT: Changez ce mot de passe dès la première connexion!
========================================
```

### 3. Se connecter
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

### 4. Tester l'accès admin
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

---

## ❓ FAQ

**Q: Que faire si j'oublie le mot de passe admin ?**
> R: Supprimez la base de données (H2 en dev) et redémarrez le service. Le compte sera recréé avec les identifiants par défaut.

**Q: Puis-je avoir plusieurs administrateurs ?**
> R: Oui, utilisez l'endpoint `/api/users/admin/create` avec un compte admin existant.

**Q: Le compte admin est-il créé en production ?**
> R: Oui, mais assurez-vous de changer les identifiants par défaut dans les variables d'environnement.

**Q: Comment désactiver la création automatique du compte admin ?**
> R: Commentez ou supprimez la classe `DataInitializer.java`.

---

## 📝 TODO / Améliorations Futures

- [ ] Interface frontend de gestion des utilisateurs (ADMIN)
- [ ] Changement de mot de passe obligatoire à la première connexion
- [ ] Authentification à deux facteurs (2FA)
- [ ] Audit des actions administratives
- [ ] Verrouillage de compte après X tentatives échouées
- [ ] Politique de mot de passe configurable
- [ ] Notification par email lors de création de compte admin
- [ ] Gestion des permissions granulaires (RBAC)

---

## 📧 Contact

Pour toute question de sécurité, contactez l'équipe de développement.

**Version:** 1.0.0  
**Dernière mise à jour:** 28 décembre 2025
