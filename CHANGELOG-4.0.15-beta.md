# DeepNull Reforged 4.0.15-beta

Beta release for Minecraft `26.1` on NeoForge `26.1.0.5-beta`.

## Major Porting Work

- Ported DeepNull Reforged from the 1.21.1 NeoForge line to Minecraft `26.1`.
- Updated registration and bootstrap paths for the current NeoForge 26.1 beta lifecycle.
- Reworked item/resource definitions for the 26.1 item model format so DeepNull, DampNull, panels, docks, and related items load correctly.
- Added the 26.1 replacement path for DeepNull selected-item rendering so stored items, blocks, fluids, and chemicals render inside the container again.
- Preserved the existing mod line on the new game version as a beta release instead of a separate long-term branch rewrite.

## Restored Feature Parity

- Restored the DeepNull Dock renderer so inserted DeepNulls and DampNulls display on the dock again.
- Restored dock interaction behavior so stored containers can be removed from the dock correctly.
- Restored DeepNull and DampNull side-tab navigation, including upgrades, transfer lock, charging, info, and related panel access.
- Restored the larger 1.21.1-style Null Workbench layouts instead of the reduced interim 26.1 screens.
- Restored the Null Workbench craft, sync, and style tab flows with the expected large-slot previews and interaction zones.
- Restored the DeepNull and DampNull upgrade-tab access that had dropped out during the early 26.1 UI pass.
- Restored the custom selected-item visual layer inside DeepNulls so held blocks and other stored contents display again.
- Restored more of the old stoneworks and extraction management behavior in the DeepNull UI.

## UI and UX Fixes

- Removed duplicate or incorrect controls that appeared during the port, including the extra top lock button on the main DeepNull screen.
- Corrected the Null Workbench style screen so it no longer shows stray `Frame`, `Glass`, or inventory text that was not part of the intended layout.
- Re-expanded the Null Workbench styling screen to support the expected preview and modifier workflow.
- Fixed DeepNull/DampNull inventory rendering scale and transform issues that made stored models appear too large in hand or in the UI.
- Fixed middle-click behavior in the DeepNull screen so blocked actions do not leak through to vanilla pick-block.
- Fixed the stoneworks amount field and other text-entry widgets so they correctly consume keyboard input in 26.1.
- Reduced redundant packet spam from custom extraction amount editing and aligned local state updates with the hotfix behavior.

## Rendering Fixes

- Fixed missing textures caused by the 26.1 item-definition resource format change.
- Fixed DeepNull and DampNull base item models so they use the proper textured model chain again.
- Corrected DeepNull in-hand and inventory transforms after the early port produced oversized or misaligned renders.
- Fixed rendering of stored blocks inside DeepNulls by restoring the nested selected-item model path.
- Tuned nested item transforms so stored block renders fit inside the DeepNull shell instead of filling the screen.
- Fixed DampNull tank contents rendering to use the correct opaque tint handling for fluids and chemicals.
- Fixed model/resource references that were still pointing at outdated wrapper models or missing panel resources.

## Data and Stability Fixes

- Fixed register-time crashes caused by 26.1 expecting block and item IDs to be assigned earlier during property construction.
- Hardened DeepNull inventory decode paths so unreadable stored item, fluid, or chemical entries are skipped and logged instead of crashing load.
- Improved sanitation of empty or invalid custom extraction state during load and editing.
- Carried over recent hotfix behavior for extraction-state cleanup and UI interaction edge cases.

## Integrations and Compatibility

- Restored the JEI transfer and category bridge needed for the Null Workbench and DeepNull workflows.
- Restored AE2 transfer compatibility wiring for the updated 26.1 codebase.
- Kept Mekanism client-side chemical rendering support in the 26.1 client path.
- Cleaned up dev-runtime integration dependencies that were tied to older 26.1 pre/snapshot builds and blocked clean standalone beta boot.

## Beta Notes

- This is a beta build targeted at NeoForge `26.1.0.5-beta`.
- The release is intended to restore feature equivalence with the 1.21.1 NeoForge line while the 26.1 ecosystem is still settling.
- Optional companion-mod integration quality still depends on matching 26.1 builds of those mods being available.
