# Changelog

## [2.1.0]

### Features
- One keyframe for FOV Layer is now parsed and executed
- Text layer: render text on screen during a cutscene #22
- Sound layer: play a sound during a cutscene #21
- Locale support (multiple languages of your story in one world)
- Added 3 actions in the text tag: shadow, mute and text align
- Added 2 keybinds to start and stop recording

### Fixes

- Can't create characters in NeoForge dedicated server 1.21.1
- Text from text tag not correctly centered on screen
- Fake player/entity playback delta, ground detection and step sounds not working correctly
- Scene with only tags not detected as a new scene #20
- Shortcuts in keyframe menus and cutscene layers conflicting with each other
- Editing camera angle position when the whole world is air - 26.2

### Misc
- Easing is now taken from the 2nd keyframe instead of the first one
- Player can't move if a 2D dialog is displayed on screen while in gameplay mode
- Template tab in camera angle maker shows display name instead of short UUID (does not apply to old data)