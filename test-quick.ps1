# Script de Test - Application Doctorat Microservices
$ErrorActionPreference = "Continue"
$API_GATEWAY = "http://localhost:8080"
$PASSED = 0
$FAILED = 0
$TOTAL = 0
$TOKEN_ADMIN = ""
$TOKEN_DOCTORANT = ""

function Test-Result {
    param([string]$Name, [int]$Expected, [int]$Actual, [string]$Details = "")
    $script:TOTAL++
    if ($Actual -eq $Expected) {
        Write-Host "[OK] $Name" -ForegroundColor Green
        $script:PASSED++
    } else {
        Write-Host "[FAIL] $Name (Expected: $Expected, Got: $Actual)" -ForegroundColor Red
        if ($Details) { Write-Host "  $Details" -ForegroundColor Yellow }
        $script:FAILED++
    }
}

function HTTP-Request {
    param([string]$Method, [string]$Uri, [string]$Body = "", [string]$Token = "")
    try {
        $headers = @{ "Content-Type" = "application/json" }
        if ($Token) { $headers["Authorization"] = "Bearer $Token" }
        
        $params = @{ Uri = "$API_GATEWAY$Uri"; Method = $Method; Headers = $headers }
        if ($Body) { $params["Body"] = $Body }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        return @{ Status = 200; Data = $response }
    } catch {
        $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 500 }
        return @{ Status = $code; Data = $null; Error = $_.Exception.Message }
    }
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   TESTS AUTOMATISES - APPLICATION DOCTORAT" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

# TEST 1: Infrastructure
Write-Host "--- 1. TESTS D INFRASTRUCTURE ---" -ForegroundColor Magenta
Write-Host "Test 1.1: API Gateway Health Check"
$result = HTTP-Request -Method GET -Uri "/actuator/health"
Test-Result "API Gateway accessible" 200 $result.Status

Write-Host "Test 1.2: Discovery Server"
try {
    $r = Invoke-WebRequest "http://localhost:8761/actuator/health" -ErrorAction Stop
    Test-Result "Discovery Server accessible" 200 $r.StatusCode
} catch {
    Test-Result "Discovery Server accessible" 200 500
}

# TEST 2: Authentification
Write-Host ""
Write-Host "--- 2. TESTS D AUTHENTIFICATION ---" -ForegroundColor Magenta

Write-Host "Test 2.1: Inscription Admin"
$adminUser = @{
    username = "admin_$(Get-Random)"
    password = "Admin123!"
    email = "admin$(Get-Random)@test.com"
    firstName = "Admin"
    lastName = "Test"
    phone = "+212600000001"
    role = "ADMIN"
} | ConvertTo-Json

$result = HTTP-Request -Method POST -Uri "/auth/register" -Body $adminUser
Test-Result "Inscription Admin" 200 $result.Status

Write-Host "Test 2.2: Connexion Admin"
if ($result.Status -eq 200) {
    $loginData = @{
        username = ($adminUser | ConvertFrom-Json).username
        password = "Admin123!"
    } | ConvertTo-Json
    
    $result = HTTP-Request -Method POST -Uri "/auth/login" -Body $loginData
    Test-Result "Connexion Admin reussie" 200 $result.Status
    
    if ($result.Status -eq 200 -and $result.Data.accessToken) {
        $script:TOKEN_ADMIN = $result.Data.accessToken
        Write-Host "  Token Admin obtenu" -ForegroundColor Cyan
    }
}

Write-Host "Test 2.3: Inscription Doctorant"
$doctorantUser = @{
    username = "doctorant_$(Get-Random)"
    password = "Doctorant123!"
    email = "doctorant$(Get-Random)@test.com"
    firstName = "Ahmed"
    lastName = "BENNANI"
    phone = "+212600000002"
    role = "DOCTORANT"
    dateOfBirth = "1995-05-15"
    cin = "AB123456"
} | ConvertTo-Json

$result = HTTP-Request -Method POST -Uri "/auth/register" -Body $doctorantUser
Test-Result "Inscription Doctorant" 200 $result.Status

Write-Host "Test 2.4: Connexion Doctorant"
if ($result.Status -eq 200) {
    $loginData = @{
        username = ($doctorantUser | ConvertFrom-Json).username
        password = "Doctorant123!"
    } | ConvertTo-Json
    
    $result = HTTP-Request -Method POST -Uri "/auth/login" -Body $loginData
    Test-Result "Connexion Doctorant reussie" 200 $result.Status
    
    if ($result.Status -eq 200 -and $result.Data.accessToken) {
        $script:TOKEN_DOCTORANT = $result.Data.accessToken
        Write-Host "  Token Doctorant obtenu" -ForegroundColor Cyan
    }
}

# TEST 3: Securite JWT
Write-Host ""
Write-Host "--- 3. TESTS SECURITE JWT ---" -ForegroundColor Magenta

Write-Host "Test 3.1: Acces sans token (doit echouer)"
$result = HTTP-Request -Method GET -Uri "/api/users"
Test-Result "Rejet sans token" 401 $result.Status

if ($TOKEN_ADMIN) {
    Write-Host "Test 3.2: Acces avec token Admin"
    $result = HTTP-Request -Method GET -Uri "/api/users" -Token $TOKEN_ADMIN
    Test-Result "Acces autorise avec token Admin" 200 $result.Status
}

