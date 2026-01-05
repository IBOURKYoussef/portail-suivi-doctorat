# 🆘 URGENT : Disque C: Plein - Libération d'Espace

## ⚠️ Situation Critique

```
Disque C: 
- Utilisé: 237.1 GB
- Libre: 0 GB ❌
- Total: 237.1 GB
```

**IMPOSSIBLE de compiler ou exécuter quoi que ce soit sans espace disque !**

---

## 🚀 Actions URGENTES (Dans l'Ordre)

### 1. Vider la Corbeille (Gain: 1-10 GB)

```powershell
# Vider la corbeille
Clear-RecycleBin -Force -ErrorAction SilentlyContinue
```

### 2. Nettoyer les Fichiers Temporaires (Gain: 5-20 GB)

```powershell
# Supprimer les fichiers temporaires Windows
Remove-Item -Path "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue

# Nettoyer le dossier Windows\Temp
Remove-Item -Path "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
```

### 3. Nettoyer les Caches IntelliJ IDEA (Gain: 2-10 GB)

```powershell
# Caches IntelliJ IDEA
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\tmp" -Recurse -Force -ErrorAction SilentlyContinue

# Logs IntelliJ
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\log" -Recurse -Force -ErrorAction SilentlyContinue
```

### 4. Nettoyer Maven .m2 Repository (Gain: 5-15 GB)

```powershell
# ATTENTION: Cela supprime toutes les dépendances Maven téléchargées
# Elles seront retéléchargées au prochain build
Remove-Item -Path "$env:USERPROFILE\.m2\repository" -Recurse -Force -ErrorAction SilentlyContinue
```

### 5. Nettoyer Docker (Si installé) (Gain: 10-50 GB)

```powershell
# Arrêter tous les conteneurs
docker stop $(docker ps -aq) 2>$null

# Supprimer tout
docker system prune -a --volumes -f 2>$null
```

### 6. Nettoyage Windows (Gain: 5-20 GB)

```powershell
# Ouvrir l'outil de nettoyage Windows
cleanmgr /d C:
```

**Dans la fenêtre qui s'ouvre:**
- ✅ Cocher TOUTES les cases
- ✅ Nettoyer les fichiers système
- ✅ Cliquer "OK"

### 7. Désinstaller Applications Inutilisées (Gain: Variable)

```powershell
# Ouvrir Paramètres > Applications
Start-Process ms-settings:appsfeatures
```

**Applications gourmandes à considérer:**
- Anciens jeux
- Logiciels non utilisés
- Anciennes versions de logiciels
- Applications préinstallées inutiles

### 8. Analyser l'Espace Disque avec WinDirStat

```powershell
# Télécharger WinDirStat (gratuit)
Start-Process "https://windirstat.net/download.html"
```

**Utilisez WinDirStat pour identifier:**
- Les gros dossiers
- Les fichiers volumineux inutiles
- Les duplicatas

---

## 🎯 Script de Nettoyage Automatique

```powershell
Write-Host "🧹 Nettoyage Automatique du Disque C:" -ForegroundColor Yellow
Write-Host ""

# 1. Corbeille
Write-Host "1. Vidage de la corbeille..." -ForegroundColor Cyan
Clear-RecycleBin -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Terminé" -ForegroundColor Green

# 2. Fichiers temporaires
Write-Host "2. Suppression des fichiers temporaires..." -ForegroundColor Cyan
Remove-Item -Path "$env:TEMP\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Terminé" -ForegroundColor Green

# 3. Caches IntelliJ
Write-Host "3. Nettoyage des caches IntelliJ..." -ForegroundColor Cyan
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\tmp" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:LOCALAPPDATA\JetBrains\IntelliJIdea*\log" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Terminé" -ForegroundColor Green

# 4. Caches Maven (OPTIONNEL - décommenter si nécessaire)
# Write-Host "4. Nettoyage du cache Maven..." -ForegroundColor Cyan
# Remove-Item -Path "$env:USERPROFILE\.m2\repository" -Recurse -Force -ErrorAction SilentlyContinue
# Write-Host "   ✓ Terminé" -ForegroundColor Green

# 5. Docker (si installé)
Write-Host "5. Nettoyage Docker..." -ForegroundColor Cyan
docker system prune -a --volumes -f 2>$null
Write-Host "   ✓ Terminé" -ForegroundColor Green

# Vérifier l'espace libéré
Write-Host ""
Write-Host "📊 Espace disque après nettoyage:" -ForegroundColor Yellow
Get-PSDrive C | Select-Object Name, @{Name="UsedGB";Expression={[math]::Round($_.Used/1GB,2)}}, @{Name="FreeGB";Expression={[math]::Round($_.Free/1GB,2)}} | Format-Table -AutoSize

Write-Host "✅ Nettoyage terminé !" -ForegroundColor Green
```

