---
navigation:
  title: Sponge Upgrade
  icon: deepnullreforged:sponge_upgrade
  parent: upgrades.md
  position: 21
item_ids:
  - deepnullreforged:sponge_upgrade
---

# Sponge Upgrade

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="deepnullreforged:sponge_upgrade" />
</Column>

Lets a DampNull absorb nearby visible source blocks in one action when you right-click near fluids.

## Availability

- DampNull only
- Every tier

## Tier Differences

| Tier | Horizontal Size | Vertical Size |
| --- | ---: | --- |
| Redstone | 6 | 4 |
| Lapis | 8 | 6 |
| Iron | 8 | 6 |
| Gold | 10 | 8 |
| Diamond | 12 | 10 |
| Emerald | 16 | 12 |
| Creative | 16 | 12 |

## Notes

- Scans in a rectangular box centered on the clicked block.
- The box reaches above and below the clicked position.
- Only absorbs visible source blocks that are not blocked by terrain.
- Uses the DampNull's configured tank rules, so it only stores fluids that fit into matching or empty tanks.
- If `voidFullFluidsOnSponge` is enabled, matching fluids aimed at an already full matching tank are still absorbed and voided.
- Absorbs all visible source blocks in range in a single use.

## Crafting

<RecipeFor id="deepnullreforged:sponge_upgrade" fallbackText="See JEI for the current recipe." />
