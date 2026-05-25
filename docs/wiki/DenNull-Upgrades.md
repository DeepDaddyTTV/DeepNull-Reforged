# DenNull Upgrades

DenNull has its own upgrade system. These upgrades are stored in DenNull custom data and do not use `DeepNullInventory`.

Open a DenNull, switch to Upgrades to install upgrade items, then use Settings for the breeding buffer, milk buffer, Name Tag buffer, dye color, Baby toggle, Tag template, Capture filter list, Farm thresholds, and production buffers.

## Upgrades

| Upgrade | Behavior |
| --- | --- |
| Breeding | Consumes valid breeding items from the DenNull buffer first, then player inventory or carried DeepNull storage when held. Successful cycles add babies directly to matching stored animal stacks without spawning parents. |
| Clone | Releasing a stored mob does not decrement its count while at least one capture exists. The recipe uses a Nether Star, four Netherite Blocks, and four DenNull MK VI stacks. |
| Dye | Applies the selected color to released sheep and vanilla collar-color NBT on supported pets. Shear output uses the selected Dye color first, then stored sheep color, then white. |
| Milk | Generates virtual Milk Buckets from stored cows and mooshrooms into a 32-bucket internal buffer. |
| Shear | Periodically generates wool from stored sheep. Held output routes into carried DeepNulls or player inventory; docked output routes to adjacent item handlers. |
| Baby | Releases ageable mobs as babies and attempts to shrink other living entities through the scale attribute. |
| Tag | Names released mobs from the configured template. Default: `{player}'s {type} #{counter}`. Server config controls whether Name Tags are consumed. |
| Capture | While carried and enabled, scans near the player for valid entities, consumes Bait, and captures up to the configured cycle limit. |
| Spawner | Lets a DenNull capture placed spawners. Spawner-mode DenNulls hold only spawner entries, and docked spawner entries tick virtually from the dock without requiring a nearby player. |
| Den Farm | Converts surplus stored populations or docked spawner cycles into virtual loot-table drops. Loot goes into the Den loot buffer first, then dock outputs when available, and farming pauses instead of voiding blocked output. |

## Capture Filters

Capture filters use entity ids such as `minecraft:cow`.

- Off: all otherwise-valid entities are allowed.
- Whitelist: only listed ids are allowed.
- Blacklist: listed ids are blocked.

Capture ignores players, passenger stacks, and tamed pets. If multiple Capture-enabled DenNulls are carried, selected main hand and offhand DenNulls win before inventory slots.

## Spawner Mode

Spawner Upgrade is exclusive with normal captures. A Spawner DenNull cannot mix normal mob entries and spawner entries. Right-click a placed spawner with a Spawner-upgraded DenNull to store its block entity data; the block is removed only after storage succeeds.

When docked, the selected spawner entry ticks from the dock position. The virtual spawner ignores the vanilla player-proximity requirement but preserves stored delay, spawn count, spawn range, max nearby entity limits, normal spawn placement checks, peaceful difficulty blocking, and spawn potentials as stored data permits.

## Farm Behavior

Den Farm has two modes. In normal Den storage it keeps each entry at or above its configured threshold and consumes surplus counts as virtual kills. In Spawner mode it does not release spawned mobs into the world; it rolls the selected spawner entity's loot table and stores the output.

Farm output is never intentionally voided. If the Den loot buffer and dock output destinations cannot accept the generated drops, the cycle pauses and no stored population is decremented.

## Server Config

DenNull automation defaults are intentionally conservative:

| Key | Default |
| --- | --- |
| `denBreedingIntervalTicks` | `1200` |
| `denBreedingMaxBirthsPerCycle` | `8` |
| `denMilkIntervalTicks` | `200` |
| `denMilkMaxBucketsPerCycle` | `8` |
| `denShearIntervalTicks` | `600` |
| `denShearMaxSheepPerCycle` | `16` |
| `denCaptureIntervalTicks` | `20` |
| `denCaptureRadius` | `5.0` |
| `denCaptureMaxEntitiesPerCycle` | `2` |
| `denTagUpgradeConsumesNameTags` | `true` |
| `denFarmIntervalTicks` | `600` |
| `denFarmMaxKillsPerCycle` | `16` |
| `denFarmLootBufferSlots` | `9` |
| `denFarmDefaultThreshold` | `8` |
