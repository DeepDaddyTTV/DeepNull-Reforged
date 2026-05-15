# DeepNull Reforged 4.2.1

Hotfix release for Minecraft `26.1` and `26.1.2` on NeoForge.

## Fixes

- Fixed DeepNull stored stacks being exposed incorrectly to external item automation, which could cause some mods to collapse very large stored stacks down to a single normal stack during round-trip updates.
- Fixed tossed-item pickup delay so it only applies when a DeepNull could actually absorb that item, instead of affecting unrelated tossed items globally.
- Fixed Crafting Tweaks compatibility across the newer API variants so the DeepNull clear-grid integration no longer fails during initialization on affected setups.
- Fixed DeepNull Dock block capability handling for external automation so mods like Pipez can interact with the dock more reliably.
- Fixed docked generator-buffer extraction through the block capability path used by external automation.
- Fixed dock fluid and energy capability state to stay synchronized across side-based automation queries, addressing the stale-state path behind reported fluid duplication behavior with AE2-style setups.

## Compatibility

- Kept support for both Minecraft `26.1` and `26.1.2`.
- Added stronger regression coverage for tossed-item pickup delay, visible DeepNull item capability behavior, dock capability state, and automation extraction paths.

## Notes

- This release is focused on regression fixes and automation compatibility rather than feature changes.
