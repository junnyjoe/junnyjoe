# Rapport de projet — Application mobile de gestion de classe

**Module** : Développement mobile — Licence 3 SIL  
**Technologies** : Java, XML, Room, Android Studio — application 100 % locale (aucun serveur)

## 1. Fonctionnalités réalisées

| Exigence | Réalisation |
|---|---|
| Sélection du rôle | `RoleActivity` : Mode Enseignant ou Mode Étudiant (choix d'un profil local) |
| Restriction des droits | Boutons d'ajout, modification et suppression masqués en mode Étudiant ; `EtudiantFormActivity` se ferme si le rôle n'est pas « enseignant » |
| Classe unique | Création, consultation, modification (enseignant) ; consultation seule (étudiant). Ligne unique `id = 1` |
| Étudiants | Ajout, liste `RecyclerView`, détail, modification, suppression avec boîte de confirmation |
| Validation | Nom, prénom, téléphone obligatoires ; e-mail au format valide ; messages d'erreur sous chaque champ |
| Appel | Bouton **Appeler** → `Intent.ACTION_DIAL` (le composeur s'ouvre, l'appel n'est jamais lancé automatiquement) |
| Messagerie locale | Choix du destinataire, envoi, `RecyclerView`, messages envoyés (à droite, couleur primaire) et reçus (à gauche, gris) |
| Pièces jointes | Sélecteur système (`OpenDocument`) pour photo, vidéo ou document ; seule l'`Uri` + le type sont stockés ; ouverture par `ACTION_VIEW` |
| Notification locale | Canal « Nouveaux messages », nom de l'expéditeur + aperçu ; un appui ouvre la conversation concernée ; permission `POST_NOTIFICATIONS` demandée (Android 13+) |
| Persistance | Room (3 entités, 3 DAO) ; données conservées après redémarrage |
| Listes vides / erreurs | Message explicite pour : aucun étudiant, aucun résultat de recherche, conversation vide, pièce jointe indisponible, notification refusée, échec d'enregistrement |
| Facultatif | Recherche d'un étudiant par nom, compteur d'étudiants, suppression d'un message envoyé (appui long), mode sombre |

## 2. Organisation de l'application

```
 ┌────────────────────────── Interface ──────────────────────────┐
 │ XML (layout/*.xml)  ◄── ViewBinding ──►  Activities           │
 │  Role · Home · Classe · EtudiantList · EtudiantForm ·          │
 │  EtudiantDetail · Conversation        + Adapters (RecyclerView)│
 └───────────────────────────────┬────────────────────────────────┘
                                 │ observe LiveData / appelle des actions
 ┌───────────────────────────────▼────────────────────────────────┐
 │ ViewModel : ClasseViewModel · EtudiantViewModel ·              │
 │             ConversationViewModel                              │
 └───────────────────────────────┬────────────────────────────────┘
 ┌───────────────────────────────▼────────────────────────────────┐
 │ Repository  (lectures LiveData, écritures sur un pool de       │
 │              threads, résultat renvoyé sur le thread principal)│
 └───────────────────────────────┬────────────────────────────────┘
 ┌───────────────────────────────▼────────────────────────────────┐
 │ Room : AppDatabase ─ ClasseDao · EtudiantDao · MessageDao      │
 │        Entités : Classe · Etudiant · Message                   │
 └─────────────────────────────────────────────────────────────────┘
 Utilitaires : Session (rôle/profil actif, SharedPreferences),
               Validators, NotificationHelper
```

Packages : `data` (entités, DAO, base, repository), `viewmodel`, `ui` (activities et adapters), `util`.

Choix principaux :
- **ViewBinding** plutôt que `findViewById` : liaisons vérifiées à la compilation.
- **Une seule `EtudiantListActivity`** réutilisée en trois modes (consultation, choix du profil, choix du destinataire) pour éviter la duplication.
- **Écritures asynchrones** via `Repository` + `ExecutorService` ; les lectures utilisent `LiveData`, donc l'interface se met à jour seule.
- **Événements ponctuels** (message enregistré, erreur) consommés après lecture pour ne pas rejouer la notification après une rotation d'écran.

## 3. Modèle de données

| Table | Colonnes |
|---|---|
| `classe` | `id` (=1), `intitule`, `description`, `anneeUniversitaire` |
| `etudiant` | `id` (auto), `nom`, `prenom`, `email`, `telephone` |
| `message` | `id` (auto), `expediteurId` → etudiant, `destinataireId` → etudiant, `contenu`, `dateTime`, `attachmentUri`, `attachmentType` (PHOTO/VIDEO/DOCUMENT), `attachmentMime`, `attachmentName` |

Les clés étrangères de `message` sont en `ON DELETE CASCADE` : supprimer un étudiant supprime ses messages (précisé dans la boîte de confirmation).

## 4. Difficultés rencontrées et solutions

- **Une seule classe** : clé primaire fixée à 1 avec `REPLACE`, ce qui évite toute gestion de doublons.
- **Accès durable aux pièces jointes** : `takePersistableUriPermission` après le choix du fichier ; sinon l'`Uri` devient invalide au redémarrage.
- **Ouverture depuis la notification** : la conversation est ouverte du point de vue du *destinataire* (`ME_ID` = destinataire) ; le profil local est mis à jour en conséquence.
- **Notification en un seul geste** : si la permission Android 13 est refusée, le message est quand même enregistré et un message l'explique.
- **`PendingIntent`** : code de requête unique par message pour que les extras ne soient pas écrasés.

## 5. Tests

Test automatique : `ValidatorsTest` (e-mail, téléphone, champs vides) — 4 tests, exécutés avec succès.

Scénarios manuels à dérouler sur émulateur/téléphone (cocher après exécution) :

| # | Scénario | Résultat attendu |
|---|---|---|
| 1 | Enseignant : créer puis modifier la classe | Données affichées sur l'accueil |
| 2 | Ajouter, consulter, modifier, supprimer un étudiant | Liste à jour ; confirmation avant suppression |
| 3 | Formulaire vide / e-mail invalide / téléphone vide | Erreurs sous les champs, rien d'enregistré |
| 4 | Liste d'étudiants vide | Message explicatif |
| 5 | Étudiant A écrit à B, puis changer de mode et se mettre en B | Message « reçu » à gauche, « envoyé » à droite |
| 6 | Joindre photo, vidéo, document puis ouvrir | Application compatible lancée ; message d'erreur si absente |
| 7 | Bouton Appeler | Composeur ouvert avec le bon numéro, appel non lancé |
| 8 | Envoi d'un message | Notification (expéditeur + aperçu) ; appui → bonne conversation |
| 9 | Mode Étudiant | Aucun bouton d'ajout/modification/suppression |
| 10 | Fermer et rouvrir l'application | Classe, étudiants et messages conservés |
| 11 | Conversation sans message | Message « Aucun message pour le moment » |

## 6. Limites

- Une seule classe ; pas d'authentification ni de synchronisation entre appareils (conforme au sujet).
- Pas d'aperçu image dans la conversation (icône + nom de fichier uniquement).
- Si le fichier joint est supprimé de l'appareil, la pièce jointe est signalée « indisponible ».
- Le projet n'a pas pu être compilé dans l'environnement de rédaction (pas de SDK Android) : la compilation, l'APK et les captures d'écran sont à produire dans Android Studio.

## 7. Guide d'installation et d'utilisation

**Installation** : ouvrir le dossier `GestionClasse` dans Android Studio, attendre la synchronisation Gradle, lancer sur un appareil Android 7.0+. Pour l'APK : *Build > Build APK(s)* puis copier `app-debug.apk` sur le téléphone et l'installer (autoriser les sources inconnues).

**Utilisation** :
1. **Enseignant** : *Créer la classe*, puis *Étudiants* → **+** pour ajouter les étudiants.
2. **Étudiant** : choisir son profil dans la liste, puis *Messages* → choisir un camarade → écrire, joindre un fichier avec le trombone, envoyer.
3. Pour lire les messages d'un autre étudiant : menu ⋮ → *Changer de mode* → choisir l'autre profil.
4. Fiche étudiant → **Appeler** ouvre le composeur.

## 8. Captures d'écran

À insérer après exécution : sélection du rôle, accueil, classe, liste, formulaire, détail, conversation.
