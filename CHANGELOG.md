# Changelog

## [2.2.4]

### Fixes
- Dialog 2D scale not fixed based on gui scale
- Entity played from a playback during subscene recording not ignored
- Can't play, stop story or change locale to more than one player using command
- "user" variable name for a character not replaced by the player name playing the story
- Default ground of entity to false
- Gameplay tag not automatically show hud again
- Delta movement of dropped item in playback random and buggy
- Interaction with item not recorded on neoforge (wrong event called)
- Quotes inside a quoted tag value breaking the parsing (e.g. a command tag holding SNBT/JSON)
- False positive error with minecraft command