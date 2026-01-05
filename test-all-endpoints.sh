#!/bin/bash

# Script de Test Automatisé - Application Doctorat Microservices
# Date: 25 décembre 2025

# Couleurs pour le terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Variables globales
API_GATEWAY="http://localhost:8080"
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
RESULTS_FILE="test-results-$(date +%Y%m%d-%H%M%S).json"

# Tokens pour différents rôles
TOKEN_ADMIN=""
TOKEN_DOCTORANT=""
TOKEN_DIRECTEUR=""
TOKEN_CANDIDAT=""
TOKEN_ADMINISTRATIF=""

# IDs créés pendant les tests
USER_ID_ADMIN=""
USER_ID_DOCTORANT=""
USER_ID_DIRECTEUR=""
CAMPAIGN_ID=""
REGISTRATION_ID=""
DEFENSE_ID=""

# Fonction pour afficher une bannière
print_banner() {
    echo -e "${CYAN}"
    echo "═══════════════════════════════════════════════════════"
    echo "   Tests Automatisés - Application Doctorat"
    echo "   Date: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "═══════════════════════════════════════════════════════"
    echo -e "${NC}"
}

# Fonction pour afficher une section
print_section() {
    echo -e "\n${MAGENTA}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${MAGENTA}  $1${NC}"
    echo -e "${MAGENTA}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

# Fonction pour incrémenter les compteurs
increment_total() {
    ((TOTAL_TESTS++))
}

increment_passed() {
    ((PASSED_TESTS++))
    increment_total
}

increment_failed() {
    ((FAILED_TESTS++))
    increment_total
}

# Fonction pour vérifier le résultat d'un test
check_result() {
    local test_name="$1"
    local expected_status="$2"
    local actual_status="$3"
    local response="$4"
    
    if [[ "$actual_status" == "$expected_status" ]]; then
        echo -e "${GREEN}✓ PASSED${NC}: $test_name (Status: $actual_status)"
        increment_passed
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}: $test_name (Expected: $expected_status, Got: $actual_status)"
        echo -e "${RED}  Response: ${response:0:200}${NC}"
        increment_failed
        return 1
    fi
}

# Fonction pour faire une requête HTTP
http_request() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local token="$4"
    
    local url="${API_GATEWAY}${endpoint}"
    local response_file=$(mktemp)
    
    if [ -n "$token" ]; then
        if [ -n "$data" ]; then
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                -d "$data" > "$response_file"
        else
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Authorization: Bearer $token" > "$response_file"
        fi
    else
        if [ -n "$data" ]; then
            curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$data" > "$response_file"
        else
            curl -s -w "\n%{http_code}" -X "$method" "$url" > "$response_file"
        fi
    fi
    
    # Lire le fichier de réponse
    local content=$(head -n -1 "$response_file")
    local status=$(tail -n 1 "$response_file")
    
    rm -f "$response_file"
    
    echo "$status|$content"
}

# ====================================================================================
# TESTS D'INFRASTRUCTURE
# ====================================================================================

