# DampNull

DampNull is the fluid-focused counterpart to the DeepNull.

Instead of storing items by slot, it stores fluids by tank.

## What a DampNull Does

A DampNull can:

- hold multiple fluids at once
- place source fluids into the world
- pick up source fluids directly from the world
- transfer fluids through the docking station
- work with compatible fluid automation from other mods

## Tank Counts by Tier

| Tier | Tanks |
| --- | ---: |
| Redstone | 9 |
| Lapis | 9 |
| Iron | 9 |
| Gold | 18 |
| Diamond | 18 |
| Emerald | 18 |
| Creative | 18 |

## Per-Tank Capacity

| Tier | Capacity Per Tank |
| --- | ---: |
| Redstone | 8,000 mB |
| Lapis | 16,000 mB |
| Iron | 32,000 mB |
| Gold | 64,000 mB |
| Diamond | 128,000 mB |
| Emerald | 256,000 mB |
| Creative | Effectively infinite |

## Using a DampNull in the World

When you select a tank, the DampNull can interact with source fluids in a bucket-like way:

- place stored fluid into the world
- pick up source fluids from the world
- work with compatible modded fluids through NeoForge fluid handling
- shift-right-click compatible tanks or storage blocks to transfer fluids in or out when transfer is unlocked

## GUI Behavior

The DampNull screen is tank-based rather than slot-based.

- left-click a tank to select it
- shift-click a tank to clear it
- hover tanks to see fluid details

Creative DampNull tanks intentionally display as half full while still behaving as effectively infinite.

## Crafting and Progression

DampNulls can be crafted directly or upgraded from previous DampNull tiers, depending on the recipe path you choose.

They are intended to be the dedicated fluid-storage tool, rather than a secondary mode on the normal DeepNull.

## Docking and Automation

Docked DampNulls expose fluid capability to compatible mods, so they can be used as compact fluid buffers in automation setups.

## Related Pages

- [DeepNull and Tiers](./DeepNull-and-Tiers.md)
- [Upgrades](./Upgrades.md)
- [Controls, JEI, and Automation](./Controls-JEI-and-Automation.md)
