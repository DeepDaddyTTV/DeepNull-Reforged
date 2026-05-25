# DenNull

DenNull is a tiered dockable Null that stores captured entities as reusable entries.

## Capturing

Right-click a supported non-player entity with DenNull to capture it. This includes interaction-heavy mobs such as Villagers, Wandering Traders, Llamas, Alpacas, Mules, and Horses; their normal right-click behavior only runs when DenNull capture fails. The target is removed only after the held DenNull stack has stored the capture. Survival captures consume one `Bait` only after the target can be stored and the DenNull has room. Creative players do not consume Bait.

DenNull refuses players, passenger stacks, and entities that cannot be safely serialized. Failed captures leave the entity in the world and do not consume Bait.

## Storage

Each DenNull tier uses the same slot shape as DeepNull. Each unique entry stacks up to the tier's per-slot capacity. Entities with volatile differences like HP, UUID, position, motion, hurt timers, or vanilla random spawn bonus attributes can stack together; meaningful data such as names, variants, villager data, trades, equipment, and owner data remains part of the stored identity.

Sneak-right-click opens the DenNull grid. Hover an entry to see its entity id, count, and preserved-data summary. Press the drop key while hovering an entry to release that specific mob at the player's feet without closing the UI. Hold Shift while hovering an entry to show the fields that make that stack unique, such as type or variant, baby/adult state, owner data, equipment, color, villager data, and preserved identity tags. The normal next/previous Null selection controls and Shift+Scroll cycle which entry is selected while DenNull is held.

The shared Null HUD appears when the selected Den entry changes and uses the existing HUD settings. It shows the selected entity name, entry number, count, entity id, preserved-data summary, and a small entity preview when available.

Captured entries render as small in-game entity previews in the grid. The preview uses the stored representative entity data when possible, and the bottom-right number shows the stored capture count just like an item stack count.

The screen also includes Upgrades and Settings views. Upgrades now draws a proper Null-style player inventory and hotbar slot grid, including locked source slots when opened remotely. Settings hides the normal player inventory so Den buffers and controls do not overlap. See [DenNull Upgrades](DenNull-Upgrades) for Breeding, Clone, Dye, Milk, Shear, Baby, Tag, Capture, Spawner, and Farm behavior.

## Releasing

Normal right-click releases the selected entry for free near the targeted block face or look position. Released entities get a fresh UUID, safe position and motion, and valid health.

## Dock and Remote Access

DenNull can be placed in a DeepNull Dock. Docked DenNulls open their own UI and can be opened remotely through registered dock links. Dock summaries aggregate loaded Den entity and spawner entries by entity id and count.

## Workbench Seed

The Null Workbench Seed tab can write DenNull entity templates. Templates mark target entity slots for future captures and do not create releasable stored entities.
