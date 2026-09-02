# Changelog

## [4.0.0]

### Break Changes
- Moved `Side` enum to `utils` package

### Features
- `singleOK()` returned in `InkActionResult` to automatically stop any one shot ink action running in background
- Positional arguments are now parsed either with the name, or single value (e.g. `animation play|animation action:play`)
- `IChapter getChapter()` and `IScene getScene()` in `IPlayerSession`
- in `ICharacterStory` `getCustomNbt()` `setCustomNbt(String nbt)` `isMainCharacter()`
- `VARIABLE_PATTERN` in `InkAction` to detect variable in a tag value before it get replaced by the compiler
- `UserPosition getLastPosition();` and `void setLastPosition(UserPosition lastPosition);` in `IStoryHandler`
- `UserPosition` record in `fr.loudo.narrativecraft.api.utils` holding `x`, `y`, `z`, `xRot` and `yRot`
- `CLIENT_SEVER` side for ink actions, to be both executed on server and client
- New `fr.loudo.narrativecraft.api.client` package holding the client side of the API
- `NarrativeCraftClientAPI` in `fr.loudo.narrativecraft.api.client`, only available on a physical client once NarrativeCraft client initialization has run, exposing `getInkTagDispatcher()` and `getSignalRegistry()`, plus a static `isAvailable()` to guard code that also runs on a server
- `ClientInkTagDispatcher` in `fr.loudo.narrativecraft.api.client.inkAction` and `ClientSignalRegistry` in `fr.loudo.narrativecraft.api.client.signals` to register ink actions and signals on the client side only
- `SignalEmitter` in `fr.loudo.narrativecraft.api.signals` with `emit(Signal, ServerPlayer)`, reachable through `NarrativeCraftAPI.getInstance().getSignalEmitter()`, to emit a `Side.SERVER` signal from the server
- `ClientSignalEmitter` in `fr.loudo.narrativecraft.api.client.signals` with `emit(Signal)`, reachable through `NarrativeCraftClientAPI.getInstance().getSignalEmitter()`, to emit a `Side.CLIENT` signal from the client
- `registerClientInkAction(Class, Supplier)`, `registerSignal(SignalType)` and `registerClientSignal(SignalType)` in `AddonContext`, the client ones to be called from your client initializer
- A signal registry rejects a `SignalType` declared for the other `Side` (`CLIENT_SERVER` is accepted by both)