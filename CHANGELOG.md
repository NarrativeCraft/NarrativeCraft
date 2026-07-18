# Changelog

## [2.1.1]

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