# DeepNull Reforged

DeepNull Reforged is a NeoForge `1.21.1` port and expansion of the original DankNull mod by p455w0rd.

It keeps the fast-access storage workflow of the original mod while adding modern automation support, JEI integration, energy features, fluid-focused DampNulls, and new upgrade systems.

## Table of Contents

- [About](#about)
- [Project Links](#project-links)
- [Downloads](#downloads)
- [Installation](#installation)
- [Features](#features)
- [Issues and Support](#issues-and-support)
- [Wiki](#wiki)
- [Building](#building)
- [License](#license)
- [Development Note](#development-note)

## About

DeepNull Reforged adds two core storage tools:

- `DeepNull` for large-capacity item storage and direct item use
- `DampNull` for multi-fluid tank storage and direct fluid interaction

The mod is built around:

- fast collection and direct use from your hotbar
- strong automation support through NeoForge capabilities
- dock-based integration for item, fluid, and energy systems
- upgrade-driven customization for storage, filtering, feeding, smelting, compression, and transfer behavior

## Project Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/deepnull-reforged)
- [Modrinth](https://modrinth.com/mod/deepnull-reforged)
- [GitHub Releases](https://github.com/MMFQDEATH/DeepNull-Reforged/releases)
- [GitHub Wiki](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki)
- [Issue Tracker](https://github.com/MMFQDEATH/DeepNull-Reforged/issues)

## Downloads

Stable builds are published on:

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/deepnull-reforged)
- [Modrinth](https://modrinth.com/mod/deepnull-reforged)
- [GitHub Releases](https://github.com/MMFQDEATH/DeepNull-Reforged/releases)

## Installation

1. Install Minecraft `1.21.1`.
2. Install NeoForge `21.1.219` or newer within the supported `1.21.1` range.
3. Place the downloaded `deepnull-reforged-*.jar` file in your `minecraft/mods/` folder.

## Features

### DeepNull

- Tiered item storage with dedicated per-slot item assignment
- Per-slot extraction and placement rules
- Tag-matching support
- Direct use of stored blocks, food, potions, buckets, and many normal right-click items
- Shift-scroll slot selection and in-hand stored-item rendering

### DampNull

- Tiered fluid tank storage
- Direct source pickup and source placement
- Transfer support with compatible tanks and fluid inventories
- Dedicated tank UI with selection and shift-click clearing

### Docking Station

- Holds one DeepNull or DampNull
- Exposes item, fluid, and energy capabilities to automation mods
- Supports storage networks, pipes, and other standard NeoForge integrations

### Upgrades

Current upgrade systems include:

- Filter Upgrade
- Bucket Upgrade
- Energy Upgrade
- Deep Energy Upgrade
- Auto-Feeding Upgrade
- Auto-Smelting Upgrade
- Basic Compression Upgrade
- Advanced Compression Upgrade

### JEI and Automation

- JEI recipe transfer support for `2x2` and `3x3` crafting
- Recipe transfer can use ingredients from carried DeepNulls
- Shift-right-click transfer support for nearby inventories and tanks
- AE2-aware transfer support

## Issues and Support

If you find a bug or want to suggest an improvement:

1. Make sure you are testing on the latest available version.
2. Check whether the issue is already reported.
3. Open an issue on the [GitHub issue tracker](https://github.com/MMFQDEATH/DeepNull-Reforged/issues).
4. Include enough detail to reproduce the problem, including modpack, version, logs, and crash reports when relevant.

## Wiki

The full user-facing documentation is available on the GitHub wiki:

- [Home](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki)
- [DeepNull and Tiers](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/DeepNull-and-Tiers)
- [DampNull](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/DampNull)
- [Upgrades](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/Upgrades)
- [Controls, JEI, and Automation](https://github.com/MMFQDEATH/DeepNull-Reforged/wiki/Controls,-JEI,-and-Automation)

## Building

This project targets:

- Minecraft `1.21.1`
- NeoForge `21.1.219`
- Java `21`

Build with:

```bash
./gradlew build
```

The built jar is written to `build/libs/`.

## License

This repository is licensed under the `MIT` license.

You are free to use, modify, fork, and redistribute it under the terms of that license.

## Development Note

This project has been developed with substantial help from ChatGPT Codex, with continuous manual testing, review, direction, and iteration during development.