test_infrastructure() {
    print_section "1. Tests d'Infrastructure"
    
    # Test 1.1: API Gateway Health
    echo -e "${BLUE}Test 1.1:${NC} Vérification de l'API Gateway"
    result=$(http_request "GET" "/actuator/health" "" "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "API Gateway Health Check" "200" "$status" "$response"
    
    # Test 1.2: Discovery Server
    echo -e "${BLUE}Test 1.2:${NC} Vérification du Discovery Server"
    result=$(curl -s -w "\n%{http_code}" "http://localhost:8761/actuator/health")
    status=$(echo "$result" | tail -n 1)
    check_result "Discovery Server Health" "200" "$status" ""
}

# ====================================================================================
# TESTS D'AUTHENTIFICATION
# ====================================================================================

test_authentication() {
    print_section "2. Tests d'Authentification"
    
    # Test 2.1: Inscription Admin
    echo -e "${BLUE}Test 2.1:${NC} Inscription d'un administrateur"
    result=$(http_request "POST" "/auth/register" '{
        "username": "admin.test@example.com",
        "password": "Admin123!",
        "email": "admin.test@example.com",
        "firstName": "Admin",
        "lastName": "Test",
        "phone": "+212600000001",
        "role": "ADMIN"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Inscription Admin" "201" "$status" "$response"
    
    # Extraire l'ID de l'admin
    USER_ID_ADMIN=$(echo "$response" | jq -r '.user.id // .id // empty')
    
    # Test 2.2: Connexion Admin
    echo -e "${BLUE}Test 2.2:${NC} Connexion Administrateur"
    result=$(http_request "POST" "/auth/login" '{
        "username": "admin.test@example.com",
        "password": "Admin123!"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "Connexion Admin" "200" "$status" "$response"; then
        TOKEN_ADMIN=$(echo "$response" | jq -r '.token // empty')
        echo -e "${CYAN}  Token Admin obtenu: ${TOKEN_ADMIN:0:50}...${NC}"
    fi
    
    # Test 2.3: Inscription Doctorant
    echo -e "${BLUE}Test 2.3:${NC} Inscription d'un doctorant"
    result=$(http_request "POST" "/auth/register" '{
        "username": "doctorant.test@example.com",
        "password": "Doctorant123!",
        "email": "doctorant.test@example.com",
        "firstName": "Ahmed",
        "lastName": "BENNANI",
        "phone": "+212600000002",
        "role": "DOCTORANT",
        "studentId": "CNE12345678"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Inscription Doctorant" "201" "$status" "$response"
    
    USER_ID_DOCTORANT=$(echo "$response" | jq -r '.user.id // .id // empty')
    
    # Test 2.4: Connexion Doctorant
    echo -e "${BLUE}Test 2.4:${NC} Connexion Doctorant"
    result=$(http_request "POST" "/auth/login" '{
        "username": "doctorant.test@example.com",
        "password": "Doctorant123!"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "Connexion Doctorant" "200" "$status" "$response"; then
        TOKEN_DOCTORANT=$(echo "$response" | jq -r '.token // empty')
        echo -e "${CYAN}  Token Doctorant obtenu: ${TOKEN_DOCTORANT:0:50}...${NC}"
    fi
    
    # Test 2.5: Inscription Directeur de Thèse
    echo -e "${BLUE}Test 2.5:${NC} Inscription d'un directeur de thèse"
    result=$(http_request "POST" "/auth/register" '{
        "username": "directeur.test@example.com",
        "password": "Directeur123!",
        "email": "directeur.test@example.com",
        "firstName": "Mohammed",
        "lastName": "ALAMI",
        "phone": "+212600000003",
        "role": "DIRECTEUR_THESE",
        "laboratoire": "Laboratoire IA",
        "grade": "Professeur"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Inscription Directeur" "201" "$status" "$response"
    
    USER_ID_DIRECTEUR=$(echo "$response" | jq -r '.user.id // .id // empty')
    
    # Test 2.6: Connexion Directeur
    echo -e "${BLUE}Test 2.6:${NC} Connexion Directeur"
    result=$(http_request "POST" "/auth/login" '{
        "username": "directeur.test@example.com",
        "password": "Directeur123!"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "Connexion Directeur" "200" "$status" "$response"; then
        TOKEN_DIRECTEUR=$(echo "$response" | jq -r '.token // empty')
        echo -e "${CYAN}  Token Directeur obtenu: ${TOKEN_DIRECTEUR:0:50}...${NC}"
    fi
    
    # Test 2.7: Inscription Candidat
    echo -e "${BLUE}Test 2.7:${NC} Inscription d'un candidat"
    result=$(http_request "POST" "/auth/register" '{
        "username": "candidat.test@example.com",
        "password": "Candidat123!",
        "email": "candidat.test@example.com",
        "firstName": "Fatima",
        "lastName": "ZAHRA",
        "phone": "+212600000004",
        "role": "CANDIDAT"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Inscription Candidat" "201" "$status" "$response"
    
    # Test 2.8: Connexion Candidat
    echo -e "${BLUE}Test 2.8:${NC} Connexion Candidat"
    result=$(http_request "POST" "/auth/login" '{
        "username": "candidat.test@example.com",
        "password": "Candidat123!"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "Connexion Candidat" "200" "$status" "$response"; then
        TOKEN_CANDIDAT=$(echo "$response" | jq -r '.token // empty')
        echo -e "${CYAN}  Token Candidat obtenu: ${TOKEN_CANDIDAT:0:50}...${NC}"
    fi
    
    # Test 2.9: Connexion avec mauvais credentials
    echo -e "${BLUE}Test 2.9:${NC} Tentative de connexion avec mauvais mot de passe"
    result=$(http_request "POST" "/auth/login" '{
        "username": "admin.test@example.com",
        "password": "WrongPassword"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Rejet mauvais credentials" "401" "$status" "$response"
    
    # Test 2.10: Inscription avec email existant
    echo -e "${BLUE}Test 2.10:${NC} Tentative d'inscription avec email existant"
    result=$(http_request "POST" "/auth/register" '{
        "username": "nouveau.user@example.com",
        "password": "Password123!",
        "email": "admin.test@example.com",
        "firstName": "Test",
        "lastName": "Duplicate",
        "role": "CANDIDAT"
    }' "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Rejet email existant" "400" "$status" "$response"
}

# ====================================================================================
# TESTS DE SÉCURITÉ JWT
# ====================================================================================

test_jwt_security() {
    print_section "3. Tests de Sécurité JWT"
    
    # Test 3.1: Accès sans token
    echo -e "${BLUE}Test 3.1:${NC} Accès à un endpoint protégé sans token"
    result=$(http_request "GET" "/users/me" "" "")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Rejet sans token" "401" "$status" "$response"
    
    # Test 3.2: Accès avec token invalide
    echo -e "${BLUE}Test 3.2:${NC} Accès avec token invalide"
    result=$(http_request "GET" "/users/me" "" "invalid.token.here")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Rejet token invalide" "401" "$status" "$response"
    
    # Test 3.3: Accès avec token valide
    echo -e "${BLUE}Test 3.3:${NC} Accès avec token valide (Admin)"
    result=$(http_request "GET" "/users/me" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "Accès avec token valide" "200" "$status" "$response"
}

# ====================================================================================
# TESTS DE CONTRÔLE D'ACCÈS (RBAC)
# ====================================================================================

test_role_based_access() {
    print_section "4. Tests de Contrôle d'Accès par Rôles (RBAC)"
    
    # Test 4.1: DOCTORANT accède à ses propres ressources ✓
    echo -e "${BLUE}Test 4.1:${NC} DOCTORANT accède à ses défenses"
    result=$(http_request "GET" "/defense/my" "" "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "DOCTORANT accède à /defense/my" "200" "$status" "$response"
    
    # Test 4.2: CANDIDAT ne peut pas accéder aux défenses ✗
    echo -e "${BLUE}Test 4.2:${NC} CANDIDAT tente d'accéder aux défenses"
    result=$(http_request "GET" "/defense/my" "" "$TOKEN_CANDIDAT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "CANDIDAT refusé pour /defense/my" "403" "$status" "$response"
    
    # Test 4.3: ADMIN peut accéder aux endpoints admin ✓
    echo -e "${BLUE}Test 4.3:${NC} ADMIN accède aux défenses en attente"
    result=$(http_request "GET" "/defense/admin/pending" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "ADMIN accède à /defense/admin/pending" "200" "$status" "$response"
    
    # Test 4.4: DOCTORANT ne peut pas accéder aux endpoints admin ✗
    echo -e "${BLUE}Test 4.4:${NC} DOCTORANT tente d'accéder à un endpoint admin"
    result=$(http_request "GET" "/defense/admin/pending" "" "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "DOCTORANT refusé pour endpoint admin" "403" "$status" "$response"
}

# ====================================================================================
# TESTS USER SERVICE
# ====================================================================================

test_user_service() {
    print_section "5. Tests User Service"
    
    # Test 5.1: Récupérer un utilisateur par ID
    echo -e "${BLUE}Test 5.1:${NC} Récupérer un utilisateur par ID"
    result=$(http_request "GET" "/users/$USER_ID_ADMIN" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /users/{id}" "200" "$status" "$response"
    
    # Test 5.2: Récupérer un utilisateur par username
    echo -e "${BLUE}Test 5.2:${NC} Récupérer un utilisateur par username"
    result=$(http_request "GET" "/users/username/admin.test@example.com" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /users/username/{username}" "200" "$status" "$response"
    
    # Test 5.3: Liste des directeurs de thèse
    echo -e "${BLUE}Test 5.3:${NC} Liste des directeurs de thèse"
    result=$(http_request "GET" "/users/directors" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /users/directors" "200" "$status" "$response"
    
    # Test 5.4: Profil utilisateur courant
    echo -e "${BLUE}Test 5.4:${NC} Récupérer son propre profil"
    result=$(http_request "GET" "/users/me" "" "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /users/me" "200" "$status" "$response"
}

# ====================================================================================
# TESTS REGISTRATION SERVICE
# ====================================================================================

test_registration_service() {
    print_section "6. Tests Registration Service"
    
    # Test 6.1: Créer une campagne (ADMIN)
    echo -e "${BLUE}Test 6.1:${NC} Créer une campagne d'inscription (ADMIN)"
    result=$(http_request "POST" "/registration/campaigns" '{
        "name": "Campagne Test 2025-2026",
        "description": "Campagne de test automatique",
        "startDate": "2025-01-01T00:00:00",
        "endDate": "2025-12-31T23:59:59",
        "maxCandidates": 100
    }' "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "POST /registration/campaigns" "201" "$status" "$response"; then
        CAMPAIGN_ID=$(echo "$response" | jq -r '.id // empty')
        echo -e "${CYAN}  Campaign ID créé: $CAMPAIGN_ID${NC}"
    fi
    
    # Test 6.2: DOCTORANT tente de créer une campagne ✗
    echo -e "${BLUE}Test 6.2:${NC} DOCTORANT tente de créer une campagne (refusé)"
    result=$(http_request "POST" "/registration/campaigns" '{
        "name": "Campagne Unauthorized",
        "startDate": "2025-01-01T00:00:00",
        "endDate": "2025-12-31T23:59:59"
    }' "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "DOCTORANT refusé pour créer campagne" "403" "$status" "$response"
    
    # Test 6.3: Créer une inscription (CANDIDAT ou DOCTORANT)
    if [ -n "$CAMPAIGN_ID" ]; then
        echo -e "${BLUE}Test 6.3:${NC} Créer une inscription"
        result=$(http_request "POST" "/registration" '{
            "campaignId": '$CAMPAIGN_ID',
            "sujetThese": "Intelligence Artificielle et Santé",
            "domaineRecherche": "IA",
            "directeurTheseId": '$USER_ID_DIRECTEUR',
            "motivations": "Test automatique"
        }' "$TOKEN_DOCTORANT")
        status=$(echo "$result" | cut -d'|' -f1)
        response=$(echo "$result" | cut -d'|' -f2-)
        if check_result "POST /registration" "201" "$status" "$response"; then
            REGISTRATION_ID=$(echo "$response" | jq -r '.id // empty')
            echo -e "${CYAN}  Registration ID créé: $REGISTRATION_ID${NC}"
        fi
    else
        echo -e "${YELLOW}⊘ SKIPPED${NC}: Test 6.3 (pas de campaign ID)"
        increment_total
    fi
    
    # Test 6.4: Consulter ses inscriptions
    echo -e "${BLUE}Test 6.4:${NC} Consulter ses inscriptions"
    result=$(http_request "GET" "/registration/my" "" "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /registration/my" "200" "$status" "$response"
}

# ====================================================================================
# TESTS DEFENSE SERVICE
# ====================================================================================

test_defense_service() {
    print_section "7. Tests Defense Service"
    
    # Test 7.1: Créer une demande de soutenance (DOCTORANT)
    echo -e "${BLUE}Test 7.1:${NC} Créer une demande de soutenance (DOCTORANT)"
    result=$(http_request "POST" "/defense" '{
        "titre": "Test Soutenance Automatique",
        "resume": "Résumé de test pour validation automatique",
        "directeurTheseId": '$USER_ID_DIRECTEUR',
        "dateProposee": "2026-06-15T14:00:00",
        "lieu": "Amphithéâtre Test",
        "specialite": "Informatique",
        "laboratoire": "Lab Test"
    }' "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    if check_result "POST /defense" "201" "$status" "$response"; then
        DEFENSE_ID=$(echo "$response" | jq -r '.id // empty')
        echo -e "${CYAN}  Defense ID créé: $DEFENSE_ID${NC}"
    fi
    
    # Test 7.2: CANDIDAT tente de créer une soutenance ✗
    echo -e "${BLUE}Test 7.2:${NC} CANDIDAT tente de créer une soutenance (refusé)"
    result=$(http_request "POST" "/defense" '{
        "titre": "Test Unauthorized",
        "directeurTheseId": 1,
        "dateProposee": "2026-06-15T14:00:00"
    }' "$TOKEN_CANDIDAT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "CANDIDAT refusé pour créer soutenance" "403" "$status" "$response"
    
    # Test 7.3: Consulter ses soutenances
    echo -e "${BLUE}Test 7.3:${NC} Consulter ses soutenances"
    result=$(http_request "GET" "/defense/my" "" "$TOKEN_DOCTORANT")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /defense/my" "200" "$status" "$response"
    
    # Test 7.4: Consulter une soutenance spécifique
    if [ -n "$DEFENSE_ID" ]; then
        echo -e "${BLUE}Test 7.4:${NC} Consulter une soutenance par ID"
        result=$(http_request "GET" "/defense/$DEFENSE_ID" "" "$TOKEN_ADMIN")
        status=$(echo "$result" | cut -d'|' -f1)
        response=$(echo "$result" | cut -d'|' -f2-)
        check_result "GET /defense/{id}" "200" "$status" "$response"
    else
        echo -e "${YELLOW}⊘ SKIPPED${NC}: Test 7.4 (pas de defense ID)"
        increment_total
    fi
    
    # Test 7.5: Valider prérequis (ADMIN)
    if [ -n "$DEFENSE_ID" ]; then
        echo -e "${BLUE}Test 7.5:${NC} Valider les prérequis (ADMIN)"
        result=$(http_request "POST" "/defense/$DEFENSE_ID/validate-prerequisites?approved=true&comment=Test%20automatique" "" "$TOKEN_ADMIN")
        status=$(echo "$result" | cut -d'|' -f1)
        response=$(echo "$result" | cut -d'|' -f2-)
        check_result "POST /defense/{id}/validate-prerequisites" "200" "$status" "$response"
    else
        echo -e "${YELLOW}⊘ SKIPPED${NC}: Test 7.5 (pas de defense ID)"
        increment_total
    fi
    
    # Test 7.6: DOCTORANT tente de valider prérequis ✗
    if [ -n "$DEFENSE_ID" ]; then
        echo -e "${BLUE}Test 7.6:${NC} DOCTORANT tente de valider prérequis (refusé)"
        result=$(http_request "POST" "/defense/$DEFENSE_ID/validate-prerequisites?approved=true" "" "$TOKEN_DOCTORANT")
        status=$(echo "$result" | cut -d'|' -f1)
        response=$(echo "$result" | cut -d'|' -f2-)
        check_result "DOCTORANT refusé pour valider prérequis" "403" "$status" "$response"
    else
        echo -e "${YELLOW}⊘ SKIPPED${NC}: Test 7.6 (pas de defense ID)"
        increment_total
    fi
    
    # Test 7.7: Statistiques (ADMIN)
    echo -e "${BLUE}Test 7.7:${NC} Récupérer les statistiques (ADMIN)"
    result=$(http_request "GET" "/defense/statistics" "" "$TOKEN_ADMIN")
    status=$(echo "$result" | cut -d'|' -f1)
    response=$(echo "$result" | cut -d'|' -f2-)
    check_result "GET /defense/statistics" "200" "$status" "$response"
}

# ====================================================================================
# TESTS DE PERFORMANCE
# ====================================================================================

test_performance() {
    print_section "8. Tests de Performance"
    
    echo -e "${BLUE}Test 8.1:${NC} Test de latence - Endpoint simple"
    start_time=$(date +%s%N)
    result=$(http_request "GET" "/users/me" "" "$TOKEN_ADMIN")
    end_time=$(date +%s%N)
    duration=$(( (end_time - start_time) / 1000000 ))
    
    status=$(echo "$result" | cut -d'|' -f1)
    if [[ "$status" == "200" && $duration -lt 500 ]]; then
        echo -e "${GREEN}✓ PASSED${NC}: Latence acceptable (${duration}ms < 500ms)"
        increment_passed
    else
        echo -e "${RED}✗ FAILED${NC}: Latence trop élevée (${duration}ms)"
        increment_failed
    fi
}

# ====================================================================================
# RÉSUMÉ DES TESTS
# ====================================================================================

print_summary() {
    print_section "Résumé des Tests"
    
    local pass_rate=0
    if [ $TOTAL_TESTS -gt 0 ]; then
        pass_rate=$(( (PASSED_TESTS * 100) / TOTAL_TESTS ))
    fi
    
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}  RÉSULTATS FINAUX${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "  Total Tests    : ${BLUE}$TOTAL_TESTS${NC}"
    echo -e "  ${GREEN}✓ Réussis${NC}      : ${GREEN}$PASSED_TESTS${NC}"
    echo -e "  ${RED}✗ Échoués${NC}      : ${RED}$FAILED_TESTS${NC}"
    echo -e "  Taux de réussite: ${CYAN}${pass_rate}%${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    # Sauvegarder les résultats
    cat > "$RESULTS_FILE" <<EOF
{
  "timestamp": "$(date -Iseconds)",
  "total_tests": $TOTAL_TESTS,
  "passed": $PASSED_TESTS,
  "failed": $FAILED_TESTS,
  "pass_rate": $pass_rate,
  "tokens": {
    "admin": "${TOKEN_ADMIN:0:20}...",
    "doctorant": "${TOKEN_DOCTORANT:0:20}...",
    "directeur": "${TOKEN_DIRECTEUR:0:20}...",
    "candidat": "${TOKEN_CANDIDAT:0:20}..."
  },
  "created_ids": {
    "campaign_id": "$CAMPAIGN_ID",
    "registration_id": "$REGISTRATION_ID",
    "defense_id": "$DEFENSE_ID"
  }
}
EOF
    
    echo -e "${MAGENTA}Résultats sauvegardés dans: $RESULTS_FILE${NC}\n"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "${GREEN}🎉 TOUS LES TESTS SONT RÉUSSIS ! 🎉${NC}\n"
        return 0
    else
        echo -e "${RED}⚠️  Certains tests ont échoué. Vérifiez les logs ci-dessus.${NC}\n"
        return 1
    fi
}

# ====================================================================================
# FONCTION PRINCIPALE
# ====================================================================================

main() {
    print_banner
    
    # Vérifier que les services sont démarrés
    echo -e "${YELLOW}Vérification des services...${NC}"
    if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo -e "${RED}ERREUR: L'API Gateway n'est pas accessible sur http://localhost:8080${NC}"
        echo -e "${YELLOW}Assurez-vous que tous les services sont démarrés.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ API Gateway accessible${NC}\n"
    
    # Exécuter les tests
    test_infrastructure
    test_authentication
    test_jwt_security
    test_role_based_access
    test_user_service
    test_registration_service
    test_defense_service
    test_performance
    
    # Afficher le résumé
    print_summary
    
    # Code de sortie
    if [ $FAILED_TESTS -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Exécuter le script
main
