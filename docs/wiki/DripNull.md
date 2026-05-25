# DripNull

DripNull is a tiered loadout Null. It stores player gear profiles inside the DripNull stack and swaps those profiles back onto the player.

## Tiers

| Tier | Item | Profiles |
| --- | --- | --- |
| Redstone | `drip_null_0` | 1 |
| Lapis | `drip_null_1` | 2 |
| Iron | `drip_null_2` | 3 |
| Gold | `drip_null_3` | 4 |
| Diamond | `drip_null_4` | 5 |
| Emerald | `drip_null_5` | 6 |
| Creative | `drip_null_6` | 12 |

Servers can tune the tier counts with `dripNullProfileSlotsByTier`; the menu caps profile pages at 12 profiles.

## Controls

- Hold right-click with a DripNull to charge it.
- Release after the charge completes to capture or swap the selected profile.
- Release early or press Escape to cancel.
- Right-click the DripNull from an inventory slot with an empty cursor to open its menu.
- Use the normal next/previous Null selection controls or `Shift + Scroll` while holding DripNull to cycle profiles.

## Profiles

An empty selected profile captures enabled slot scopes into the shared vault and clears those player slots. A populated selected profile swaps by returning the currently equipped profile to the vault, then equipping the selected profile. If that populated profile is already equipped, using it again stows the live slot contents into the DripNull and clears those player slots.

The Profiles view previews the selected profile's saved item grid. Hover items in that preview to see their normal item tooltips. The Clear button removes the selected profile and moves unshared stored items into Recovery instead of deleting them.

The Settings view controls which scopes the selected profile captures:

- hotbar
- main inventory
- armor
- offhand
- optional Curios scope

DripNull stacks are skipped during capture to prevent recursive storage.

## Recovery

The Recovery view exposes loose stored items as real DripNull slots. You can take individual stacks or shift-move them. The Recover action still extracts all loose displaced items and idle vault items only if every recovered stack can fit in the player inventory.

## Mend Upgrade

The DripNull Mend Upgrade installs from the Recovery view. When installed, XP pickup repairs damaged stored vault or loose recovery items. Held DripNulls are processed first and repair faster than DripNulls sitting elsewhere in inventory.

## Drip Stand

The Drip Stand stores one DripNull. Normal right-click runs the same swap immediately with no charge. Shift-right-click removes the stored DripNull to the player inventory or drops it if the inventory is full.

## Recipes

Base DripNull uses Redstone DeepNull Panels, Leather, and an Armor Stand. Higher craftable tiers use the previous DripNull, the next panel tier, and Leather. DripNull Mend Upgrade uses Upgrade Core, Experience Bottles, Iron Ingots, and Anvils. Drip Stand uses an Armor Stand, DeepNull Docking Station, Iron, and Redstone DeepNull Panels.
