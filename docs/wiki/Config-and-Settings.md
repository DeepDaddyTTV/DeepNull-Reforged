# Config and Settings

DeepNull Reforged exposes client, common, and server config groups so players and pack authors can tune both quality-of-life behavior and balance.

## Config Scope

- **Client**: visual options, HUD behavior, control preferences, and local integration hints
- **Common**: default values written into newly created Nulls
- **Server**: gameplay toggles, upgrade enable flags, and tier balance values

## Client Settings

| Setting | What It Does |
| --- | --- |
| `showHud` | Enables the held-null HUD overlay. |
| `hudOffsetX` / `hudOffsetY` | Moves the HUD horizontally or vertically on screen. |
| `hudBackgroundOpacity` | Changes HUD background transparency. |
| `hudDisplayMs` | Controls how long the HUD remains visible after changes. |
| `enableShiftScrollSelection` | Allows `Shift + Scroll` to cycle selected DeepNull slots or DampNull tanks. |
| `invertDampNullInteraction` | Makes DampNulls behave like Mekanism tanks: right click opens the GUI, while `Shift + Right Click` handles fluid interaction. |
| `enableUpdateChecker` | Enables the release-check message that links to the mod download page. |
| `showGuideMeHint` | Shows the GuideME tooltip hint when GuideME is installed. |
| `animateDockedNulls` | Enables the dock hover and spin render animation. |
| `showFullDeepNullCounts` | Shows full stored numbers in the DeepNull GUI instead of compact `1.9K` / `2M` style values. |

## Common Defaults

| Setting | What It Does |
| --- | --- |
| `defaultTransferLocked` | Sets the initial transfer-lock state for newly created Nulls. |
| `defaultAutoPickupEnabled` | Sets the starting auto-pickup state for new DeepNulls. |
| `defaultAutoFeedingEnabled` | Sets the starting Auto-Feeding state when that upgrade is installed. |
| `defaultAutoSmeltingEnabled` | Sets the starting Auto-Smelting state when that upgrade is installed. |
| `defaultStoneworksAmount` | Sets the initial Stoneworks monitor value for new DeepNulls. |

## Server Feature Toggles

| Setting | What It Does |
| --- | --- |
| `disableTagMatching` | Turns off dictionary-tag matching for DeepNull filters. |
| `tagBlacklist` | Blocks specific tags from being used for tag matching. |
| `tagWhitelist` | If populated, only listed tags are allowed for tag matching. |
| `enableAutoPickup` | Globally enables or disables automatic pickup into matching DeepNulls. |
| `enableAutoFeeding` | Enables or disables Auto-Feeding behavior. |
| `enableAutoSmelting` | Enables or disables Auto-Smelting behavior. |
| `enableCompression` | Enables or disables Basic and Advanced Compression behavior. |
| `enableStoneworks` | Enables or disables Stoneworks processing. |
| `enableStoneGenerator` | Enables or disables DampNull Stone Generator output. |
| `enableObsidianGenerator` | Enables or disables DampNull Obsidian Generator output. |
| `enableSpongeUpgrade` | Enables or disables the DampNull Sponge Upgrade. |
| `voidFullFluidsOnSponge` | If enabled, the Sponge Upgrade will still absorb and void matching fluids when the matching tank is already full. |
| `enableChemicalStorage` | Enables or disables Mekanism chemical storage and transfer support. |
| `dockGeneratorBufferSize` | Sets the hidden dock output buffer size used by generator DampNulls. |

## Tier Value Arrays

These server settings are read in tier order:

`Redstone, Lapis, Iron, Gold, Diamond, Emerald, Creative`

| Setting | What It Controls |
| --- | --- |
| `itemCapacityByTier` | DeepNull item capacity per slot. |
| `fluidCapacityByTier` | DampNull fluid capacity per tank. |
| `energyCapacityByTier` | FE capacity for the Energy Upgrade. |
| `deepEnergyCapacityByTier` | FE capacity for the Deep Energy Upgrade. |
| `energyTransferByTier` | FE transfer rate for the Energy Upgrade. |
| `deepEnergyTransferByTier` | FE transfer rate for the Deep Energy Upgrade. |
| `dampNullTankCountByTier` | Number of DampNull tanks per tier. |
| `stoneGenerationRateByTier` | Stone Generator output rate per tier. |
| `spongeAbsorbLimitByTier` | Legacy setting kept for compatibility. Sponge now absorbs all visible source blocks in range. |
| `spongeRangeWidthByTier` | Horizontal Sponge Upgrade box size by tier. |
| `spongeRangeHeightByTier` | Vertical Sponge Upgrade box size by tier. |