**Pour exécuter ce script:**
```powershell
# Copier tout le code ci-dessus et le coller dans PowerShell (en tant qu'Administrateur)
```

---

## 📁 Dossiers à Vérifier Manuellement

### Gros consommateurs d'espace typiques:

```powershell
# 1. Téléchargements
explorer C:\Users\$env:USERNAME\Downloads

# 2. Bureau
explorer C:\Users\$env:USERNAME\Desktop

# 3. Documents
explorer C:\Users\$env:USERNAME\Documents

# 4. Vidéos
explorer C:\Users\$env:USERNAME\Videos

# 5. AppData Local
explorer $env:LOCALAPPDATA

# 6. Anciens projets
explorer D:\
```

**Vérifiez:**
- ❌ Vieux projets non utilisés
- ❌ Fichiers ISO, images disque
- ❌ Anciennes sauvegardes
- ❌ Captures d'écran/vidéos inutiles
- ❌ Logs volumineux

---

## 🎯 Espace Minimum Requis

Pour ce projet de microservices:
- **Compilation:** Minimum 5 GB libre
- **Exécution (Maven):** Minimum 10 GB libre
- **Exécution (Docker):** Minimum 20 GB libre
- **Recommandé:** 30+ GB libre

---

## 🔧 Solution Immédiate pour Compiler

Une fois **au moins 5 GB libérés**, utilisez Maven en ligne de commande au lieu de l'IDE:

```powershell
# Compiler SANS l'IDE (moins gourmand en espace)
cd "d:\project microservices\microservices-doctorat-app"

# Compiler chaque service individuellement
cd api-gateway
.\mvnw clean compile -DskipTests

cd ..\user-service
.\mvnw clean compile -DskipTests

cd ..\defense-service
.\mvnw clean compile -DskipTests

cd ..\registration-service
.\mvnw clean compile -DskipTests
```

---

## 🆘 Si Toujours Pas Assez d'Espace

### Option 1: Déplacer le Projet sur un Autre Disque

```powershell
# Vérifier les autres disques disponibles
Get-PSDrive -PSProvider FileSystem | Select-Object Name, @{Name="FreeGB";Expression={[math]::Round($_.Free/1GB,2)}}

# Déplacer le projet (exemple vers D:)
Move-Item -Path "d:\project microservices\microservices-doctorat-app" -Destination "E:\project microservices\microservices-doctorat-app"
```

### Option 2: Augmenter l'Espace Disque

1. **Disque dur externe:** Connecter un disque externe et déplacer des fichiers
2. **Nettoyage de disque avancé:** Supprimer les points de restauration système
3. **Redimensionner les partitions:** Utiliser Gestion des disques Windows
4. **Mise à niveau disque:** Installer un disque plus grand

---

## ✅ Checklist Après Nettoyage

Une fois l'espace libéré:

- [ ] Vérifier l'espace libre: `Get-PSDrive C`
- [ ] Au moins 10 GB libre ✓
- [ ] Fermer IntelliJ IDEA
- [ ] Redémarrer l'ordinateur (recommandé)
- [ ] Compiler avec Maven CLI au lieu de l'IDE
- [ ] Si OK, rouvrir IntelliJ

---

## 🚀 Compilation Optimisée (Après Nettoyage)

```powershell
# 1. Nettoyer les anciens builds
cd "d:\project microservices\microservices-doctorat-app"
Get-ChildItem -Path . -Include target -Recurse -Directory | Remove-Item -Recurse -Force

# 2. Compiler TOUS les services en une fois
.\mvnw clean package -DskipTests

# 3. Si erreur d'espace, compiler un par un:
cd discovery-server
.\mvnw clean package -DskipTests

cd ..\api-gateway
.\mvnw clean package -DskipTests

cd ..\user-service
.\mvnw clean package -DskipTests

cd ..\defense-service
.\mvnw clean package -DskipTests

cd ..\registration-service
.\mvnw clean package -DskipTests

cd ..\notification-service
.\mvnw clean package -DskipTests
```

---

## 📞 Résumé des Actions

### IMMÉDIAT (5 minutes)

1. Vider la corbeille
2. Supprimer fichiers temporaires
3. Nettoyer caches IntelliJ
4. Vérifier l'espace libéré

### SI NÉCESSAIRE (10-20 minutes)

5. Nettoyer Maven .m2
6. Nettoyer Docker
7. Utiliser cleanmgr
8. Désinstaller applications

### APRÈS NETTOYAGE

9. Redémarrer l'ordinateur
10. Compiler avec Maven CLI
11. Exécuter les services
12. Lancer les tests

---

**🎯 Objectif: Libérer au moins 10-20 GB avant de continuer !**
