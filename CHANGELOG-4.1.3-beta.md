# DeepNull Reforged 4.1.3-beta

Cumulative changelog for the Minecraft `26.1` / NeoForge `26.1` branch since `4.0.16-beta`.

## Branch Sync and Platform Updates

- Synced the remaining `1.21.1` gameplay, UI, transfer, and integration changes through the `4.1.3` line into the `26.1` branch.
- Updated the 26.1 beta line to NeoForge `26.1.0.8-beta`.
- Updated the JEI-side 26.1 integration/runtime target to the current published `26.1` line.

## DeepNull and DampNull Behavior

- Added the newer transfer output modes to DeepNulls and DampNulls: transfer all, transfer matching, and locked.
- Added transfer direction controls so held/container transfers can be limited to insert, extract, or omnidirectional behavior.
- Added DampNull sponge upgrade toggling and hotkey support.
- Updated held shift-transfer behavior to respect the newer output-mode and transfer-direction rules for both item and fluid storage.
- Synced newer overflow, extraction, and stoneworks behavior from the later `1.21.1` line, including the follow-up stoneworks fallback fixes from `4.1.1`.

## Crafting, JEI, and Compatibility

- Reworked JEI crafting transfer handling so DeepNull-backed crafting is more reliable on `26.1`.
- Fixed recipe transfer return behavior so borrowed crafting ingredients can be returned to the preferred DeepNull after transfer, cancel, or container close.
- Fixed crafting transfer logic to respect actual extractable amounts and custom extraction limits instead of assuming all matching stored items are available.
- Restored the newer carried-DeepNull JEI registration behavior from the `4.1.2` / `4.1.3` line.
- Added Crafting Tweaks clear-grid compatibility for DeepNull-backed crafting grids.
- Synced the newer AE2-side transfer compatibility behavior that was already present in the updated `1.21.1` line.

## UI and UX

- Added updated client messaging for transfer output mode, transfer direction, and DampNull-specific controls.
- Added the DampNull upgrades screen title and updated hotkey labels for the newer transfer/sponge actions.
- Improved upgrade-screen wording so the supported item/tier text matches the newer `1.21.1` behavior.
- Improved custom extraction editing to support the newer workflow, including applying the same custom minimum across occupied slots.

## Data and 26.1 Compatibility

- Updated DeepNull recipe data to the `26.1` ingredient format so the recipes load correctly on the current NeoForge beta.
- Added the missing empty GameTest structure used by the regression suite.
- Fixed the vanilla mineable tag path to the `26.1` data layout.

## Testing and CI

- Ported the `1.21.1` GitHub Actions workflow shape to `26.1` instead of using a single basic build-only job.
- GitHub Actions now runs the base `26.1` build plus the regression GameTest server for this branch.
- Added the `26.1` regression GameTest suite coverage for core DeepNull behavior, Null Workbench behavior, crafting transfer regressions, AE2 compatibility, and Mekanism compatibility.
- Added unit-test coverage for the newer UI text and compatibility registration behavior.
- Added an optional-integration CI lane for JEI + AE2 on `26.1`.
- That optional runtime lane now skips the game boot step automatically when the published AE2 / GuideME artifacts still target `26.1-snapshot-*` instead of the released `26.1` game version, so CI stays truthful instead of reporting a false DeepNull failure.
