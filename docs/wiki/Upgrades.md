# Upgrades

Upgrades add special behaviors to DeepNulls. They are installed through the upgrade interface.

![DeepNull upgrades](images/upgrades.png)

## Upgrade Overview

| Upgrade | What It Does | Availability |
| --- | --- | --- |
| Filter Upgrade | Adds ghost-slot filtering and presets | Iron and above |
| Bucket Upgrade | Enables stored bucket and fluid-container use from item slots | All tiers |
| Energy Upgrade | Adds FE storage and charging support | Diamond and above |
| Deep Energy Upgrade | Boosts Emerald FE storage and transfer | Emerald only |
| Auto-Feeding Upgrade | Automatically eats stored food when needed | All tiers |
| Auto-Smelting Upgrade | Smelts incoming supported pickups when a matching item already exists | All tiers |
| Basic Compression Upgrade | Crafts 2x2 compression recipes automatically | All tiers |
| Advanced Compression Upgrade | Crafts 3x3 compression recipes automatically | All tiers |

## Filter Upgrade

The Filter Upgrade adds a ghost-slot filter and mode selector.

Supported filter modes:

- Whitelist
- Blacklist
- Miner
- Herbalist
- Hunter
- Network Engineer
- Builder

## Bucket Upgrade

The Bucket Upgrade lets a DeepNull use stored buckets and compatible fluid containers directly from normal item slots.

This is separate from DampNull, which is the dedicated fluid-storage item.

## Energy Upgrade

The Energy Upgrade adds Forge Energy storage and transfer.

Once installed, a DeepNull can:

- store FE
- charge items in the player inventory when charging is enabled
- expose energy through the docking station

### Energy Capacity

| Tier | FE Capacity | FE Transfer |
| --- | ---: | ---: |
| Diamond | 100,000 FE | 2,000 FE/t |
| Emerald | 1,000,000 FE | 20,000 FE/t |
| Creative | Effectively infinite | Effectively infinite |

## Deep Energy Upgrade

Deep Energy Upgrade is an Emerald-only enhancement that requires the base Energy Upgrade.

It increases Emerald DeepNulls to:

- 25,000,000 FE capacity
- 100,000 FE/t transfer

## Auto-Feeding Upgrade

Auto-Feeding automatically consumes stored food when the player has hunger.

It uses the real stored item instead of generating free hunger, so consumables are actually consumed unless the item itself has special behavior that preserves it. It will also store excess saturation from the consumables.

## Auto-Smelting Upgrade

Auto-Smelting converts incoming pickups into their smelted result when:

- the item has a valid smelting recipe
- the DeepNull already contains either the raw form or the smelted result
- the item comes from world pickup rather than from transfer or dock movement

This is intended for things like:

- chunk or dust style inputs into ingots
- cobblestone into stone
- logs into charcoal

Ore blocks are intentionally excluded.

### Auto-Smelting Filter

Auto-Smelting has its own dedicated filter screen.

It supports only:

- Whitelist
- Blacklist

This lets you control which world-picked items are allowed to be auto-smelted without affecting your normal item filter behavior.

## Compression Upgrades

Compression upgrades use real crafting recipes, including datapack and mod recipes where applicable.

### Basic Compression Upgrade

Supports self-recipes that fill a `2x2` grid.

Example:

- string into wool

### Advanced Compression Upgrade

Supports self-recipes that fill a `3x3` grid.

Example:

- wheat into hay bales

Compression only happens if the resulting item is already stored in the DeepNull.

## Related Pages

- [DeepNull and Tiers](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/DeepNull-and-Tiers)
- [Controls, JEI, and Automation](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/Controls,-JEI,-and-Automation)
