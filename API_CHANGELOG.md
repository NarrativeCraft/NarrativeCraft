# Changelog

## [3.1.0]

### Features
- Added `singleOK()` return in `InkActionResult` to automatically stop any one shot ink action running in background
- Positional arguments are now parsed either with the name, or single value (e.g. `animation play|animation action:play` )
- `CLIENT_SEVER` side for ink actions, to be both executed on server and client
