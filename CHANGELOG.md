# Changelog

## [2.0.11]

### Fixes

- Npc skin cannot be applied
- Not teleported to first camera in cutscene editor if no animations
- Crash if going in cutscene editor with npcs without session set
- Camera angle not taking custom global dialog data
- Can't stop sound from ink tag if it started without fade
- Animation and subscene tag loop not working
- Shake tag `0 0 0` not clearing active shakes
- Animation and subscene stop not killing entities
- 3D Dialog glitched rendering if a shader is on using iris in 26.2
- Can open character position screen camera angle in production mode (and sometimes may cause a crash)
- Playing one story to one player in multiplayer had characters disappearing
- Bandwidth optimization for sending skins

### Misc
- NPC name tag are now not rendered
- Skip indicator on dialogs is hidden when we go to the next dialog