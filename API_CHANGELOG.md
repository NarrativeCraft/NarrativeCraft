# Changelog

## [3.1.0]

### Features
- Added `singleOK()` return in `InkActionResult` to automatically stop any one shot ink action running in background
- Positional arguments are now parsed either with the name, or single value (e.g. `animation play|animation action:play`)
- Added `IChapter getChapter()` and `IScene getScene()` in `IPlayerSession`
- Added in `ICharacterStory` `getCustomNbt()` `setCustomNbt(String nbt)` `isMainCharacter()`
- Added `VARIABLE_PATTERN` in `InkAction` to detect variable in a tag value before it get replaced by the compiler
- Added `UserPosition getLastPosition();` and `void setLastPosition(UserPosition lastPosition);` in `IStoryHandler`
- Added `UserPosition` record in `fr.loudo.narrativecraft.api.utils` holding `x`, `y`, `z`, `xRot` and `yRot`