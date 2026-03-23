# Synchronizer

The Synchronizer stores a Null configuration snapshot so it can be restored into another compatible Null through the Null Workbench.

## What It Stores

- Filter settings
- Upgrade configuration settings
- Transfer and behavior toggles
- Other Null configuration data

It does **not** copy stored items, fluids, chemicals, or energy.

## Crafting

Pattern:

```text
 G
 F
 U
```

- `G`: gold ingot
- `F`: any Null frame
- `U`: Upgrade Core
