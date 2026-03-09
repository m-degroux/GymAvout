# GymAvout

**GymAvout** est une application Android sociale conçue pour connecter les passionnés de sport. Elle permet aux utilisateurs de créer, découvrir et rejoindre des séances de sport (Football, Tennis, Running, etc.) près de chez eux.

## Fonctionnalités

### Profil Utilisateur
- **Personnalisation complète** : Nom, prénom, bio, lieu de vie et niveau.
- **Sports pratiqués** : Affichage dynamique sous forme de "Chips" (bulles) colorées.
- **Statistiques** : Suivi des points gagnés, du nombre d'activités créées et de la note moyenne.
- **Gestion de compte** : Modification du profil et changement de photo via Firebase Storage.

### Découverte & Activités
- **Flux d'actualités** : Liste en temps réel des activités sportives proposées par la communauté.
- **Recherche intelligente** : Filtrez les activités par titre, sport ou ville.
- **Détails complets** : Consultez le lieu, l'heure, le niveau requis et la liste des participants avec leurs avatars.
- **Participation en un clic** : Rejoignez une séance et prévenez automatiquement l'organisateur.

### Messagerie Instantanée
- **Chat en temps réel** : Discutez avec les organisateurs pour régler les détails de la séance.
- **Notifications de participation** : Envoi automatique d'un message lors de l'inscription à une activité.
- **Historique** : Liste de toutes vos conversations actives.

### Paramètres & UI
- **Design Moderne** : Interface sombre (Dark Mode) optimisée pour le confort visuel.
- **Thème personnalisé** : Utilisation des composants Material Design 3.
- **Sécurité** : Authentification sécurisée via Firebase Auth.

## Stack Technique

- **Langage** : [Kotlin](https://kotlinlang.org/)
- **Base de données** : [Firebase Realtime Database](https://firebase.google.com/)
- **Authentification** : Firebase Auth
- **Stockage d'images** : Firebase Storage
- **Chargement d'images** : [Glide](https://github.com/bumptech/glide)
- **Architecture** : Clean UI avec Fragments et Activities.

## Aperçus

*(Ajoutez vos captures d'écran ici plus tard)*
- `Accueil` : Liste des activités et barre de recherche.
- `Profil` : Statistiques et sports pratiqués.
- `Chat` : Interface de discussion fluide.

## Installation

1. **Cloner le projet** :
   ```bash
   git clone https://github.com/votre-username/GymAvout.git
   ```
2. **Configuration Firebase** :
   - Créez un projet sur la [Console Firebase](https://console.firebase.google.com/).
   - Ajoutez une application Android avec le package `fr.math.gymavout`.
   - Téléchargez le fichier `google-services.json` et placez-le dans le dossier `app/`.
   - Activez **Auth** (Email/Password), **Realtime Database** (Europe-West1) et **Storage**.

3. **Build** :
   - Ouvrez le projet dans Android Studio.
   - Synchronisez les fichiers Gradle.
   - Lancez l'application sur un émulateur ou un appareil physique.

