# Changelog

## [2.2.3]

### Fixes
- 1.20.1 - game crash when editing camera angle position
- Overriding an animation with same name linked to a subscene being removed from it
- Hurt bobbing when changing camera angle (I couldn't reproduce this bug, but I assume I fix it I hope)
- Cutscene editor playhead staying still when pressing play
- Slight drift between two keyframes sharing the same position and rotation
- Fov layer reading the easing from the wrong keyframe of the segment
- Camera spinning a full turn when the yaw or roll crosses 180/-180
- Cutscene not starting at the first camera keyframe the first time it plays in production
- Cutscene not stopping playbacks when finished in cutscene tag
- Conflict client-server when exiting an editor (interaction, camera angle...)
- Starting recording when riding an entity make it de-spawn
- 1.20.1 - Closing options screen with "esc" while in main screen completely close it
- OS conflict with ink import using "/" or "\\" for path
- Non character entities with name tag shown in playback
- HUD still hidden after a cutscene ended
- Accidentally spectating a character when entering a camera angle and left-clicking the entity 
- Fake player appearing transparent when adding it either in camera angle editor main screen editor

## Misc
- Shows a sentence in chat when a story has finished