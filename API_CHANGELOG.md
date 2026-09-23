# Changelog

## [5.0.0]

### Break Changes
- Removed `KeyframeMenu` and every UI member of `Keyframe` (`KEYFRAME_SPRITE`, `KEYFRAME_SELECTED_SPRITE`, `SIZE`, `x`, `y`, `click`, `drag`, `render`, `createMenu`, `isHovered`, `setLayerPosition`, `getX`, `setX`, `getY`, `setY`): the authoring UI is being rebuilt
- A cutscene layer is now data only, so it can be loaded on a dedicated server: `execute(float)` and `stop()` were removed from `ICutsceneLayer`, and `createDefaultKeyframe(int)` from `CutsceneLayer`. Their client behavior moved to `ClientCutsceneLayerType`
- An addon registering an `ICutsceneLayerType` must now also register a `ClientCutsceneLayerType` from its client initializer, otherwise its layer is loaded and saved but never played

### Features
- `ClientCutsceneLayerType<L>` in `fr.loudo.narrativecraft.api.client.editors.cutscene`, holding the client side of a layer type: `getTypeId()`, `getLayerClass()`, `createDefaultKeyframe(L, int)` and `createPlayer(L)`
- `ClientCutsceneLayerPlayer` with `execute(float)` and `stop()`, created once per layer and per playback, so the playback state no longer lives in the layer
- `ClientCutsceneLayerRegistry`, reachable through `NarrativeCraftClientAPI.getInstance().getCutsceneLayerRegistry()`, and `registerClientCutsceneLayer(ClientCutsceneLayerType)` in `AddonContext`
- `getSortedKeyframes`, `isTickCoveredBy`, `getFirstKeyframeTick`, `getLastKeyframeTick` and `isExactTick` in `CutsceneLayer` are now public

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