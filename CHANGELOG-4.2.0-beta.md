# DeepNull Reforged 4.2.0-beta

Beta release for Minecraft `26.1` on NeoForge, updated to maintain parity with the current `1.21.1` branch and to support the `26.1.2` game line.

## What’s New

- Synced the `26.1` branch forward with the newer `1.21.1` updates through the `4.2.0` line.
- Added compatibility for Minecraft `26.1.2` while keeping support for the existing `26.1` branch line.
- Expanded automated testing and CI coverage for the 26.1 branch so regression checks run closer to the current main NeoForge line.

## DeepNull and DampNull Improvements

- Added the global auto-pickup toggle from the newer branch, including persistent player state and client hotkey support.
- Prevented newly tossed items from being immediately re-absorbed by auto-pickup by adding the newer pickup-delay behavior.
- Added the `Connections` filter preset mode for connection-style items such as pipes, cables, and similar transport components.
- Updated DampNull fluid handling so gas-only configurations reject normal fluid insertion the same way they do on the newer branch.
- Added right-click fluid container transfer support for docked DampNulls.
- Updated dock automation extraction so default keep-one behavior and explicit keep amounts match the newer branch logic.
- Increased supported stoneworks amount handling to the newer full-range behavior instead of the older small cap.
- Synced the newer transfer-from-target matching behavior so only actually accepted items are extracted from connected inventories.

## Config and Data Fixes

- Brought over the newer DampNull default fluid-capacity values.
- Added migration handling for older server configs so legacy DampNull fluid-capacity defaults are corrected automatically.
- Added the newer Crafting Tweaks return-integration config toggle to match the current main branch behavior.
- Updated guide and wiki docs for the new filter mode and config behavior.

## UI and Controls

- Added the newer GUI input mapping behavior for primary, secondary, and tertiary DeepNull actions.
- Added the new global auto-pickup keybind and in-game feedback messages.
- Updated DeepNull, DampNull, and Null Workbench input handling so screen actions follow the newer branch behavior more closely.
- Increased the stoneworks amount text entry limit to support larger values cleanly.

## Compatibility

- Kept JEI crafting transfer behavior aligned with the newer branch fixes, including better validation when transfers cannot be completed.
- Synced the newer AE2 transfer behavior changes from the current `1.21.1` branch.
- Kept Crafting Tweaks integration aligned with the current config-driven behavior.
- Expanded the accepted Minecraft version range so this build works on both `26.1` and `26.1.2`.

## Testing and CI

- Added regression coverage for global auto-pickup, thrown-item delay, DampNull fluid restrictions, dock fluid transfer, dock automation extraction, stoneworks limits, and filter matching.
- Updated the GitHub Actions workflow to run the current 26.1 build and GameTest path with the new parity changes.
- Added a GameTest config cleanup step so regression runs start from a clean config state.

## Beta Notes

- This is still a beta release for the NeoForge `26.1` line.
- The mod now accepts the `26.1.x` line, including `26.1.2`, without dropping `26.1` support.
- Optional companion-mod behavior still depends on matching `26.1` releases of those mods being available.
