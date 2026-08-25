# Changelog

## [2.3.0]

### Features
- Timed choice
- Image tag
- Target a character using @char(name) in the command tag (e.g. `# command "attribute @char(user) minecraft:scale base set 3"`)
- Assign custom nbt to characters
- Render image in dialog `Clara: [img clara/happy] I'm feeling good right now!`
- Item requirement tag
- Locked choice
- Character head bobbing when talking (it's back!)
- Save tag (include last character position)
- Re-use entity already in world close to first position of animation in story

### Fixes
- Dialog 2D scale not fixed based on gui scale
- Entity played from a playback during subscene recording not ignored
- Can't play, stop or change locale to more than one player using command
- "user" variable name for a character not replaced by the player name playing the story