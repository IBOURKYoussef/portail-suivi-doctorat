# Script de Test Automatisé - Application Doctorat Microservices
# Date: 25 décembre 2025
# PowerShell Version

$ErrorActionPreference = "Continue"

# Variables globales
$API_GATEWAY = "http://localhost:8080"
$TOTAL_TESTS = 0
$PASSED_TESTS = 0
$FAILED_TESTS = 0
$RESULTS_FILE = "test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"

# Tokens pour différents rôles
$TOKEN_ADMIN = ""
$TOKEN_DOCTORANT = ""
$TOKEN_DIRECTEUR = ""
$TOKEN_CANDIDAT = ""

# IDs créés pendant les tests
$USER_ID_ADMIN = ""
$USER_ID_DOCTORANT = ""
$USER_ID_DIRECTEUR = ""
$CAMPAIGN_ID = ""
$REGISTRATION_ID = ""
$DEFENSE_ID = ""

# Fonction pour afficher une bannière
function Print-Banner {
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "   Tests Automatisés - Application Doctorat" -ForegroundColor Cyan
    Write-Host "   Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
}

# Fonction pour afficher une section
function Print-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Magenta
    Write-Host "  $Title" -ForegroundColor Magenta
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Magenta
    Write-Host ""
}

# Fonction pour vérifier le résultat d'un test
function Check-Result {
    param(
        [string]$TestName,
        [string]$ExpectedStatus,
        [string]$ActualStatus,
        [string]$Response = ""
    )
    
    $script:TOTAL_TESTS++
    
    if ($ActualStatus -eq $ExpectedStatus) {
        Write-Host "✓ PASSED: " -ForegroundColor Green -NoNewline
        Write-Host "$TestName (Status: $ActualStatus)"
        $script:PASSED_TESTS++
        return $true
    } else {
        Write-Host "✗ FAILED: " -ForegroundColor Red -NoNewline
        Write-Host "$TestName (Expected: $ExpectedStatus, Got: $ActualStatus)"
        if ($Response) {
            $truncated = $Response.Substring(0, [Math]::Min(200, $Response.Length))
            Write-Host "  Response: $truncated" -ForegroundColor Red
        }
        $script:FAILED_TESTS++
        return $false
    }
}

# Fonction pour faire une requête HTTP
function Invoke-HttpRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Data = "",
        [string]$Token = ""
    )
    
    $url = "$API_GATEWAY$Endpoint"
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    try {
        $params = @{
            Uri = $url
            Method = $Method
            Headers = $headers
            ErrorAction = "Stop"
        }
        
        if ($Data) {
            $params["Body"] = $Data
        }
        
        $response = Invoke-WebRequest @params
        $status = $response.StatusCode
        $content = $response.Content
        
        return @{
            Status = $status
            Content = $content
        }
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        $content = ""
        
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $content = $reader.ReadToEnd()
            $reader.Close()
        }
        
        return @{
            Status = $status
            Content = $content
        }
    }
}

# ====================================================================================
# TESTS D'INFRASTRUCTURE
# ====================================================================================

function Test-Infrastructure {
    Print-Section "1. Tests d'Infrastructure"
    
    # Test 1.1: API Gateway Health
    Write-Host "Test 1.1: Vérification de l'API Gateway" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/actuator/health"
    Check-Result -TestName "API Gateway Health Check" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
    
    # Test 1.2: Discovery Server
    Write-Host "Test 1.2: Vérification du Discovery Server" -ForegroundColor Blue
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8761/actuator/health" -ErrorAction Stop
        Check-Result -TestName "Discovery Server Health" -ExpectedStatus "200" -ActualStatus $response.StatusCode
    } catch {
        Check-Result -TestName "Discovery Server Health" -ExpectedStatus "200" -ActualStatus "500"
    }
}

# ====================================================================================
# TESTS D'AUTHENTIFICATION
# ====================================================================================

