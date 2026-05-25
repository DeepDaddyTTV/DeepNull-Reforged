---
navigation:
  title: DenNull
  icon: deepnullreforged:den_null_0
  position: 3
---

# DenNull

<ItemImage id="deepnullreforged:den_null_0" />

DenNull stores captured non-player entities as tiered entries.

## Capture

Right-click a supported entity with DenNull to capture it. This also applies to interaction-heavy mobs such as Villagers, Wandering Traders, Llamas, Alpacas, Mules, and Horses; their normal interaction only runs when DenNull capture fails. The entity is removed only after the held DenNull stack has stored the capture. Survival captures consume one <ItemLink id="deepnullreforged:bait" /> after the target is accepted and capacity is available. Creative players do not consume Bait.

DenNull refuses players, passenger stacks, and entities that cannot be safely serialized. Failed captures leave the entity in the world and do not consume Bait.

## Release and UI

Sneak-right-click opens the DenNull grid. Normal right-click releases the selected entry for free near the targeted block face or look position. While the grid is open, hover an entry and press your drop key to release that specific mob at your feet without closing the screen. The existing next/previous Null selection controls and Shift+Scroll cycle the selected Den entry while DenNull is held.

The shared Null HUD appears when the selected Den entry changes and uses the same HUD settings as DeepNull. It shows the selected entity, entry number, count, entity id, summary, and a small preview when available.

Entries stack when only volatile runtime data differs, such as HP, UUID, position, motion, rotation, and hurt timers. Meaningful data such as names, variants, villager data, trades, equipment, and owner data remains part of the stored identity.

Captured entries render as small in-game entity previews in the grid. The preview uses the stored representative entity data when possible, and the bottom-right number shows the stored capture count just like an item stack count. Hover an entry to see its display name, entity id, count, and preserved-data summary.

The DenNull screen also has Upgrades and Settings views. The Upgrades tab uses a Null-style inventory grid and typed upgrade placeholders so player inventory slots and locked source slots are readable. See [DenNull Upgrades](dennull-upgrades.md) for Breeding, Clone, Dye, Milk, Shear, Baby, Tag, and Capture behavior.

## Dock and Remote Access

DenNull can be placed in a DeepNull Dock and opened remotely through registered dock links. Dock summaries include loaded Den entity and spawner counts.

## Workbench Seed

The Null Workbench Seed tab can write DenNull entity templates. Templates mark target entity slots for future captures and do not create releasable stored entities.
