# Cutscene Controller

## Layers

La timeline est organisée en **layers** empilés verticalement. Une colonne à gauche liste tous les layers disponibles.

Cliquer sur un élément appartenant à un layer inactif switche automatiquement sur ce layer, avec un bref highlight dans la colonne pour signaler le changement.

Les layers de base sont :
- **Caméra** (un ou plusieurs) : chaque layer caméra représente un chemin d'interpolation continu. Il peut y en avoir plusieurs, chacun créé via le bouton `+` dans la colonne.
- **Trigger** : contient les keyframes trigger jaunes.

Les développeurs peuvent enregistrer des layers personnalisés via l'API du mod, en fournissant un nom.

### Priorité des layers caméra

L'ordre des layers dans la colonne définit leur priorité : le layer le plus haut est prioritaire. Si deux layers caméra ont des keyframes qui jouent sur la même plage de ticks, le layer de plus haute priorité est joué, et l'autre est **complètement ignoré** pour toute cette plage. L'utilisateur peut réordonner les layers dans la colonne pour ajuster la priorité.

## Menu

Pour chaque élément sélectionné, un menu apparaît à droite avec les options modifiables. Il y aura un bouton **apply** pour appliquer les changements et un bouton **supprimer** avec une icône de poubelle permet de supprimer l'élément sélectionné, avec une dialog box de confirmation.

## Barre de progression

Il y a une barre de progression en bas de l'écran. La tête de lecture est une ligne rouge verticale qui **traverse tous les layers**. Le tick courant est global, pas par layer.

Les keyframes sont représentées dans le layer auquel elles appartiennent. Un bouton play/pause se trouve au début de la barre, avec le tick actuel et le tick maximum affichés au milieu.

Pour déplacer une keyframe, il faut la sélectionner et glisser la souris. Un tooltip affiche le tick exact pendant le glissement.

## Layers caméra et cuts

Chaque layer caméra est un chemin d'interpolation continu entre toutes ses keyframes. Pour créer un cut, l'utilisateur ajoute un nouveau layer caméra. Les keyframes du nouveau layer commencent là où la caméra doit couper.

Les keyframes d'un même layer sont reliées par une ligne pour montrer qu'elles sont interpolées ensemble. Il n'y a pas de fond coloré — les layers étant séparés, la distinction visuelle est assurée par la structure des layers eux-mêmes.

Le seul type de transition entre deux layers caméra est le **cut instantané**.

## Preview

Quand l'utilisateur appuie sur play, sa caméra est automatiquement positionnée et tous les keyframes s'exécutent. Pour une expérience immersive, il peut appuyer sur `H` pour cacher l'HUD, cacher ou afficher l'HUD n'affecte pas la lecture.

## Comportement

L'utilisateur est en mode spectateur. La barre de progression et les autres icônes sont rendues en continu sur l'écran, avec la possibilité d'interagir avec en appuyant sur `T`.

## Raccourcis

- `H` : Afficher / Cacher l'HUD (indépendant du play/pause)
- `T` : Accéder/Quitter l'accès aux widgets
- `DEL` : Supprimer l'élément sélectionné
- `CTRL + Z` : Annuler l'action
- `CTRL + Y` : Rétablir l'action annulée
- `CTRL + S` : Sauvegarder la cutscene
- `ARROW RIGHT` : Déplacer la keyframe sélectionnée de 1 tick vers la droite
- `ARROW LEFT` : Déplacer la keyframe sélectionnée de 1 tick vers la gauche
- `SHIFT + ARROW RIGHT` : Déplacer la keyframe sélectionnée de 5 ticks vers la droite
- `SHIFT + ARROW LEFT` : Déplacer la keyframe sélectionnée de 5 ticks vers la gauche

---

# Keyframe

## Verte (caméra)

La keyframe verte définit la position de la caméra à un tick donné. Elle appartient au layer caméra dans lequel elle a été créée, et l'interpolation est calculée entre toutes les keyframes de ce layer.

### Ajout

Se positionner à l'endroit voulu dans le monde, puis appuyer sur le bouton `+` du layer caméra actif.

### Menu (droite)

Quand une keyframe verte est sélectionnée, le menu affiche :
- Les coordonnées (x, y, z), le pitch, le yaw et le FOV
- Un dropdown **easing** pour changer le type d'interpolation. Quand un easing non-linéaire est sélectionné, l'icône de la keyframe passe d'une forme ronde à un sablier.

### Easing

Le dropdown easing est disponible dans le menu de la keyframe. Changer l'easing modifie visuellement l'icône de la keyframe (rond → sablier).
Un développeur tier pourra ajouter son propre easing.

---

## Jaune (trigger)

La keyframe trigger appartient au **layer trigger**. Elle sert à injecter des lignes de script Ink directement dans la cutscene à un tick donné.

### Ajout

Appuyer sur le bouton `+` du layer trigger dans la colonne de gauche pour ajouter une keyframe trigger au tick courant.

### Menu (droite)

Quand une keyframe trigger est sélectionnée, le menu affiche un champ multiligne pour saisir les commandes Ink à exécuter.