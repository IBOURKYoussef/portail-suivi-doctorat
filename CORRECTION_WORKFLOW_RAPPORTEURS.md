# 🔧 Correction : Workflow Rapporteurs

## ❌ Erreur Rencontrée

```
POST {{base_url}}/api/rapporteurs/1/report
404 Not Found
```

## 🎯 Cause

Le rapporteur avec ID=1 n'existe pas encore. Les rapporteurs sont créés automatiquement lors de la **proposition du jury**.

---

## ✅ Workflow CORRECT

### Ordre d'exécution obligatoire :

#### 1️⃣ Créer une Soutenance (DOCTORANT)
```http
POST {{base_url}}/api/defenses
Authorization: Bearer {{doctorant_token}}
```
➡️ Sauvegarde `{{defense_id}}`

---

#### 2️⃣ Valider les Prérequis (ADMIN)
```http
POST {{base_url}}/api/defenses/{{defense_id}}/validate-prerequisites?approved=true&comment=Prérequis validés
Authorization: Bearer {{admin_token}}
```
➡️ Status devient : `PREREQUISITES_VALIDATED`

---

#### 3️⃣ **NOUVEAU** : Proposer le Jury (DIRECTEUR_THESE)
```http
POST {{base_url}}/api/defenses/{{defense_id}}/jury
Authorization: Bearer {{directeur_token}}
X-User-Id: {{directeur_id}}
Content-Type: application/json

Body:
{
  "president": {
    "professorId": {{admin_id}},
    "name": "Prof. Hassan ALAMI",
    "institution": "ENSIAS Rabat",
    "grade": "Professeur de l'enseignement supérieur",
    "email": "hassan.alami@ensias.ma",
    "role": "PRESIDENT"
  },
  "examiners": [
    {
      "professorId": {{directeur_id}},
      "name": "Dr. Fatima BENNANI",
      "institution": "FST Fès",
      "grade": "Professeur habilité",
      "email": "f.bennani@fst.ma",
      "role": "EXAMINER"
    }
  ],
  "rapporteurs": [
    {
      "professorId": {{admin_id}},
      "name": "Prof. Mohammed TAZI",
      "institution": "Université Mohammed V",
      "grade": "Professeur",
      "email": "m.tazi@um5.ma"
    },
    {
      "professorId": {{directeur_id}},
      "name": "Prof. Amina LAKHDISSI",
      "institution": "INPT Rabat",
      "grade": "Professeur",
      "email": "a.lakhdissi@inpt.ma"
    }
  ]
}
```

**Résultat** :
- ✅ Jury créé (président + examinateurs)
- ✅ 2 Rapporteurs créés automatiquement avec IDs (1 et 2)
- ➡️ Status devient : `JURY_PROPOSED`

---

#### 4️⃣ Lister les Rapporteurs
```http
GET {{base_url}}/api/rapporteurs/defense/{{defense_id}}
Authorization: Bearer {{admin_token}}
```

**Réponse attendue** :
```json
[
  {
    "id": 1,
    "professorId": 1,
    "name": "Prof. Mohammed TAZI",
    "institution": "Université Mohammed V",
    "grade": "Professeur",
    "email": "m.tazi@um5.ma",
    "status": "INVITED",
    "reportFilePath": null,
    "opinion": null,
    "reportSubmissionDate": null
  },
  {
    "id": 2,
    "professorId": 2,
    "name": "Prof. Amina LAKHDISSI",
    "institution": "INPT Rabat",
    "grade": "Professeur",
    "email": "a.lakhdissi@inpt.ma",
    "status": "INVITED",
    "reportFilePath": null,
    "opinion": null,
    "reportSubmissionDate": null
  }
]
```

➡️ **Notez les IDs des rapporteurs** (1 et 2)

---

#### 5️⃣ Valider le Jury (ADMIN)
```http
PUT {{base_url}}/api/defenses/{{defense_id}}/jury/validate
Authorization: Bearer {{admin_token}}
```
➡️ Status devient : `JURY_VALIDATED`

---

#### 6️⃣ MAINTENANT : Soumettre Rapport Rapporteur 1
```http
POST {{base_url}}/api/rapporteurs/1/report
Authorization: Bearer {{directeur_token}}
Content-Type: application/json

Body:
{
  "report": "Le candidat démontre une excellente maîtrise du sujet. Les contributions scientifiques sont significatives et bien documentées. Le manuscrit est de qualité et mérite d'être soutenu.",
  "reportFilePath": "/documents/rapporteur_report_1.pdf",
  "opinion": "FAVORABLE"
}
```

**Résultat attendu** : ✅ `200 OK`

---

