# Changelog

## [2.2.3]

### Fixes
- Overriding an animation with same name linked to a subscene being removed from it
- Hurt bobbing when changing camera angle (I couldn't this bug, but I assume I fix it I hope)
- Cutscene editor playhead staying still when pressing play
- Slight drift between two keyframes sharing the same position and rotation
- Fov layer reading the easing from the wrong keyframe of the segment
- Camera spinning a full turn when the yaw or roll crosses 180/-180
- Cutscene not starting at the first camera keyframe the first time it plays in production
- Cutscene not stopping playbacks when finished in cutscene tag