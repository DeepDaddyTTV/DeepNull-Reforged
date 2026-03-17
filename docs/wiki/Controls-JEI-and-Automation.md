# Controls, JEI, and Automation

This page covers how to use DeepNull Reforged in normal gameplay, crafting, and automation setups.

## Hotkeys

DeepNull Reforged includes configurable keybinds.

### Open / Close DeepNull in Hotbar

Opens a DeepNull or DampNull from the hotbar. If one of the DeepNull Reforged screens is already open, the same key closes it.

### Cycle Selected Slot

Cycle forward or backward through the selected DeepNull slot while the item is held.

### Toggle Transfer Lock

Toggles whether shift-right-click transfer with block inventories and tanks is enabled for the held null.

## Mouse and GUI Controls

### DeepNull

- `Shift + Scroll`: cycle selected slot while held
- `Middle Click`: select a matching stored block when possible
- `Alt + Click`: set selected slot in the GUI
- `Alt + Arrow`: move slot positions in the GUI
- `Ctrl + Click`: cycle extraction mode
- `P + Click`: cycle placement mode

### DampNull

- `Left Click`: select a tank
- `Shift + Left Click`: clear a tank

## Transfer Lock

Transfer Lock controls whether shift-right-click block transfer is active.

When transfer is unlocked:

- `Shift + Right Click` on a compatible inventory will try to push stored contents into it
- if nothing can be pushed, it will try to pull matching contents back into the null
- if you are aiming at a normal block instead of a valid transfer target, the selected block or fluid still places normally
- if you are not aiming at a reachable block, `Shift + Right Click` opens the GUI

This works for:

- DeepNull item storage
- DampNull fluid storage

The transfer is designed to respect the container’s own stack and tank rules.

## JEI Integration

DeepNull Reforged integrates with JEI in several ways.

### Recipe Display

JEI can show recipe and informational integration for DeepNull content, the dock, and supported upgrades.

### Crafting Transfer

JEI recipe transfer can pull ingredients from:

- the player inventory
- carried DeepNulls

Supported crafting transfer targets include:

- the player `2x2` crafting grid
- vanilla crafting tables
- compatible modded crafting menus that expose a normal crafting matrix

JEI transfer can source ingredients from:

- the player inventory
- DeepNull contents carried by the player

## Docking Station

The Docking Station holds one DeepNull or DampNull and exposes its storage to automation.

Supported capability types include:

- item storage
- fluid storage
- energy storage

This makes the dock useful with:

- pipes
- storage buses
- automation blocks
- networked storage systems

## Using Nulls with Other Mods

DeepNull Reforged is designed to cooperate with other mods through standard NeoForge capabilities wherever possible.

That includes:

- item automation
- fluid automation
- FE transfer
- JEI recipe transfer

Common examples include:

- vanilla and modded chests/barrels
- Functional Storage inventories
- AE2 interfaces and compatible storage access blocks

## Update Notifications

The client can notify players when a newer version is available and provide a CurseForge link for downloads.

## Related Pages

- [Home](./Home.md)
- [DeepNull and Tiers](./DeepNull-and-Tiers.md)
- [DampNull](./DampNull.md)
- [Upgrades](./Upgrades.md)
