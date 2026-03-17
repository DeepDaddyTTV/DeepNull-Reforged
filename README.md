# DeepNull Reforged

DeepNull Reforged is a Minecraft 1.21.1 port of the original DankNull mod by p455w0rd. It keeps the core functionality while also updating it to support Minecraft 1.21.1 and Neoforge.

## Current scope

- Multiple DeepNull tiers with increasing slot counts and capacity
- Per-slot extraction mode, placement mode, and tag-matching mode
- Docking station block for automation and network access
- NeoForge item handler exposure for docks and attached automation
- In-hand selected item render and timed HUD selection overlay
- Reordering support inside the GUI with `Alt+Arrow`

## Docking station

The DeepNull Docking Station can hold a DeepNull and expose it as an inventory to compatible pipes and item transfer. Currently supports generic NeoForge pipes and systems such as LaserIO, EnderIO-style transport, and AE2.

## Build

This project targets:

- Minecraft `1.21.1`
- NeoForge `21.1.219`
- Java `21`

Build with:

```bash
./gradlew build
```

The built jar is written to `build/libs/`.

## Notes

- This is currently very early access and not recommended until a final RC is added.
- The runtime mod id is `deepnullreforged`.
- The visible mod and item naming is `DeepNull Reforged` / `DeepNull`.
- This project will eventually be adding functionality to the mod once it's underlying code is finalized.

## Disclaimer

- This mod has taken multiple hours to make, but it is vibe coded with the help of ChatGPT Codex 5.4. I have been monitoring it and contributing as much as possible as well as testing it at every turn, but for those of you completely against vibe coded mods, this is your disclaimer as I want to be fully open about it.
