---
navigation:
  title: Controls, JEI, and Automation
  icon: deepnullreforged:deepnull_dock
  position: 5
item_ids:
  - deepnullreforged:deepnull_dock
---

# Controls, JEI, and Automation

This page covers controls, transfer behavior, JEI integration, and docking automation.

## Hotkeys

- open / close a DeepNull or DampNull from the hotbar
- cycle selected slot while held
- toggle transfer lock for the held null

## DeepNull Controls

- `Shift + Scroll`: cycle selected slot
- `Middle Click`: select a matching stored block when possible
- `Alt + Click`: set selected slot in the GUI
- `Alt + Arrow`: move items around in the GUI
- `Ctrl + Click`: cycle extraction mode
- `P + Click`: cycle placement mode

## DampNull Controls

- `Left Click`: select tank
- `Shift + Left Click`: clear tank
- `Shift + Right Click`: transfer with compatible tanks or chemicals when transfer is unlocked

## Transfer Lock

When transfer is unlocked:

- `Shift + Right Click` on a compatible inventory or tank tries to push contents into it
- if nothing can be pushed, it tries to pull matching contents back in
- if you are aiming at a normal block instead of a transfer target, normal placement still works
- if you are not aiming at a reachable block, `Shift + Right Click` opens the GUI

## JEI Integration

JEI crafting transfer can use:

- the player inventory
- carried DeepNull contents

Supported targets include:

- the player `2x2` crafting grid
- vanilla crafting tables
- compatible modded crafting menus with a normal crafting matrix

## Docking Station

The docking station exposes:

- item storage
- fluid storage
- energy storage
- chemical storage when the Chemical Upgrade is installed

That makes it useful with pipes, storage buses, tanks, and other standard NeoForge automation systems.

## Related Pages

- [Config and Settings](config-and-settings.md)
- [Docking Station](docking-station.md)
