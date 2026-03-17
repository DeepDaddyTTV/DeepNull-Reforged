# DeepNull and Tiers

This page covers how the main item-storage DeepNulls work.

## What a DeepNull Does

A DeepNull stores items in internal slots. Each slot is dedicated to one item type at a time and can hold far more than a normal stack.

DeepNulls are designed to:

- collect matching items automatically
- let you select a slot and use that stored item directly
- respect per-slot extraction and placement rules
- work in-hand, in GUIs, and through the docking station

## Tier Progression

| Tier | Slots | Capacity Per Slot | Total Item Capacity |
| --- | ---: | ---: | ---: |
| Redstone | 9 | 128 | 1,152 |
| Lapis | 18 | 512 | 9,216 |
| Iron | 27 | 1,152 | 31,104 |
| Gold | 36 | 2,048 | 73,728 |
| Diamond | 45 | 3,200 | 144,000 |
| Emerald | 54 | 2,147,483,647 | Effectively unlimited |
| Creative | 54 | 2,147,483,647 | Effectively unlimited |

## Slot Rules

Each DeepNull item slot has its own behavior.

### Extraction Mode

Controls how many items are allowed to be taken out by automation or direct extraction.

### Placement Mode

Controls how many items are allowed to be placed into the world from the selected slot.

### Tag Matching

When enabled on a slot that supports dictionary-style tags, compatible matching items can stack into that slot even if they are not the exact same item instance.

## Selected Slot

One stored slot is considered the selected slot. That selected item is what the DeepNull uses for:

- direct placement
- direct right-click use
- block selection workflows
- rendered held-item display

## Direct Item Use

Stored items can do more than just sit in storage. DeepNulls can use supported stored items directly, including:

- blocks
- buckets and many fluid containers
- food
- drinkables and potion-like consumables
- other normal right-click items that work through standard item use

With the right upgrades installed, a DeepNull can also:

- auto-feed the player with stored food
- auto-smelt supported world pickups
- auto-compress supported `2x2` and `3x3` self-recipes

## Creative Tier

Creative DeepNulls support locking and effectively infinite storage behavior. They are intended as the top-end utility tier rather than a normal progression step.

## Related Pages

- [Upgrades](./Upgrades.md)
- [Controls, JEI, and Automation](./Controls-JEI-and-Automation.md)
- [DampNull](./DampNull.md)
