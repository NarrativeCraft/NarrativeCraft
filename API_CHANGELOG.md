# Changelog

## [3.1.0]

### Features
- `singleOK()` returned in `InkActionResult` to automatically stop any one shot ink action running in background
- Positional arguments are now parsed either with the name, or single value (e.g. `animation play|animation action:play`)
- `IChapter getChapter()` and `IScene getScene()` in `IPlayerSession`
- in `ICharacterStory` `getCustomNbt()` `setCustomNbt(String nbt)` `isMainCharacter()`
- `VARIABLE_PATTERN` in `InkAction` to detect variable in a tag value before it get replaced by the compiler
- `UserPosition getLastPosition();` and `void setLastPosition(UserPosition lastPosition);` in `IStoryHandler`
- `UserPosition` record in `fr.loudo.narrativecraft.api.utils` holding `x`, `y`, `z`, `xRot` and `yRot`
- `CLIENT_SEVER` side for ink actions, to be both executed on server and client