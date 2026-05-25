# Synchronizer

The Synchronizer stores a Null configuration snapshot so it can be restored into another compatible Null through the Null Workbench.

## What It Stores

- Filter settings
- Seeder ghost slot reservations
- Upgrade configuration settings
- Transfer and behavior toggles
- Other Null configuration data

It does **not** copy stored items, fluids, chemicals, or energy.

## Use

- Insert a Null in the left input slot and a Synchronizer in the second left slot of the Null Workbench Sync tab.
- Press **Back Up** to store the Null configuration into the Synchronizer.
- Insert a fresh compatible Null plus the stored Synchronizer and press **Restore** to apply the saved settings.
- Both actions take 3 seconds and return the Null plus Synchronizer in the two large output slots on the right.

## Clearing

- Shift-right click in the air with a configured Synchronizer to clear it.
- Place a configured Synchronizer by itself in any crafting grid to clear it.

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
