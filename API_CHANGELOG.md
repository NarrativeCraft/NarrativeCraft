# Changelog

## [3.1.0]

### Features
- Added `singleOK()` return in `InkActionResult` to automatically stop any one shot ink action running in background
- Positional arguments are now parsed either with the name, or single value (e.g. `animation play|animation action:play`)
- Added `IChapter getChapter()` and `IScene getScene()` in `IPlayerSession`
- Added in `ICharacterStory` `getCustomNbt()` `setCustomNbt(String nbt)` `isMainCharacter()`