# Script de redémarrage complet de l'application

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  REDEMARRAGE COMPLET - APPLICATION DOCTORAT" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# Arrêter tous les services Java
Write-Host "Arret de tous les services..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 5
Write-Host "OK Tous les services arretes`n" -ForegroundColor Green

$basePath = "D:\project microservices\microservices-doctorat-app"

# 1. Discovery Server
Write-Host "[1/6] Demarrage Discovery Server (8761)..." -ForegroundColor Cyan
Set-Location "$basePath\discovery-server"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 20
Write-Host "OK Discovery Server demarre`n" -ForegroundColor Green

# 2. User Service
Write-Host "[2/6] Demarrage User Service (8081)..." -ForegroundColor Cyan
Set-Location "$basePath\user-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 20
Write-Host "OK User Service demarre`n" -ForegroundColor Green

# 3. Registration Service
Write-Host "[3/6] Demarrage Registration Service (8082)..." -ForegroundColor Cyan
Set-Location "$basePath\registration-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15
Write-Host "OK Registration Service demarre`n" -ForegroundColor Green

# 4. Defense Service
Write-Host "[4/6] Demarrage Defense Service (8083)..." -ForegroundColor Cyan
Set-Location "$basePath\defense-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15
Write-Host "OK Defense Service demarre`n" -ForegroundColor Green

# 5. Notification Service
Write-Host "[5/6] Demarrage Notification Service (8084)..." -ForegroundColor Cyan
Set-Location "$basePath\notification-service"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15
Write-Host "OK Notification Service demarre`n" -ForegroundColor Green

# 6. API Gateway (en dernier)
Write-Host "[6/6] Demarrage API Gateway (8080)..." -ForegroundColor Cyan
Set-Location "$basePath\api-gateway"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 25
Write-Host "OK API Gateway demarre`n" -ForegroundColor Green

# Vérification
Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION DES SERVICES" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

$services = @{
    8761 = "Discovery Server"
    8080 = "API Gateway"
    8081 = "User Service"
    8082 = "Registration Service"
    8083 = "Defense Service"
    8084 = "Notification Service"
}

foreach ($port in $services.Keys | Sort-Object) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "OK $($services[$port]) (port $port) : ACTIF" -ForegroundColor Green
    } else {
        Write-Host "ERREUR $($services[$port]) (port $port) : INACTIF" -ForegroundColor Red
    }
}

# Vérifier Eureka
Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "  SERVICES ENREGISTRES DANS EUREKA" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

Start-Sleep -Seconds 10

try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"} -ErrorAction Stop
    $apps = $eureka.applications.application
    if ($apps) {
        foreach ($app in $apps) {
            Write-Host "OK $($app.name)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "Aucun service enregistre dans Eureka" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERREUR Impossible de contacter Eureka" -ForegroundColor Red
}

Write-Host "`n================================================" -ForegroundColor Green
Write-Host "  DEMARRAGE TERMINE !" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "`nAccedez a:" -ForegroundColor Yellow
Write-Host "  - Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor Cyan
Write-Host "`nPour tester avec Postman:" -ForegroundColor Yellow
Write-Host "  1. POST http://localhost:8080/auth/register" -ForegroundColor Cyan
Write-Host "  2. Copiez le token" -ForegroundColor Cyan
Write-Host "  3. Utilisez-le dans les autres requetes`n" -ForegroundColor Cyan