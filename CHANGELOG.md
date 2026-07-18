# Changelog

## [2.1.1]

### Features

- Improved story manager UI
- Improved cutscene maker editor UI
- Added a dialog button in camera angle preview to switch to dialog mode
- Scrolling behavior if too many characters in camera angle in dialog mode

### Fixes
- Some crash case while trying to show a character skin
- Crash when opening the global dialog editor with no target character
- Prevent saving an animation while still recording
- Cutscene scrubbing no longer freezes the server on a bad tick
- Loading a save with no compiled chapter/scene now fails cleanly
- Disconnecting while recording now cleans up subscene playbacks
- Spawn-tick-0 actions are no longer executed twice
- Narration lines with a colon are no longer mistaken for a speaker
- Spamming "continue dialogue" no longer skips lines
- Removed fake players no longer linger in command name auto-complete #25
- Characters spawned from camera angle "on ground" always false
- Switching editor giving conflicts
- Spamming characters button in dialog mode rendering multiple dialogs at the same time

## [2.1.0]

### Features

- One keyframe for Fov Layer is now parsed and executed
- Text layer : render text on screen during a cutscene #22
- Sound layer : play a sound during a cutscene #21
- Locale support (multiple languages of your story in one world)
- Added 3 actions in text tag : shadow, mute and text align
- Added 2 keybinds to start recording and stop it

### Fixes

- Can't create characters in NeoForge dedicated server 1.21.1
- Text from text tag not centered correctly on screen
- Fake player/entity playback delta, ground detection and step sounds not correctly working
- Scene with only tags not detected as a new scene #20
- Shortcuts in keyframe menus and cutscene layers conflicting

### Misc

- Easing is now taken from the 2nd keyframe not the first one
- Player can't move if a 2d dialog is rendered on screen if in gameplay mode
- Template tab in camera angle maker shows display name instead of short uuid (does not apply to old data)

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
