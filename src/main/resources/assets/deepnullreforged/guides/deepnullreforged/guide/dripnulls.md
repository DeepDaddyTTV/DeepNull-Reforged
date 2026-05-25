---
navigation:
  title: DripNull
  icon: deepnullreforged:drip_null_0
  position: 4
item_ids:
  - deepnullreforged:drip_null_0
  - deepnullreforged:drip_null_1
  - deepnullreforged:drip_null_2
  - deepnullreforged:drip_null_3
  - deepnullreforged:drip_null_4
  - deepnullreforged:drip_null_5
  - deepnullreforged:drip_null_6
  - deepnullreforged:drip_mend_upgrade
  - deepnullreforged:drip_stand
---

# DripNull

<Column alignItems="center" fullWidth={true}>
  <ItemGrid>
    <ItemIcon id="deepnullreforged:drip_null_0" />
    <ItemIcon id="deepnullreforged:drip_null_1" />
    <ItemIcon id="deepnullreforged:drip_null_2" />
    <ItemIcon id="deepnullreforged:drip_null_3" />
    <ItemIcon id="deepnullreforged:drip_null_4" />
    <ItemIcon id="deepnullreforged:drip_null_5" />
    <ItemIcon id="deepnullreforged:drip_null_6" />
  </ItemGrid>
</Column>

DripNulls save and swap player loadouts. A profile stores references to real items in the DripNull vault, so the same tool or armor piece can belong to multiple profiles without being copied.

## Tiers

| Tier | Profiles |
| --- | ---: |
| Redstone | 1 |
| Lapis | 2 |
| Iron | 3 |
| Gold | 4 |
| Diamond | 5 |
| Emerald | 6 |
| Creative | 12 |

Servers can tune the profile counts with `dripNullProfileSlotsByTier`, up to 12 profiles.

## Controls

- Hold right-click with a DripNull to charge it.
- Release after a full charge to capture or swap the selected profile.
- Release early or press Escape to cancel the charge.
- Right-click the DripNull from an inventory slot with an empty cursor to open Profiles, Settings, and Recovery.
- Use the normal next/previous Null selection controls or `Shift + Scroll` while holding DripNull to cycle profiles.

## Profiles and Recovery

An empty selected profile captures enabled scopes into the shared vault and clears those player slots. A populated selected profile swaps by returning the currently equipped profile to the vault first, then equipping the selected profile. If that populated profile is already equipped, using it again stows the live slot contents back into DripNull and clears those player slots.

The Profiles view previews the selected profile's saved item grid with normal hover tooltips. Clear removes the selected profile and moves unshared stored items to Recovery.

The Settings view controls hotbar, main inventory, armor, offhand, and optional Curios scopes. DripNull stacks are skipped during capture.

Recovery exposes loose stored items as real DripNull slots, so you can take individual stacks or shift-move them. Recover still moves all loose displaced items and idle vault items back to the player only when every recovered stack can fit.

## Mend Upgrade

<ItemImage id="deepnullreforged:drip_mend_upgrade" />

The DripNull Mend Upgrade installs from the Recovery view. When installed, XP pickup repairs damaged stored vault or loose recovery items. Held DripNulls are processed first and repair faster than DripNulls elsewhere in inventory.

## Drip Stand

<ItemImage id="deepnullreforged:drip_stand" />

The Drip Stand stores one DripNull. Normal right-click runs the same swap immediately without charge time. Shift-right-click removes the stored DripNull to the player inventory or drops it if the inventory is full.

<RecipeFor id="deepnullreforged:drip_null_0" fallbackText="See JEI for the current DripNull recipe." />
<RecipeFor id="deepnullreforged:drip_mend_upgrade" fallbackText="See JEI for the DripNull Mend Upgrade recipe." />
<RecipeFor id="deepnullreforged:drip_stand" fallbackText="See JEI for the Drip Stand recipe." />
