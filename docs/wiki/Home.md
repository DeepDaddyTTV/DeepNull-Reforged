# DeepNull Reforged Wiki

DeepNull Reforged is a NeoForge 1.21.1 port and expansion of the classic `/dev/null`-style storage tool. It adds two storage families:

- `DeepNull` for item storage
- `DampNull` for fluid storage

Both are items that can he held in your inventory and used to both store and directly use their contents.

![DeepNull Reforged overview](images/home.gif)

## Contents

- [DeepNull and Tiers](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/DeepNull-and-Tiers)
- [DampNull](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/DampNull)
- [Upgrades](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/Upgrades)
- [Controls, JEI, and Automation](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/Controls,-JEI,-and-Automation)

## At a Glance

### DeepNull

- Stores very large amounts of items by slot
- Supports per-slot extract and place rules
- Can use stored buckets, food, potions, and many normal right-click items directly
- Can auto-feed, auto-smelt, and auto-compress with upgrades
- Works with JEI crafting transfer and automation mods
- Can push to and pull from nearby inventories with shift-right-click when transfer is unlocked

### DampNull

- Stores fluids by tank instead of storing buckets as items
- Can place and pick up source fluids directly in the world
- Can transfer fluids through the docking station and compatible automation mods
- Uses dedicated tank UIs and supports tank clearing from the GUI
- Can also push to and pull from nearby tanks with shift-right-click when transfer is unlocked

### Docking Station

- Holds one DeepNull or DampNull
- Exposes item, fluid, and energy capabilities to compatible mods
- Renders the stored null hovering above the dock
- Supports item, fluid, and energy automation through standard NeoForge capabilities

## Design Goals

DeepNull Reforged is built around three ideas:

- Keep your inventory clean
- Let storage stay useful in active gameplay, not just in menus
- Revive and improve on a personal favorite mod

## Update Notifications

Client builds can show an update message when a newer release is detected. The message links to the CurseForge project page for downloads.

## Notes

- Creative tiers are intentionally special-case tiers with effectively infinite behavior.
- Some advanced behaviors depend on upgrades being installed.
- JEI integration is intended to be broad, including recipe transfer from inventory and carried DeepNulls.
