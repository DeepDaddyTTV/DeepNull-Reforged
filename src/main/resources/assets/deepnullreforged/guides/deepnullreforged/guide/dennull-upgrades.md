---
navigation:
  title: DenNull Upgrades
  icon: deepnullreforged:den_capture_upgrade
  position: 35
---

# DenNull Upgrades

DenNull upgrades install in the DenNull Upgrades view. Use Settings to manage breeding items, Milk Buckets, Name Tags, dye color, baby release, tag naming, Capture filters, Farm thresholds, and production buffers.

## Production Upgrades

- <ItemLink id="deepnullreforged:den_breeding_upgrade" /> consumes breeding food from the DenNull buffer first, then carried inventory or carried DeepNull storage, and adds babies directly to matching animal stacks.
- <ItemLink id="deepnullreforged:den_milk_upgrade" /> creates virtual Milk Buckets from stored cows and mooshrooms into a 32-bucket internal buffer.
- <ItemLink id="deepnullreforged:den_shear_upgrade" /> creates wool from stored sheep. Dye settings determine wool color before the stored sheep color fallback.
- <ItemLink id="deepnullreforged:den_farm_upgrade" /> turns surplus Den populations or docked spawner cycles into loot-table drops. If the loot buffer and dock outputs are full, farming pauses instead of voiding output.

## Release Upgrades

- <ItemLink id="deepnullreforged:den_clone_upgrade" /> makes releases free while at least one matching capture remains stored.
- <ItemLink id="deepnullreforged:den_dye_upgrade" /> dyes released sheep and vanilla collar-color NBT on supported pets with the selected color.
- <ItemLink id="deepnullreforged:den_baby_upgrade" /> releases ageable mobs as babies and attempts to shrink other living entities.
- <ItemLink id="deepnullreforged:den_tag_upgrade" /> names released mobs with the configured template. The default is `{player}'s {type} #{counter}`.

## Capture Upgrade

<ItemLink id="deepnullreforged:den_capture_upgrade" /> scans around the player while carried and enabled. It consumes Bait like manual capture, skips players, passenger stacks, and tamed pets, and uses the configured entity-id filter.

Filter modes are Off, Whitelist, and Blacklist. Entries use ids such as `minecraft:cow`.

## Spawner Upgrade

<ItemLink id="deepnullreforged:den_spawner_upgrade" /> lets a DenNull capture placed spawners. Spawner DenNulls are exclusive: they hold spawner entries or normal captures, not both.

When docked, the selected spawner entry runs virtually from the dock and does not need a nearby player. Den Farm changes that path into virtual loot generation instead of real mob release.