#### 7️⃣ Soumettre Rapport Rapporteur 2
```http
POST {{base_url}}/api/rapporteurs/2/report
Authorization: Bearer {{directeur_token}}
Content-Type: application/json

Body:
{
  "report": "Travail de recherche solide avec des apports significatifs. Quelques améliorations mineures suggérées mais le niveau global est satisfaisant.",
  "reportFilePath": "/documents/rapporteur_report_2.pdf",
  "opinion": "FAVORABLE"
}
```

**Résultat attendu** : ✅ `200 OK`

**Validation automatique** :
- Quand TOUS les rapporteurs ont soumis (2/2)
- ET que la majorité est FAVORABLE (2/2 = 100% > 50%)
- ➡️ Status devient automatiquement : `REPORTS_RECEIVED`

---

#### 8️⃣ Vérifier le Status
```http
GET {{base_url}}/api/defenses/{{defense_id}}
Authorization: Bearer {{admin_token}}
```

Le status doit être : `REPORTS_RECEIVED`

---

#### 9️⃣ Continuer le Workflow
Une fois `REPORTS_RECEIVED`, vous pouvez :

```http
POST {{base_url}}/api/defenses/{{defense_id}}/authorize?authorized=true&defenseDate=2025-09-15T14:00:00&location=Amphi A&room=Salle 101&comments=Autorisation accordée
Authorization: Bearer {{admin_token}}
```

---

## 📊 Résumé des Status

| Étape | Action | Status Avant | Status Après |
|-------|--------|--------------|--------------|
| 1 | Create Defense | - | `SUBMITTED` |
| 2 | Validate Prerequisites | `SUBMITTED` | `PREREQUISITES_VALIDATED` |
| 3 | **Propose Jury** | `PREREQUISITES_VALIDATED` | `JURY_PROPOSED` |
| 4 | Validate Jury | `JURY_PROPOSED` | `JURY_VALIDATED` |
| 5 | Submit Reports (tous) | `JURY_VALIDATED` | `REPORTS_RECEIVED` ⚙️ auto |
| 6 | Authorize Defense | `REPORTS_RECEIVED` | `AUTHORIZED` |
| 7 | Schedule Defense | `AUTHORIZED` | `SCHEDULED` |
| 8 | Record Result | `SCHEDULED` | `COMPLETED` |

---

## 🔧 Actions Requises

### 1. Re-importer Collection Postman
La collection a été mise à jour avec les nouveaux endpoints :
- ✅ Propose Jury (DIRECTEUR_THESE)
- ✅ Get Jury Members
- ✅ Validate Jury (ADMIN)

### 2. Redémarrer API Gateway
Les routes ont été ajoutées :
```yaml
# Nouvelles routes
- /api/rapporteurs/**  → DEFENSE-SERVICE
- /api/documents/**    → DEFENSE-SERVICE
```

Recompiler et redémarrer :
```powershell
cd api-gateway
.\mvnw.cmd clean package -DskipTests
# Puis redémarrer le service
```

### 3. Redémarrer Defense Service
Pour charger les nouvelles classes (DocumentService, DocumentController, etc.)

---

## 🎯 Ordre de Test Complet

1. ✅ Register 4 users (Admin, Directeur, Doctorant, Candidat)
2. ✅ Create Defense (Doctorant)
3. ✅ Validate Prerequisites (Admin)
4. 🆕 **Propose Jury (Directeur)** ← Crée les rapporteurs
5. ✅ Get Jury Members (vérifier IDs)
6. ✅ Validate Jury (Admin)
7. 🆕 **Submit Rapporteur 1 Report (Directeur)** ← Utiliser ID réel
8. 🆕 **Submit Rapporteur 2 Report (Directeur)** ← Utiliser ID réel
9. ✅ Verify Status = REPORTS_RECEIVED (automatique)
10. ✅ Authorize Defense (Admin)
11. ✅ Schedule Defense (Admin)
12. ✅ Record Result (Admin)

---

## ⚠️ Points Importants

1. **Les rapporteurs n'existent PAS au démarrage** - ils sont créés par "Propose Jury"
2. **Minimum 2 rapporteurs requis** dans JuryProposalRequest
3. **Tous les rapporteurs doivent soumettre** avant changement de status
4. **Majorité favorable requise** (≥50%) pour passer à REPORTS_RECEIVED
5. **Status PREREQUISITE_VALIDATED requis** avant Propose Jury

---

## 📝 Opinion Rapporteur

Valeurs possibles pour `opinion` :
- `FAVORABLE` : Recommande la soutenance
- `DEFAVORABLE` : Ne recommande pas
- `WITH_RESERVES` : Recommande avec réserves

---

**Date** : 28 décembre 2025
