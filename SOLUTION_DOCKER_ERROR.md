# 🔧 Solution au Problème Docker - Read-Only File System

## 🚨 Problème Rencontré

```
failed to extract layer... read-only file system
input/output error
```

**Cause:** Système de fichiers Docker Desktop corrompu ou plein.

---

## ✅ Solutions (Dans l'Ordre)

### Solution 1: Redémarrer Docker Desktop (RECOMMANDÉ)

```powershell
# 1. Arrêter Docker Desktop complètement
# Clic droit sur l'icône Docker dans la barre des tâches → Quit Docker Desktop

# 2. Attendre 10 secondes

# 3. Relancer Docker Desktop
# Depuis le menu Démarrer

# 4. Attendre que Docker soit complètement démarré (icône stable)

# 5. Réessayer
docker-compose up -d
```

### Solution 2: Nettoyer Docker (Si Solution 1 échoue)

```powershell
# 1. Arrêter tous les conteneurs
docker stop $(docker ps -aq)

# 2. Supprimer tous les conteneurs
docker rm $(docker ps -aq)

# 3. Nettoyer complètement
docker system prune -a --volumes -f

# 4. Relancer Docker Desktop (Quit + Restart)

# 5. Réessayer
docker-compose up -d
```

### Solution 3: Réinitialiser Docker Desktop (Dernier Recours)

⚠️ **ATTENTION:** Cela supprimera TOUTES vos images et conteneurs Docker !

```powershell
# Dans Docker Desktop:
# 1. Ouvrir Docker Desktop
# 2. Settings (icône engrenage) → Troubleshoot → Reset to factory defaults
# 3. Cliquer sur "Reset"
# 4. Attendre la réinitialisation complète
# 5. Redémarrer Windows (recommandé)
# 6. Réessayer
docker-compose up -d
```

---

## 🎯 Solution Alternative: Démarrage Manuel des Services

**Si Docker continue à poser problème**, démarrez les services directement avec Maven :

### 1. Démarrer les Services un par un

```powershell
# Terminal 1 - Discovery Server (Eureka)
cd discovery-server
.\mvnw clean spring-boot:run

# Attendre "Started EurekaServerApplication" (environ 30 secondes)
```

Ouvrir un **NOUVEAU terminal** pour chaque service suivant :

```powershell
# Terminal 2 - Config Server
cd config-server
.\mvnw clean spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway
.\mvnw clean spring-boot:run

# Terminal 4 - User Service
cd user-service
.\mvnw clean spring-boot:run

# Terminal 5 - Defense Service
cd defense-service
.\mvnw clean spring-boot:run

# Terminal 6 - Registration Service
cd registration-service
.\mvnw clean spring-boot:run

# Terminal 7 - Notification Service
cd notification-service
.\mvnw clean spring-boot:run
```

**Total: 7 terminaux PowerShell ouverts en parallèle**

### 2. Vérifier que Tous les Services sont UP

Ouvrir un **8ème terminal** et exécuter :

```powershell
# Vérifier API Gateway
Invoke-WebRequest http://localhost:8080/actuator/health
# Attendu: Status 200 OK

# Vérifier Eureka Dashboard
Start-Process http://localhost:8761
# Vérifier que tous les services sont enregistrés (5 services: API-GATEWAY, USER-SERVICE, DEFENSE-SERVICE, REGISTRATION-SERVICE, NOTIFICATION-SERVICE)
```

### 3. Exécuter les Tests

```powershell
# Dans le terminal principal (ou un 9ème terminal)
cd "d:\project microservices\microservices-doctorat-app"
.\test-all-endpoints.ps1
```

---

## 📊 Vérification Ports Occupés

Avant de démarrer, vérifier qu'aucun port n'est déjà utilisé :

```powershell
# Vérifier tous les ports nécessaires
netstat -ano | findstr "8080 8081 8082 8083 8084 8761 8888"

# Si des ports sont occupés, tuer les processus:
# 1. Identifier le PID dans la dernière colonne
# 2. Tuer le processus:
taskkill /PID <PID> /F
```

---

## 🐛 Dépannage Docker Desktop

### Vérifier l'Espace Disque

```powershell
# Vérifier l'espace disponible sur C:
Get-PSDrive C | Select-Object Used,Free

# Si < 10GB libre, libérer de l'espace:
# - Vider la corbeille
# - Supprimer fichiers temporaires: cleanmgr
# - Désinstaller applications inutiles
```

### Augmenter la Mémoire Docker

1. Ouvrir **Docker Desktop**
2. **Settings** → **Resources**
3. **Memory**: Augmenter à minimum **4 GB** (recommandé: 6-8 GB)
4. **Disk image size**: Vérifier au moins **20 GB**
5. Cliquer **Apply & Restart**

### Logs Docker Desktop

```powershell
# Voir les logs Docker
docker logs <container-name>

# Ou dans Docker Desktop: Containers → Sélectionner un conteneur → View Details
```

---

## ✅ Checklist de Validation

Après avoir résolu le problème:

- [ ] Docker Desktop démarre sans erreur
- [ ] `docker ps` fonctionne
- [ ] `docker version` affiche la version correctement
- [ ] Les ports 8080-8084, 8761, 8888 sont libres
- [ ] Mémoire disponible > 4 GB
- [ ] Espace disque > 10 GB

Ensuite:

- [ ] Démarrer les services (Docker Compose OU Manuel)
- [ ] Vérifier Eureka: http://localhost:8761 (tous services UP)
- [ ] Exécuter les tests: `.\test-all-endpoints.ps1`

---

## 🎯 Recommandation Finale

**Pour ce projet, je recommande le démarrage MANUEL** (Maven) plutôt que Docker Compose car :

✅ Plus rapide à déboguer
✅ Logs visibles directement dans chaque terminal
✅ Pas de problèmes de Docker Desktop
✅ Facile de redémarrer un seul service
✅ Consomme moins de ressources

**Inconvénient:** 7 terminaux à gérer (mais vous pouvez utiliser Windows Terminal avec onglets)

---

## 💡 Astuce: Windows Terminal

Pour gérer les 7 terminaux facilement:

1. Installer **Windows Terminal** depuis le Microsoft Store
2. Ouvrir Windows Terminal
3. Créer 7 onglets (Ctrl+Shift+T)
4. Dans chaque onglet, démarrer un service
5. Naviguer entre les onglets avec Ctrl+Tab

---

## 📞 Si Tout Échoue

Si ni Docker ni Maven ne fonctionnent :

1. **Vérifier Java:**
   ```powershell
   java -version  # Doit afficher Java 17+
   ```

2. **Réinstaller Maven:**
   ```powershell
   # Utiliser le wrapper Maven inclus
   .\mvnw --version
   ```

3. **Vérifier la compilation:**
   ```powershell
   .\mvnw clean install -DskipTests
   ```

4. **Consulter les logs:**
   - Chaque service génère des logs dans `target/` ou `logs/`
   - Chercher les erreurs avec `Get-Content .\logs\spring.log | Select-String "ERROR"`

---

## 🚀 Prochaine Étape

**Une fois les services démarrés**, exécuter les tests :

```powershell
# Vérifier que l'API Gateway répond
Invoke-WebRequest http://localhost:8080/actuator/health

# Si OK (200), lancer les tests
.\test-all-endpoints.ps1
```

**Bonne chance ! 🎯**