Write-Host "Test 3.3: Acces avec token invalide"
$result = HTTP-Request -Method GET -Uri "/api/users" -Token "InvalidToken123"
Test-Result "Rejet avec token invalide" 401 $result.Status

# TEST 4: RBAC
Write-Host ""
Write-Host "--- 4. TESTS RBAC ---" -ForegroundColor Magenta

if ($TOKEN_DOCTORANT) {
    Write-Host "Test 4.1: Doctorant accedant a /api/users (admin only)"
    $result = HTTP-Request -Method GET -Uri "/api/users" -Token $TOKEN_DOCTORANT
    Test-Result "Rejet acces non autorise" 403 $result.Status
}

if ($TOKEN_ADMIN) {
    Write-Host "Test 4.2: Admin peut lister les users"
    $result = HTTP-Request -Method GET -Uri "/api/users" -Token $TOKEN_ADMIN
    Test-Result "Admin accede aux users" 200 $result.Status
}

# TEST 5: User Service
Write-Host ""
Write-Host "--- 5. TESTS USER SERVICE ---" -ForegroundColor Magenta

if ($TOKEN_ADMIN) {
    Write-Host "Test 5.1: Lister tous les utilisateurs"
    $result = HTTP-Request -Method GET -Uri "/api/users" -Token $TOKEN_ADMIN
    Test-Result "Liste utilisateurs recuperee" 200 $result.Status
    
    if ($result.Status -eq 200 -and $result.Data) {
        $userCount = if ($result.Data -is [array]) { $result.Data.Count } else { 1 }
        Write-Host "  $userCount utilisateur(s) trouve(s)" -ForegroundColor Cyan
    }
}

# TEST 6: Defense Service
Write-Host ""
Write-Host "--- 6. TESTS DEFENSE SERVICE ---" -ForegroundColor Magenta

if ($TOKEN_DOCTORANT) {
    Write-Host "Test 6.1: Creer une demande de soutenance"
    $defenseData = @{
        title = "Intelligence Artificielle et Machine Learning"
        description = "These sur application du ML"
        defenseDate = "2026-06-15T10:00:00"
        location = "Salle A"
        doctorantId = 1
        directeurId = 2
    } | ConvertTo-Json
    
    $result = HTTP-Request -Method POST -Uri "/defense/create" -Body $defenseData -Token $TOKEN_DOCTORANT
    Test-Result "Creation demande soutenance" 201 $result.Status
    
    Write-Host "Test 6.2: Lister les soutenances"
    $result = HTTP-Request -Method GET -Uri "/defense" -Token $TOKEN_DOCTORANT
    Test-Result "Liste soutenances" 200 $result.Status
}

# TEST 7: Registration Service
Write-Host ""
Write-Host "--- 7. TESTS REGISTRATION SERVICE ---" -ForegroundColor Magenta

if ($TOKEN_ADMIN) {
    Write-Host "Test 7.1: Creer une campagne"
    $campaignData = @{
        name = "Campagne Doctorat 2025-2026"
        startDate = "2025-09-01"
        endDate = "2025-12-31"
        description = "Campagne de recrutement"
        maxCandidates = 100
        active = $true
    } | ConvertTo-Json
    
    $result = HTTP-Request -Method POST -Uri "/registration/campaigns" -Body $campaignData -Token $TOKEN_ADMIN
    Test-Result "Creation campagne" 201 $result.Status
    
    Write-Host "Test 7.2: Lister les campagnes"
    $result = HTTP-Request -Method GET -Uri "/registration/campaigns" -Token $TOKEN_ADMIN
    Test-Result "Liste campagnes" 200 $result.Status
}

# TEST 8: Notification Service
Write-Host ""
Write-Host "--- 8. TESTS NOTIFICATION SERVICE ---" -ForegroundColor Magenta

if ($TOKEN_ADMIN) {
    Write-Host "Test 8.1: Envoyer une notification"
    $notificationData = @{
        userId = 1
        userEmail = "test@example.com"
        userName = "Test User"
        type = "SYSTEM"
        title = "Test notification"
        message = "Test message"
    } | ConvertTo-Json
    
    $result = HTTP-Request -Method POST -Uri "/notification/send" -Body $notificationData -Token $TOKEN_ADMIN
    Test-Result "Envoi notification" 201 $result.Status
    
    Write-Host "Test 8.2: Liste notifications"
    $result = HTTP-Request -Method GET -Uri "/notification" -Token $TOKEN_ADMIN
    Test-Result "Liste notifications" 200 $result.Status
}

# RESUME
Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   RESUME DES TESTS" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total Tests:  $TOTAL" -ForegroundColor White
Write-Host "Reussis:      $PASSED" -ForegroundColor Green
Write-Host "Echoues:      $FAILED" -ForegroundColor Red

$passRate = if ($TOTAL -gt 0) { [math]::Round(($PASSED / $TOTAL) * 100, 2) } else { 0 }
Write-Host "Taux:         $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 60) { "Yellow" } else { "Red" })
Write-Host ""

if ($FAILED -eq 0) {
    Write-Host "TOUS LES TESTS SONT REUSSIS !" -ForegroundColor Green
} else {
    Write-Host "ATTENTION: $FAILED test(s) ont echoue" -ForegroundColor Yellow
}
Write-Host ""
