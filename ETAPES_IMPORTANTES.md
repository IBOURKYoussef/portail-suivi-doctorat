# ⚠️ ÉTAPES OBLIGATOIRES AVANT DE TESTER

## ❌ ERREUR 403 Forbidden - POURQUOI ?

Vous obtenez "403 Forbidden" car vous n'avez **PAS** exécuté les requêtes dans l'ordre !

## ✅ ORDRE OBLIGATOIRE

### PHASE 1 : Authentication (À FAIRE EN PREMIER !)

**VOUS DEVEZ exécuter ces 4 requêtes AVANT TOUTE AUTRE CHOSE :**

1. **Register ADMIN** → Cliquez sur "Send" → Vérifie que tu reçois "201 Created" → La variable `admin_token` est sauvegardée automatiquement

2. **Register DIRECTEUR_THESE** → Cliquez sur "Send" → Vérifie "201 Created" → La variable `directeur_token` est sauvegardée

3. **Register DOCTORANT** → Cliquez sur "Send" → Vérifie "201 Created" → La variable `doctorant_token` est sauvegardée ✅

4. **Register CANDIDAT** → Cliquez sur "Send" → Vérifie "201 Created" → La variable `candidat_token` est sauvegardée

### VÉRIFICATION des variables

Après avoir exécuté les 4 Register, clique sur l'icône "œil" 👁️ en haut à droite de Postman à côté de "Doctorat App - Local" et vérifie que tu vois :

```
admin_token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
admin_id: 1
directeur_token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
directeur_id: 2
doctorant_token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
doctorant_id: 3
candidat_token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
candidat_id: 4
```

**Si ces variables sont VIDES**, c'est normal que tu reçois 403 !

### MAINTENANT : Create Defense

**SEULEMENT APRÈS** avoir vérifié que `doctorant_token` et `directeur_id` existent, tu peux exécuter :

5. **Create Defense Request (DOCTORANT)** → Maintenant ça devrait marcher !

## 🔍 POURQUOI 403 ?

- **403 Forbidden** = Tu es authentifié mais tu n'as pas le rôle correct
- Si `{{doctorant_token}}` est VIDE → Le header Authorization est "Bearer " (vide) → 403
- Si `{{directeur_id}}` est vide → Le JSON contient `"directorId": ` (invalide) → Erreur

## 📝 CHECKLIST

- [ ] J'ai importé `Postman-Collection-Complete.json`
- [ ] J'ai importé `Postman-Environment-Local.json`
- [ ] J'ai sélectionné "Doctorat App - Local" dans le menu déroulant
- [ ] J'ai exécuté "Register ADMIN" → 201 ✅
- [ ] J'ai exécuté "Register DIRECTEUR_THESE" → 201 ✅
- [ ] J'ai exécuté "Register DOCTORANT" → 201 ✅
- [ ] J'ai exécuté "Register CANDIDAT" → 201 ✅
- [ ] J'ai vérifié que les variables contiennent des valeurs (œil 👁️)
- [ ] MAINTENANT je peux tester "Create Defense Request"

## 🛠️ SI ÇA NE MARCHE TOUJOURS PAS

Si après avoir suivi TOUTES ces étapes tu reçois encore 403, envoie-moi :

1. Une capture d'écran des variables d'environnement (œil 👁️)
2. Une capture d'écran de la réponse "Register DOCTORANT" (doit être 201)
3. La réponse exacte du serveur pour "Create Defense"
