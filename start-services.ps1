# Script de Démarrage des Microservices
# Usage: .\start-services.ps1

param(
    [string]$Service = "all"
)

$ErrorActionPreference = "Continue"

function Start-Service-In-NewWindow {
    param(
        [string]$ServiceName,
        [string]$ServicePath,
        [int]$Port
    )
    
    Write-Host "🚀 Démarrage de $ServiceName sur le port $Port..." -ForegroundColor Cyan
    
    $command = "cd '$ServicePath' ; Write-Host '═══════════════════════════════════════' -ForegroundColor Magenta ; Write-Host '  $ServiceName - Port $Port' -ForegroundColor Yellow ; Write-Host '═══════════════════════════════════════' -ForegroundColor Magenta ; Write-Host '' ; .\mvnw spring-boot:run ; pause"
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $command
    
    Write-Host "✓ $ServiceName démarré dans une nouvelle fenêtre" -ForegroundColor Green
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  DÉMARRAGE DES MICROSERVICES" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$rootPath = "d:\project microservices\microservices-doctorat-app"

if ($Service -eq "all" -or $Service -eq "discovery") {
    Start-Service-In-NewWindow -ServiceName "Discovery Server (Eureka)" `
                                -ServicePath "$rootPath\discovery-server" `
                                -Port 8761
    Write-Host "⏳ Attente de 30 secondes pour Eureka..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}

if ($Service -eq "all" -or $Service -eq "gateway") {
    Start-Service-In-NewWindow -ServiceName "API Gateway" `
                                -ServicePath "$rootPath\api-gateway" `
                                -Port 8080
    Start-Sleep -Seconds 5
}

if ($Service -eq "all" -or $Service -eq "user") {
    Start-Service-In-NewWindow -ServiceName "User Service" `
                                -ServicePath "$rootPath\user-service" `
                                -Port 8081
    Start-Sleep -Seconds 5
}

if ($Service -eq "all" -or $Service -eq "defense") {
    Start-Service-In-NewWindow -ServiceName "Defense Service" `
                                -ServicePath "$rootPath\defense-service" `
                                -Port 8083
    Start-Sleep -Seconds 5
}

if ($Service -eq "all" -or $Service -eq "registration") {
    Start-Service-In-NewWindow -ServiceName "Registration Service" `
                                -ServicePath "$rootPath\registration-service" `
                                -Port 8082
    Start-Sleep -Seconds 5
}

if ($Service -eq "all" -or $Service -eq "notification") {
    Start-Service-In-NewWindow -ServiceName "Notification Service" `
                                -ServicePath "$rootPath\notification-service" `
                                -Port 8084
    Start-Sleep -Seconds 5
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  TOUS LES SERVICES SONT EN COURS DE DÉMARRAGE" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Services démarrés:" -ForegroundColor Cyan
Write-Host "  • Discovery Server: http://localhost:8761" -ForegroundColor White
Write-Host "  • API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  • User Service: http://localhost:8081" -ForegroundColor White
Write-Host "  • Defense Service: http://localhost:8083" -ForegroundColor White
Write-Host "  • Registration Service: http://localhost:8082" -ForegroundColor White
Write-Host "  • Notification Service: http://localhost:8084" -ForegroundColor White
Write-Host ""
Write-Host "⏳ Attendez 1-2 minutes que tous les services s'enregistrent dans Eureka" -ForegroundColor Yellow
Write-Host ""
Write-Host "🔍 Vérifier Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "🧪 Exécuter les tests: .\test-all-endpoints.ps1" -ForegroundColor Cyan
Write-Host ""
