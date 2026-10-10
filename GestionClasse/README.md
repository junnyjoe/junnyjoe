# Gestion de classe — application Android (Java / XML / Room)

Projet de fin de module (Licence 3 SIL) : gestion d'une classe, de ses étudiants et d'une messagerie locale.
Voir le rapport complet : [`docs/RAPPORT.md`](docs/RAPPORT.md).

## Ouvrir et lancer
1. Android Studio (Koala ou plus récent) → **Open** → dossier `GestionClasse`.
2. Laisser la synchronisation Gradle se terminer (AGP 8.5.2, Gradle 8.7, JDK 17).
3. Choisir un émulateur ou un téléphone (Android 7.0 / API 24 minimum) puis **Run ▶**.
4. APK de démonstration : **Build > Build Bundle(s) / APK(s) > Build APK(s)** →
   `app/build/outputs/apk/debug/app-debug.apk`.

## Tests unitaires
`./gradlew testDebugUnitTest` (validation des formulaires).
