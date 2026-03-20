# Cutscene Controller


## Menu
Pour chaque élements, il y aura un petit menu à droite montrant les options modifiable. Il y aura toujours tout en bas 2 bouton **Apply changes** et un bouton **supprimer** avec une icône de poubelle pour supprimer l'élément actuel. Il y aura un dialog box pour confirmer.

## Barre de progression

Il y a une barre de progression en bas de l'écran avec une ligne jaune pour montrer où l'ont se trouve. Des icônes (keyframe) seront présent, avec pour chacun un layer unique superposé entre eux sous forme d'opacité basse. Il y a un bouton play/pause au début de la barre, avec le tick actuel et tick maxiumum au milieu de la barre de progression.
Pour déplacer un keyframe, il faut sélectionner et glisser la souris, il y aura un petit tooltip pour montrer à quel tick se trouve exactement la keyframe pendant le glissement.

## Preview

Quand l'utilisateur va appuyer sur play, ça va automatiquement placer sa caméra et executer tous les keyframe. Pour avoir une expérience immersive pour voir à quoi pourrait ressembler sa cinématique, il pourra appuyer sur *H* pour cacher l'HUD. Le moyen pour mettre pause est soit *ECHAP* ou remontrer l'HUD pour mettre en pause.

## Comportement
L'utilisateur sera sous forme de mode spectateur, mais sans le  delta mouvement avec un mouvement brute pour être plus précis, la barre de progressions ainsi que les autres icônes seront rendu en continu sur l'écran avec la possibilité d'intéragir avec en appuyant sur *T*.

## Shorcut

- DEL : Supprimer l'élément sélectionné
- H : Toogle l'HUD.
- T : Toogle l'accès au widgets.
- CTRL + Z : Annuler l'action
- CTRL + Y : Re-mettre l'action précédemment annulé
- CTRL + S : Sauvegarder la cutscene controller
- ARROW RIGHT : Déplacer la keyframe sélectionné de 1 tick vers la droite
- ARROW LEFT : Déplacer la keyframe sélectionné de 1 tick vers la gauche
- SHIFT + ARROW RIGHT : Déplacer la keyframe sélectionné de 5 ticks vers la droite
- SHIFT + ARROW LEFT : Déplacer la keyframe sélectionné de 5 ticks vers la gauche
- ECHAP : Ouvre un menu pour sauvegarder, quitter et sauvegarder, quitter sans sauvegarder (avec confimation)

# Keyframe

## Vert

Le keyframe vert est une keyframe qui sert à définir la position d'une caméra pour faire une interpolation entre 2 points.

### L'ajout
La keyframe verte sera placé dans la barre de progression. Pour en ajouter un, il faut se positionner ou l'on veut que la caméra soit et on appuie sur le petit "+".

Cela va ajouter une keyframe verte dans la barre de progression. Si l'on souhait modifier la position, il faut appuyer sur la keyframe pour ovrir un menu à droite.

Ce menu aura plusieurs champs, avec les coordoonées (x, y, z), le pitch, yaw et le FOV. quand on est exactement sur le tick de la keyframe, ces data seront mis à jour en live.

Si l'on veut mettre à jour la position, il faut appuyer tout en bas sur "Apply changes".

### Easing
Pour changer le easing, il y aura un champ en plus avec un dropdown montrant les possibilité de easing. Si sélectionné, l'icone du keyframe va changer de forme ronde à sablier.

## Jaune

La keyframe jaune est une keyframe trigger qui va servir à renseigner des lignes de script utilisé sur les fichiers ink pour directement les executer dans la cutscene.

### L'ajout

Pour ajouter une keyframe trigger, il y aura une icône pour l'ajouter, ceci va ajouter une icone keyframe trigger dans un autre layer.

### Modifier

Pour ajouter des commandes, il faut appuyer dessus pour ouvrir un champ multiligne pour ajouter les commandes que l'ont veut.

