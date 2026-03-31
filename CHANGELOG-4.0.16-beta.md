# DeepNull Reforged 4.0.16-beta

Follow-up beta release for Minecraft `26.1` on NeoForge `26.1.0.5-beta`.

## Ported 1.21.1 Hotfixes

- Ported the latest `1.21.1` regression fixes forward into the `26.1` branch.
- Brought the 26.1 line back in sync with the newer `4.0.16` behavior on the main NeoForge branch where that logic still applies cleanly.

## Gameplay and Behavior Fixes

- Added a new server config option, `voidFullItemsOnPickup`, for DeepNull auto-pickup overflow handling.
- Fixed auto-pickup so matching items can still be absorbed and voided when the matching DeepNull slot is already full, instead of being rejected.
- Restored stored single-item interaction proxying for non-block, non-bucket items so tools and similar stateful items behave more like the current `1.21.1` branch again.

## UI Fixes

- Fixed DeepNull info panels to show the actual tiered DeepNull item name instead of the generic mod item group title.
- Fixed DampNull info panels to show the actual tiered DampNull item name instead of the generic mod item group title.

## Compatibility

- Added Inventory Sorter compatibility IMC so DeepNull storage/container slots are blacklisted correctly.

## Docs

- Updated config documentation for the new `voidFullItemsOnPickup` server option in both the wiki docs and the in-game guide docs.

## Beta Notes

- This remains a beta build targeted at NeoForge `26.1.0.5-beta`.
- Companion-mod compatibility still depends on matching 26.1-side releases being available.
