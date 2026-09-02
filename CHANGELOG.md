# Changelog

## [2.3.0]

### Feature
- Signals (with a bunch of events)
- Timed choice
- Image tag
- Target a character using @/char(name) in the command tag (e.g. `# command "attribute @char(user) minecraft:scale base set 3"`)
- Assign custom nbt to characters
- Render image in dialog `Clara: [img clara/happy] I'm feeling good right now!`
- Item requirement tag
- Locked choice tag
- Character head bobbing when talking (it's back!)
- Save tag (include last character position)
- Re-use entity already in world close to first position of animation in story
- Pre-compile story `/nc story reload (as_file)` - will be loaded in priority when the server/world start
- Launch multiple interaction instead of one in the story
- Return player to main screen when finished story (if available)
- Togglable settings to render names of global characters
- Local save file

### Fix

- Cannot interpolate with "add" action in time tag