function Test-Authentication {
    Print-Section "2. Tests d'Authentification"
    
    # Test 2.1: Inscription Admin
    Write-Host "Test 2.1: Inscription d'un administrateur" -ForegroundColor Blue
    $adminData = @{
        username = "admin.test@example.com"
        password = "Admin123!"
        email = "admin.test@example.com"
        firstName = "Admin"
        lastName = "Test"
        phone = "+212600000001"
        role = "ADMIN"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/register" -Data $adminData
    if (Check-Result -TestName "Inscription Admin" -ExpectedStatus "201" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:USER_ID_ADMIN = if ($responseObj.user.id) { $responseObj.user.id } else { $responseObj.id }
    }
    
    # Test 2.2: Connexion Admin
    Write-Host "Test 2.2: Connexion Administrateur" -ForegroundColor Blue
    $loginData = @{
        username = "admin.test@example.com"
        password = "Admin123!"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/login" -Data $loginData
    if (Check-Result -TestName "Connexion Admin" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:TOKEN_ADMIN = $responseObj.token
        Write-Host "  Token Admin obtenu: $($TOKEN_ADMIN.Substring(0, 50))..." -ForegroundColor Cyan
    }
    
    # Test 2.3: Inscription Doctorant
    Write-Host "Test 2.3: Inscription d'un doctorant" -ForegroundColor Blue
    $doctorantData = @{
        username = "doctorant.test@example.com"
        password = "Doctorant123!"
        email = "doctorant.test@example.com"
        firstName = "Ahmed"
        lastName = "BENNANI"
        phone = "+212600000002"
        role = "DOCTORANT"
        studentId = "CNE12345678"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/register" -Data $doctorantData
    if (Check-Result -TestName "Inscription Doctorant" -ExpectedStatus "201" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:USER_ID_DOCTORANT = if ($responseObj.user.id) { $responseObj.user.id } else { $responseObj.id }
    }
    
    # Test 2.4: Connexion Doctorant
    Write-Host "Test 2.4: Connexion Doctorant" -ForegroundColor Blue
    $loginData = @{
        username = "doctorant.test@example.com"
        password = "Doctorant123!"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/login" -Data $loginData
    if (Check-Result -TestName "Connexion Doctorant" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:TOKEN_DOCTORANT = $responseObj.token
        Write-Host "  Token Doctorant obtenu: $($TOKEN_DOCTORANT.Substring(0, 50))..." -ForegroundColor Cyan
    }
    
    # Test 2.5: Inscription Directeur
    Write-Host "Test 2.5: Inscription d'un directeur de thèse" -ForegroundColor Blue
    $directeurData = @{
        username = "directeur.test@example.com"
        password = "Directeur123!"
        email = "directeur.test@example.com"
        firstName = "Mohammed"
        lastName = "ALAMI"
        phone = "+212600000003"
        role = "DIRECTEUR_THESE"
        laboratoire = "Laboratoire IA"
        grade = "Professeur"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/register" -Data $directeurData
    if (Check-Result -TestName "Inscription Directeur" -ExpectedStatus "201" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:USER_ID_DIRECTEUR = if ($responseObj.user.id) { $responseObj.user.id } else { $responseObj.id }
    }
    
    # Test 2.6: Connexion Directeur
    Write-Host "Test 2.6: Connexion Directeur" -ForegroundColor Blue
    $loginData = @{
        username = "directeur.test@example.com"
        password = "Directeur123!"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/login" -Data $loginData
    if (Check-Result -TestName "Connexion Directeur" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:TOKEN_DIRECTEUR = $responseObj.token
        Write-Host "  Token Directeur obtenu: $($TOKEN_DIRECTEUR.Substring(0, 50))..." -ForegroundColor Cyan
    }
    
    # Test 2.7: Inscription Candidat
    Write-Host "Test 2.7: Inscription d'un candidat" -ForegroundColor Blue
    $candidatData = @{
        username = "candidat.test@example.com"
        password = "Candidat123!"
        email = "candidat.test@example.com"
        firstName = "Fatima"
        lastName = "ZAHRA"
        phone = "+212600000004"
        role = "CANDIDAT"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/register" -Data $candidatData
    Check-Result -TestName "Inscription Candidat" -ExpectedStatus "201" -ActualStatus $result.Status -Response $result.Content
    
    # Test 2.8: Connexion Candidat
    Write-Host "Test 2.8: Connexion Candidat" -ForegroundColor Blue
    $loginData = @{
        username = "candidat.test@example.com"
        password = "Candidat123!"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/login" -Data $loginData
    if (Check-Result -TestName "Connexion Candidat" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content) {
        $responseObj = $result.Content | ConvertFrom-Json
        $script:TOKEN_CANDIDAT = $responseObj.token
        Write-Host "  Token Candidat obtenu: $($TOKEN_CANDIDAT.Substring(0, 50))..." -ForegroundColor Cyan
    }
    
    # Test 2.9: Connexion avec mauvais credentials
    Write-Host "Test 2.9: Tentative de connexion avec mauvais mot de passe" -ForegroundColor Blue
    $loginData = @{
        username = "admin.test@example.com"
        password = "WrongPassword"
    } | ConvertTo-Json
    
    $result = Invoke-HttpRequest -Method "POST" -Endpoint "/auth/login" -Data $loginData
    Check-Result -TestName "Rejet mauvais credentials" -ExpectedStatus "401" -ActualStatus $result.Status -Response $result.Content
}

# ====================================================================================
# TESTS DE SÉCURITÉ JWT
# ====================================================================================

function Test-JwtSecurity {
    Print-Section "3. Tests de Sécurité JWT"
    
    # Test 3.1: Accès sans token
    Write-Host "Test 3.1: Accès à un endpoint protégé sans token" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/me"
    Check-Result -TestName "Rejet sans token" -ExpectedStatus "401" -ActualStatus $result.Status -Response $result.Content
    
    # Test 3.2: Accès avec token invalide
    Write-Host "Test 3.2: Accès avec token invalide" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/me" -Token "invalid.token.here"
    Check-Result -TestName "Rejet token invalide" -ExpectedStatus "401" -ActualStatus $result.Status -Response $result.Content
    
    # Test 3.3: Accès avec token valide
    Write-Host "Test 3.3: Accès avec token valide (Admin)" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/me" -Token $TOKEN_ADMIN
    Check-Result -TestName "Accès avec token valide" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
}

# ====================================================================================
# TESTS DE CONTRÔLE D'ACCÈS (RBAC)
# ====================================================================================

function Test-RoleBasedAccess {
    Print-Section "4. Tests de Contrôle d'Accès par Rôles (RBAC)"
    
    # Test 4.1: DOCTORANT accède à ses défenses
    Write-Host "Test 4.1: DOCTORANT accède à ses défenses" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/defense/my" -Token $TOKEN_DOCTORANT
    Check-Result -TestName "DOCTORANT accède à /defense/my" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
    
    # Test 4.2: CANDIDAT ne peut pas accéder aux défenses
    Write-Host "Test 4.2: CANDIDAT tente d'accéder aux défenses" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/defense/my" -Token $TOKEN_CANDIDAT
    Check-Result -TestName "CANDIDAT refusé pour /defense/my" -ExpectedStatus "403" -ActualStatus $result.Status -Response $result.Content
    
    # Test 4.3: ADMIN peut accéder aux endpoints admin
    Write-Host "Test 4.3: ADMIN accède aux défenses en attente" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/defense/admin/pending" -Token $TOKEN_ADMIN
    Check-Result -TestName "ADMIN accède à /defense/admin/pending" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
    
    # Test 4.4: DOCTORANT ne peut pas accéder aux endpoints admin
    Write-Host "Test 4.4: DOCTORANT tente d'accéder à un endpoint admin" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/defense/admin/pending" -Token $TOKEN_DOCTORANT
    Check-Result -TestName "DOCTORANT refusé pour endpoint admin" -ExpectedStatus "403" -ActualStatus $result.Status -Response $result.Content
}

# ====================================================================================
# TESTS USER SERVICE
# ====================================================================================

function Test-UserService {
    Print-Section "5. Tests User Service"
    
    # Test 5.1: Récupérer un utilisateur par ID
    Write-Host "Test 5.1: Récupérer un utilisateur par ID" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/$USER_ID_ADMIN" -Token $TOKEN_ADMIN
    Check-Result -TestName "GET /users/{id}" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
    
    # Test 5.2: Liste des directeurs
    Write-Host "Test 5.2: Liste des directeurs de thèse" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/directors" -Token $TOKEN_ADMIN
    Check-Result -TestName "GET /users/directors" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
    
    # Test 5.3: Profil utilisateur courant
    Write-Host "Test 5.3: Récupérer son propre profil" -ForegroundColor Blue
    $result = Invoke-HttpRequest -Method "GET" -Endpoint "/users/me" -Token $TOKEN_DOCTORANT
    Check-Result -TestName "GET /users/me" -ExpectedStatus "200" -ActualStatus $result.Status -Response $result.Content
}

# ====================================================================================
# RÉSUMÉ DES TESTS
# ====================================================================================

function Print-Summary {
    Print-Section "Résumé des Tests"
    
    $pass_rate = 0
    if ($TOTAL_TESTS -gt 0) {
        $pass_rate = [Math]::Round(($PASSED_TESTS / $TOTAL_TESTS) * 100, 2)
    }
    
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  RÉSULTATS FINAUX" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host "  Total Tests    : " -NoNewline
    Write-Host $TOTAL_TESTS -ForegroundColor Blue
    Write-Host "  ✓ Réussis      : " -NoNewline
    Write-Host $PASSED_TESTS -ForegroundColor Green
    Write-Host "  ✗ Échoués      : " -NoNewline
    Write-Host $FAILED_TESTS -ForegroundColor Red
    Write-Host "  Taux de réussite: " -NoNewline
    Write-Host "$pass_rate%" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
    Write-Host ""
    
    # Sauvegarder les résultats
    $results = @{
        timestamp = (Get-Date -Format 'o')
        total_tests = $TOTAL_TESTS
        passed = $PASSED_TESTS
        failed = $FAILED_TESTS
        pass_rate = $pass_rate
        tokens = @{
            admin = if ($TOKEN_ADMIN) { $TOKEN_ADMIN.Substring(0, [Math]::Min(20, $TOKEN_ADMIN.Length)) + "..." } else { "" }
            doctorant = if ($TOKEN_DOCTORANT) { $TOKEN_DOCTORANT.Substring(0, [Math]::Min(20, $TOKEN_DOCTORANT.Length)) + "..." } else { "" }
            directeur = if ($TOKEN_DIRECTEUR) { $TOKEN_DIRECTEUR.Substring(0, [Math]::Min(20, $TOKEN_DIRECTEUR.Length)) + "..." } else { "" }
            candidat = if ($TOKEN_CANDIDAT) { $TOKEN_CANDIDAT.Substring(0, [Math]::Min(20, $TOKEN_CANDIDAT.Length)) + "..." } else { "" }
        }
        created_ids = @{
            campaign_id = $CAMPAIGN_ID
            registration_id = $REGISTRATION_ID
            defense_id = $DEFENSE_ID
        }
    } | ConvertTo-Json -Depth 10
    
    $results | Out-File -FilePath $RESULTS_FILE -Encoding UTF8
    
    Write-Host "Résultats sauvegardés dans: $RESULTS_FILE" -ForegroundColor Magenta
    Write-Host ""
    
    if ($FAILED_TESTS -eq 0) {
        Write-Host "🎉 TOUS LES TESTS SONT RÉUSSIS ! 🎉" -ForegroundColor Green
        Write-Host ""
        return $true
    } else {
        Write-Host "⚠️  Certains tests ont échoué. Vérifiez les logs ci-dessus." -ForegroundColor Red
        Write-Host ""
        return $false
    }
}

# ====================================================================================
# FONCTION PRINCIPALE
# ====================================================================================

function Main {
    Print-Banner
    
    # Vérifier que les services sont démarrés
    Write-Host "Vérification des services..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "$API_GATEWAY/actuator/health" -ErrorAction Stop
        Write-Host "✓ API Gateway accessible" -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "ERREUR: L'API Gateway n'est pas accessible sur $API_GATEWAY" -ForegroundColor Red
        Write-Host "Assurez-vous que tous les services sont démarrés." -ForegroundColor Yellow
        exit 1
    }
    
    # Exécuter les tests
    Test-Infrastructure
    Test-Authentication
    Test-JwtSecurity
    Test-RoleBasedAccess
    Test-UserService
    
    # Afficher le résumé
    $success = Print-Summary
    
    # Code de sortie
    if ($success) {
        exit 0
    } else {
        exit 1
    }
}

# Exécuter le script
Main
