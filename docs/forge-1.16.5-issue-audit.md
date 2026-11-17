# Forge 1.16.5 open-issue audit

Reviewed the 14 open issues in `DeepDaddyTTV/DeepNull-Reforged` on 2026-11-17 against the compiled `src/legacy/java` implementation. The retained modern source is not the runtime for this branch. The all-tier inventory alignment fix and optional-mod refmap configuration remain intact.

## Findings

| Issue | Legacy disposition and evidence |
| --- | --- |
| [#22](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/22) mass dropping | **Related defect fixed.** The legacy menu already limited drops to 64, so the reported 500,000-item dump was not reproduced. It incorrectly dropped 64 pearls/tools and accepted invalid buttons. Q now drops one item; Ctrl+Q uses the item's actual stack limit; invalid buttons and an occupied cursor do not drop storage. Tests cover 500,000 stored items, 64/16/1 stack limits and creative storage. |
| [#25](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/25) mined dock loses Null | **Not reproduced; covered.** The existing removal callback extracts and drops the stored Null before removing the tile. A regression invokes that callback after tile NBT save/load for both item and fluid Nulls, preserving 500,000 contents and custom tags, with exactly one drop even if removal is repeated. No dock production change was needed. |
| [#14](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/14) destructive tank transfer | **Related legacy defects reproduced and fixed.** Separate cached/direct fluid handlers overwrote each other's changes. Forge's generic bucket helper also discarded the filled copy for creative players while draining the real tank, and interaction ran on the client. The held stack now uses one canonical capability; tank transfer acts on live storage on the server. Tests cover survival/creative players, client non-mutation, filling/emptying, partial capacity, full tanks and tagged fluids. The reporter's exact EvilCraft/EnderIO setup remains unverified. |
| [#24](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/24) fluid packing | **Independent legacy overflow defect fixed.** Fabric's source/flowing-fluid adapter is absent here. Legacy storage filled a matching tank correctly but refused the 17th bucket after its first 16,000 mB tank filled. Fill now continues into additional matching/empty tanks; drain aggregates the same tagged fluid across tanks. Simulation and capacity boundaries are covered. |
| [#19](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/19) contents lost after restart | **Related legacy data-loss defect fixed.** A cached item capability could overwrite newer GUI contents, and capabilities ignored replaced stack NBT. Canonical handlers plus tag refresh preserve interleaved changes. ItemStack save/load tests preserve extended counts; fluid storage gets equivalent coverage. This is not an end-to-end reproduction of the reporter's 1.21.1 server restart. |
| [#15](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/15) upgraded MK IV rejects items | **Not reproduced in tested legacy path.** A Gold/MK IV Null with both compression upgrades can withdraw/reinsert a normal stack and pick up a matching stack. The pickup fixture has no matching compression recipe; recipe-specific modpack behavior is unverified. |
| [#23](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/23) global GUI mouse bindings | **Reported mechanism absent.** Legacy screen handling uses mouse events directly and registers no global GUI mouse keybindings. |
| [#18](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/18) JEI crafting interaction | **Reported integration absent.** The legacy JEI plugin supplies information and a recipe catalyst; it has no modern crafting-transfer interception. No new configuration feature added. |
| [#17](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/17) Just Dire Things/construction sticks | **Exact compatibility scenario not run.** Report concerns ATM 11 / 26.1.2. Legacy has no equivalent global integration hook; direct stored-item use exists. No unsupported cross-version fix was copied. |
| [#16](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/16) Fabric charging | **Different backend.** Legacy exposes Forge Energy through `DeepNullEnergyStorage`; the report concerns Fabric 26.1.2 generators/cables. Those integrations were not tested. |
| [#12](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/12) Crafting Tweaks reflection failure | **Reported code absent.** No legacy Crafting Tweaks reflection bridge is compiled. |
| [#11](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/11) Ender mirroring | **Existing compatibility limitation.** Cross-dimensional mirroring is not implemented in the legacy port, as documented in the README. This audit does not add it. |
| [#7](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/7) workbench placement | **Reported override absent.** The legacy workbench extends the vanilla crafting table and has no custom `canSurvive` rejection. |
| [#5](https://github.com/DeepDaddyTTV/DeepNull-Reforged/issues/5) docked clay production | **Feature request.** No new neighboring-dock production feature added. |

## Regression execution

`./gradlew build runData` runs the seven texture/layout unit cases, existing Forge recipe/menu/persistence validation, and 13 grouped open-issue regression cases. The issue cases use real Forge-loaded item/menu/capability implementations, with mocked player/world/tank surroundings where required. They are not a graphical client test or a running-server restart test.

`runData` includes the development-only `regression` source set and writes `build/reports/legacy-issues.txt` only after every case passes. Gradle removes the old marker first and fails if a new marker is absent. Mockito and the issue harness are excluded from the released mod JAR. Detailed case results appear in `run-data/logs/latest.log`.

The existing CI matrix executes the same checks in five configurations:

All five configurations passed locally on 2026-11-17, with all 13 issue groups passing in each run and seven layout unit cases passing. The built mod JAR was inspected and contains neither the issue harness nor Mockito classes. GitHub Actions remains the authoritative remote result for each commit.

| Configuration | JEI | Mekanism | AE2 |
| --- | --- | --- | --- |
| base | false | false | false |
| jei | true | false | false |
| mekanism | false | true | false |
| ae2-mekanism | false | true | true |
| all-integrations | true | true | true |

Pass each value as `-PincludeJeiRuntime=...`, `-PincludeMekanismRuntime=...`, and `-PincludeAe2Runtime=...`. Java 8 and Forge 36.2.42 are used. Optional-mod startup passing does not certify every machine or screen in those mods. No release publication or issue comments/closures are part of this audit.
