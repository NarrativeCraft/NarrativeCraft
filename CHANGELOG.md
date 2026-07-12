# Changelog

## [2.1.0]

### Features

- One keyframe for Fov Layer is now parsed and executed
- Text layer : render text on screen during a cutscene #22
- Sound layer : play a sound during a cutscene #21
- Locale support (multiple languages of your story in one world)
- Added 3 actions in text tag : shadow, mute and text align

### Fixes

- Can't create characters in NeoForge dedicated server 1.21.1
- Text from text tag not centered correctly on screen
- Fake player/entity playback delta, ground detection and step sounds not correctly working
- Scene with only tags not detected as a new scene #20
- Shortcuts in keyframe menus and cutscene layers conflicting

### Misc

- Easing is now taken from the 2nd keyframe not the first one
- Player can't move if a 2d dialog is rendered on screen if in gameplay mode