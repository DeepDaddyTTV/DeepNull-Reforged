<p align="center">
  <img src="./deepnull_media_logo_just_text.png" alt="DeepNull Reforged" width="960">
</p>

A Forge port of DeepNull Reforged `4.2.1` for Minecraft `1.16.5`, based only on the published `v4.2.1` release.

> This branch is the `1.16.5` compatibility line. The original `1.21.1` implementation is retained in `src/main/java` as a release reference; Forge 1.16.5 compiles the API-specific implementation in `src/legacy/java`.

DeepNull Reforged keeps the original fast-access storage concept, then pushes it further with modern automation support, fluid handling, JEI integration, upgrades, configuration, in-game documentation, and a full companion fluid variant called the DampNull.

## Table of Contents

- [About](#about)
- [Project Links](#project-links)
- [Downloads](#downloads)
- [Installation](#installation)
- [What It Does](#what-it-does)
- [1.16.5 Compatibility](#1165-compatibility)
- [Issues and Support](#issues-and-support)
- [Wiki](#wiki)
- [Building](#building)
- [License](#license)
- [Development Note](#development-note)

## About

DeepNull Reforged adds two core storage tools:

- `DeepNull` for large-capacity item storage and direct item use
- `DampNull` for multi-fluid and chemical storage with direct interaction

The mod is built around:

- fast collection and direct use from your hotbar
- strong automation support through NeoForge capabilities
- dock-based integration for item, fluid, energy, and compatible chemical systems
- upgrade-driven customization for storage, filtering, feeding, smelting, compression, transfer behavior, generation, and styling
- companion documentation through both the GitHub wiki and optional GuideME integration

## 1.16.5 Compatibility

The 1.16.5 port includes:

- all released DeepNull, DampNull, panel, dock, workbench, synchronizer, and upgrade registry IDs
- tiered portable item storage with extended integer stack counts, a six-row menu, direct selected-item use, matching-item auto-pickup, and Shift+wheel or middle-click selection
- multi-tank DampNull fluid storage, fluid capability transfer, source pickup/placement, and Sponge Upgrade absorption
- a dock that exposes the inserted Null's item, fluid, or energy capability to Forge-compatible automation
- tier upgrades that preserve stored NBT, energy storage, auto-feeding, auto-smelting, 2x2/3x3 compression, stone/obsidian generation, synchronizer backup/restore, and JEI information/crafting integration
- Java 8 and Forge `36.2.42` compatibility

Minecraft 1.16.5 has no GuideME release, and the 1.21.1 Mekanism chemical, AE2-aware crafting-transfer, workbench styling, and cross-dimensional Ender mirroring implementations do not have direct API equivalents in this branch. Their item IDs and recipes remain present for pack/world compatibility; the standard Forge item/fluid/energy dock capabilities remain available.

## Project Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/deepnull-reforged)
- [Modrinth](https://modrinth.com/mod/deepnull-reforged)
- [GitHub Releases](https://github.com/DeepDaddyTTV/DeepNull-Reforged/releases)
- [GitHub Wiki](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki)
- [Issue Tracker](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues)

## Downloads

Stable builds are published on:

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/deepnull-reforged)
- [Modrinth](https://modrinth.com/mod/deepnull-reforged)
- [GitHub Releases](https://github.com/DeepDaddyTTV/DeepNull-Reforged/releases)

## Installation

1. Install Minecraft `1.16.5`.
2. Install Forge `36.2.42`.
3. Place the downloaded `deepnull-reforged-*.jar` file in your `minecraft/mods/` folder.

## What It Does

### DeepNull

- Adds DeepNulls from Redstone through Creative.
- Each DeepNull stores huge amounts of items across a number of slots based on its tier.
- Stored blocks, items, buckets, food, and other right-click items can be used directly from the DeepNull.
- Stored blocks can be selected through the GUI or with `Shift + Scroll Wheel` and placed directly from the item with a live preview.
- Pick Block support can select matching stored materials from the DeepNull.
- DeepNulls automatically absorb matching items if that item already exists inside.
- JEI crafting transfer can pull ingredients from carried DeepNulls.
- Per-slot behavior includes:
  - extraction rules
  - placement rules
  - tag matching
  - selection
  - slot reordering
  - custom minimum extraction amounts

Tier notes:

- Lower tiers hold smaller amounts per slot.
- Higher tiers dramatically increase capacity.
- Creative-tier Nulls are effectively infinite utility tools.

### DampNull

- Adds DampNulls, a dedicated fluid-storage counterpart to the DeepNull.
- DampNulls store multiple fluids at once in internal tanks.
- Fluids can be selected, placed into the world, and picked back up directly from source blocks.
- DampNulls support transfer with compatible fluid inventories and tanks.
- Optional Mekanism support allows DampNulls to work with compatible chemical storage and transfer.
- DampNulls now support generator-style and utility upgrades that expand them beyond simple tank storage.

### Docking Station

- Adds a Docking Station that can hold either a DeepNull or DampNull.
- Supports standard NeoForge capability-based automation for:
  - item transfer
  - fluid transfer
  - FE transfer
  - chemical transfer when the installed Null supports it
- Works with universal piping and storage systems that respect standard handlers.

### Upgrades

DeepNulls and DampNulls support a growing upgrade system, including:

- Filter Upgrade
- Bucket Upgrade
- Energy Upgrade
- Deep Energy Upgrade
- Chemical Upgrade
- Auto-Feeding Upgrade
- Auto-Smelting Upgrade
- Basic Compression Upgrade
- Advanced Compression Upgrade
- Stone Generator
- Obsidian Generator
- Sponge Upgrade
- Stoneworks Upgrade
- Ender Upgrade

These upgrades allow features like:

- whitelist / blacklist filtering
- using stored buckets and fluid containers directly from a DeepNull
- FE storage and charging
- automatic eating from stored food
- automatic smelting of supported world pickups
- automatic `2x2` and `3x3` compression recipes
- fluid and chemical transfer support
- passive material generation and stone-processing automation
- linked storage mirroring through the Ender Upgrade

### Null Workbench and Synchronizer

- Adds the Null Workbench, a dedicated utility station for crafting, syncing, and styling Nulls.
- Workbench recipes provide balanced alternate progression paths for DeepNull tiers, DampNull tiers, panels, and support items.
- The style tab allows frame and glass recoloring while preserving normal tier defaults until the item is actually customized.
- The sync tab works with the Synchronizer to back up and restore Null configuration.
- The Synchronizer can also be cleared directly, similar to the Ender Upgrade workflow.

### JEI / QoL / Automation

- JEI recipe transfer support for `2x2` and `3x3` crafting
- JEI workbench recipe transfer support for the Null Workbench
- Recipe transfer can use ingredients from carried DeepNulls
- `Shift + Right Click` transfer can push to and pull from compatible inventories and tanks
- Includes configurable transfer locking, hotkeys, and multiple client/common/server config options
- AE2-aware transfer support
- Optional GuideME support for in-game documentation

## Issues and Support

If you find a bug or want to suggest an improvement:

1. Make sure you are testing on the latest available version.
2. Check whether the issue is already reported.
3. Open an issue on the [GitHub issue tracker](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues).
4. Include enough detail to reproduce the problem, including modpack, version, logs, and crash reports when relevant.

## Wiki

The full user-facing documentation is available on the GitHub wiki:

- [Home](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki)
- [DeepNulls](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/DeepNulls)
- [DampNulls](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/DampNulls)
- [Upgrades](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/Upgrades)
- [Items](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/Items)
- [Controls, JEI, and Automation](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/Controls-JEI-and-Automation)
- [Config and Settings](https://github.com/DeepDaddyTTV/DeepNull-Reforged/wiki/Config-and-Settings)

## Building

This branch targets:

- Minecraft `1.16.5`
- Forge `36.2.42`
- Java `8`

Build with:

```bash
./gradlew build
```

The built jar is written to `build/libs/`.

## License

This repository is licensed under the `MIT` license.

That means you are free to:

- use it in modpacks
- redistribute it
- modify it
- fork it

Credit is appreciated, but not required.

## Development Note

This project has been developed with substantial help from ChatGPT Codex, alongside a lot of manual direction, iteration, testing, and cleanup.

I want to be fully open about that so people can decide for themselves whether they want to use it.
