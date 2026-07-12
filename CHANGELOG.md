# Changelog

## [2.1.0]

### Features

- One keyframe for Fov Layer is now parsed and executed
- Text layer : render text on screen during a cutscene #22
- Sound layer : play a sound during a cutscene #21
- Locale support (multiple languages of your story in one world)
- Added 3 in text tag : shadow, mute and text align

### Fixes

- Can't create characters in NeoForge dedicated server 1.21.1
- Text from text tag not centered correctly on screen
- Fake player/entity playback delta, ground detection and step sounds not correctly working
- Scene with only tags not detected as a new scene #20

### Misc

- Easing is now taken from the 2nd keyframe not the first one
- Player can't move if a 2d dialog is rendered on screen if in gameplay mode

## [2.0.14]

### Fixes

- Can't drop item with NeoForge
- Every entity on a playback had a visible name (like items dropped)

## [2.0.11]

### Feature
- Added `current` argument in `session` command to know your current session `/nc session current`.

### Fixes

- Npc skin cannot be applied
- Not teleported to first camera in cutscene editor if no animations
- Crash if going in cutscene editor with npcs without session set
- Camera angle not taking custom global dialog data
- Can't stop sound from ink tag if it started without fade
- Animation and subscene tag loop not working
- Shake tag `0 0 0` not clearing active shakes
- Animation and subscene stop not killing entities
- Can open character position screen camera angle in production mode (and sometimes may cause a crash)
- Playing one story to one player in multiplayer had characters disappearing
- Bandwidth optimization for sending skins
- Camera rotation rotating only one side in cutscene if rotation applied
- Template characters saved in memory even without saving in camera angle editor
- Camera angle not renaming correctly
- Deleting an animation or subscene automatically remove them for subscene or cutscene linked to
- Load player save if choosing a scene from the main menu
- Music from main menu not stopped if leaving the screen with the hidden "Leave screen" button
- Music from main menu not looping
- Skin of npc not sent if changing dialog data if no session set
- Editing a text in text tag making scroll text sound
- Rotation sent for teleporting pitch and yaw inverted

### Misc
- NPC name tag are now not rendered
- Skip indicator on dialogs is hidden when we go to the next dialog
