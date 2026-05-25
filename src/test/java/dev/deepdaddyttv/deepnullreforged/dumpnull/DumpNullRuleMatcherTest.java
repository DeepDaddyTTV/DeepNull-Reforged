package dev.deepdaddyttv.deepnullreforged.dumpnull;

import io.netty.buffer.Unpooled;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import dev.deepdaddyttv.deepnullreforged.client.DumpNullScreen;
import dev.deepdaddyttv.deepnullreforged.network.DumpNullPayloads;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DumpNullRuleMatcherTest {
    @Test
    void presetOptionsAndTargetsRoundTripThroughMenuPayloadCodec() {
        List<DumpNullPresetOption> options = List.of(
                new DumpNullPresetOption("vanilla", "dn.dumpnull.preset.vanilla", DumpNullPresetOption.TARGET_NONE),
                new DumpNullPresetOption("biome", "dn.dumpnull.preset.biome", DumpNullPresetOption.TARGET_BIOME)
        );
        List<DumpNullPresetTarget> targets = List.of(
                new DumpNullPresetTarget(ResourceLocation.withDefaultNamespace("plains"), "minecraft: plains"),
                new DumpNullPresetTarget(ResourceLocation.withDefaultNamespace("the_nether"), "minecraft: the nether")
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullPresetOption.writeList(buffer, options);
        DumpNullPresetTarget.writeList(buffer, targets);

        assertEquals(options, DumpNullPresetOption.readList(buffer));
        assertEquals(targets, DumpNullPresetTarget.readList(buffer));
    }

    @Test
    void applyPresetReplacesPresetRulesAndPreservesManualRules() {
        DumpNullRule oldPreset = DumpNullRule.presetGenerated(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("bow"), "old");
        DumpNullRule manual = DumpNullRule.basic(DumpNullRuleAction.PASS, ResourceLocation.withDefaultNamespace("trident"), 80, List.of(), List.of());
        DumpNullRule newPreset = DumpNullRule.presetGenerated(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("iron_helmet"), "new");
        DumpNullData data = new DumpNullData(List.of(ResourceLocation.withDefaultNamespace("drowned")), List.of(oldPreset, manual));

        DumpNullData updated = data.applyPreset(new DumpNullPresetResult(
                "new",
                List.of(ResourceLocation.withDefaultNamespace("zombie")),
                List.of(newPreset)
        ));

        assertEquals(List.of(ResourceLocation.withDefaultNamespace("zombie")), updated.selectedMobs());
        assertEquals(List.of(manual, newPreset), updated.rules());
        assertFalse(updated.rules().contains(oldPreset));
        assertTrue(updated.rules().get(1).isPresetGenerated());
    }

    @Test
    void presetAcceptedRulesAreOptInAndExcludeDiscardedItems() {
        ResourceLocation bone = ResourceLocation.withDefaultNamespace("bone");
        ResourceLocation bow = ResourceLocation.withDefaultNamespace("bow");
        DumpNullRule oldPreset = DumpNullRule.presetGenerated(DumpNullRuleAction.PASS, ResourceLocation.withDefaultNamespace("arrow"), "old");
        DumpNullRule manual = DumpNullRule.basic(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("rotten_flesh"), 0, List.of(), List.of());
        DumpNullPresetResult preset = new DumpNullPresetResult(
                "hostile",
                List.of(ResourceLocation.withDefaultNamespace("skeleton")),
                List.of(DumpNullRule.presetGenerated(DumpNullRuleAction.VOID, bow, "hostile"))
        );
        DumpNullData data = new DumpNullData(List.of(), List.of(oldPreset, manual));

        DumpNullData visualOnly = data.applyPreset(preset);
        DumpNullData writeAccepted = data.applyPreset(DumpNullPresetCatalog.withAcceptedRules(preset, List.of(bone, bow)));

        assertEquals(List.of(manual, preset.presetRules().getFirst()), visualOnly.rules());
        assertEquals(DumpNullItemStatus.NEUTRAL, visualOnly.statusFor(bone));
        assertEquals(DumpNullItemStatus.ACCEPTED, writeAccepted.statusFor(bone));
        assertEquals(DumpNullItemStatus.DISCARDED, writeAccepted.statusFor(bow));
        assertTrue(writeAccepted.rules().stream().filter(DumpNullRule::isPresetGenerated).allMatch(rule -> !rule.itemId().equals(ResourceLocation.withDefaultNamespace("arrow"))));
    }

    @Test
    void applyPresetPayloadRoundTripsPresetAndTargetIds() {
        DumpNullPayloads.ApplyPresetPayload original = new DumpNullPayloads.ApplyPresetPayload(
                "biome",
                List.of("minecraft:plains", "minecraft:desert"),
                true
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullPayloads.ApplyPresetPayload.STREAM_CODEC.encode(buffer, original);
        DumpNullPayloads.ApplyPresetPayload loaded = DumpNullPayloads.ApplyPresetPayload.STREAM_CODEC.decode(buffer);

        assertEquals(original, loaded);
    }

    @Test
    void mobOptionsRoundTripThroughMenuPayloadCodec() {
        List<DumpNullMobOption> original = List.of(
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("zombie"), "entity.minecraft.zombie", "MONSTER", true),
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("cow"), "entity.minecraft.cow", "CREATURE", false)
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullMobOption.writeList(buffer, original);
        List<DumpNullMobOption> loaded = DumpNullMobOption.readList(buffer);

        assertEquals(original, loaded);
    }

    @Test
    void mobFiltersClassifyHostilePeacefulBossSelectedAndLoadedDrops() {
        DumpNullMobOption zombie = new DumpNullMobOption(ResourceLocation.withDefaultNamespace("zombie"), "entity.minecraft.zombie", "MONSTER", false);
        DumpNullMobOption cow = new DumpNullMobOption(ResourceLocation.withDefaultNamespace("cow"), "entity.minecraft.cow", "CREATURE", true);
        DumpNullMobOption wither = new DumpNullMobOption(ResourceLocation.withDefaultNamespace("wither"), "entity.minecraft.wither", "MONSTER", false);

        assertEquals(DumpNullMobFilter.HOSTILE, DumpNullMobFilter.toggleSingle(null, DumpNullMobFilter.HOSTILE));
        assertEquals(DumpNullMobFilter.PEACEFUL, DumpNullMobFilter.toggleSingle(DumpNullMobFilter.HOSTILE, DumpNullMobFilter.PEACEFUL));
        assertEquals(null, DumpNullMobFilter.toggleSingle(DumpNullMobFilter.PEACEFUL, DumpNullMobFilter.PEACEFUL));
        assertTrue(DumpNullMobFilter.HOSTILE.matches(zombie, true, true));
        assertFalse(DumpNullMobFilter.HOSTILE.matches(cow, true, true));
        assertTrue(DumpNullMobFilter.PEACEFUL.matches(cow, true, true));
        assertFalse(DumpNullMobFilter.PEACEFUL.matches(zombie, true, true));
        assertTrue(DumpNullMobFilter.BOSS.matches(wither, true, true));
        assertFalse(DumpNullMobFilter.BOSS.matches(zombie, true, true));
        assertTrue(DumpNullMobFilter.SELECTED.matches(cow, true, true));
        assertFalse(DumpNullMobFilter.SELECTED.matches(zombie, true, true));
        assertTrue(DumpNullMobFilter.HAS_DROPS.matches(zombie, false, false));
        assertTrue(DumpNullMobFilter.HAS_DROPS.matches(zombie, true, true));
        assertFalse(DumpNullMobFilter.HAS_DROPS.matches(zombie, true, false));
    }

    @Test
    void catalogGroupingOrdersMinecraftFirstAndCategoriesByPopulation() {
        List<DumpNullMobOption> options = List.of(
                new DumpNullMobOption(ResourceLocation.fromNamespaceAndPath("zeta_mobs", "brute"), "entity.zeta_mobs.brute", "MONSTER", false),
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("cow"), "entity.minecraft.cow", "CREATURE", false),
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("sheep"), "entity.minecraft.sheep", "CREATURE", false),
                new DumpNullMobOption(ResourceLocation.fromNamespaceAndPath("alpha_mobs", "fish"), "entity.alpha_mobs.fish", "WATER_AMBIENT", false),
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("zombie"), "entity.minecraft.zombie", "MONSTER", false)
        );

        assertEquals(List.of("minecraft", "alpha_mobs", "zeta_mobs"), DumpNullScreen.catalogNamespaceOrder(options));
        assertEquals(List.of("CREATURE", "MONSTER"), DumpNullScreen.catalogCategoryOrder(options, "minecraft"));
    }

    @Test
    void catalogCategoryOrderingUsesAlphabeticalTieBreaks() {
        List<DumpNullMobOption> options = List.of(
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("bat"), "entity.minecraft.bat", "AMBIENT", false),
                new DumpNullMobOption(ResourceLocation.withDefaultNamespace("zombie"), "entity.minecraft.zombie", "MONSTER", false)
        );

        assertEquals(List.of("AMBIENT", "MONSTER"), DumpNullScreen.catalogCategoryOrder(options, "minecraft"));
    }

    @Test
    void copyListFormatterEmitsOneDisplayNameAndIdPerLine() {
        String text = DumpNullStatusCopyFormatter.format(List.of(
                new DumpNullStatusCopyFormatter.Entry("Blaze Rod", ResourceLocation.withDefaultNamespace("blaze_rod")),
                new DumpNullStatusCopyFormatter.Entry("Rotten\nFlesh", ResourceLocation.withDefaultNamespace("rotten_flesh"))
        ));

        assertEquals(
                "Blaze Rod - minecraft:blaze_rod" + System.lineSeparator() + "Rotten Flesh - minecraft:rotten_flesh",
                text
        );
    }

    @Test
    void dropCandidatesRoundTripThroughMenuPayloadCodec() {
        List<DumpNullDropCandidate> original = List.of(
                new DumpNullDropCandidate(ResourceLocation.withDefaultNamespace("zombie"), ResourceLocation.withDefaultNamespace("rotten_flesh"), "loot", 65, 128),
                new DumpNullDropCandidate(ResourceLocation.withDefaultNamespace("drowned"), ResourceLocation.withDefaultNamespace("trident"), "equipment", 7, 192)
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullDropCandidate.writeList(buffer, original);
        List<DumpNullDropCandidate> loaded = DumpNullDropCandidate.readList(buffer);

        assertEquals(original, loaded);
    }

    @Test
    void itemStatusSummariesAndMobDropRowsRoundTripThroughPayloadCodecs() {
        List<DumpNullItemStatusSummary> summaries = List.of(
                new DumpNullItemStatusSummary(ResourceLocation.withDefaultNamespace("bone"), DumpNullItemStatus.ACCEPTED, false, false),
                new DumpNullItemStatusSummary(ResourceLocation.withDefaultNamespace("bow"), DumpNullItemStatus.DISCARDED, true, true)
        );
        List<DumpNullMobDropRow> rows = List.of(new DumpNullMobDropRow(
                ResourceLocation.withDefaultNamespace("zombie"),
                List.of(new DumpNullDropCandidate(ResourceLocation.withDefaultNamespace("zombie"), ResourceLocation.withDefaultNamespace("rotten_flesh"), "loot", 64, 128))
        ));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullItemStatusSummary.writeList(buffer, summaries);
        DumpNullMobDropRow.writeList(buffer, rows);

        assertEquals(summaries, DumpNullItemStatusSummary.readList(buffer));
        assertEquals(rows, DumpNullMobDropRow.readList(buffer));
    }

    @Test
    void lazyUiPayloadsRoundTripStatusAndFilterData() {
        DumpNullPayloads.RequestMobDropsPayload request = new DumpNullPayloads.RequestMobDropsPayload(
                4,
                List.of("minecraft:zombie", "minecraft:drowned")
        );
        DumpNullPayloads.MobDropsPayload response = new DumpNullPayloads.MobDropsPayload(
                4,
                List.of(new DumpNullMobDropRow(ResourceLocation.withDefaultNamespace("drowned"), List.of()))
        );
        DumpNullPayloads.SearchMobDropsPayload searchRequest = new DumpNullPayloads.SearchMobDropsPayload(4, "rotten flesh");
        DumpNullPayloads.MobDropSearchPayload searchResponse = new DumpNullPayloads.MobDropSearchPayload(
                4,
                "rotten flesh",
                List.of("minecraft:zombie"),
                List.of(new DumpNullMobDropRow(
                        ResourceLocation.withDefaultNamespace("zombie"),
                        List.of(new DumpNullDropCandidate(ResourceLocation.withDefaultNamespace("zombie"), ResourceLocation.withDefaultNamespace("rotten_flesh"), "loot", 64, 128))
                ))
        );
        DumpNullPayloads.SetItemStatusPayload status = new DumpNullPayloads.SetItemStatusPayload(
                "minecraft:bone",
                DumpNullItemStatus.ACCEPTED.ordinal()
        );
        DumpNullPayloads.SetItemStatusesPayload statuses = new DumpNullPayloads.SetItemStatusesPayload(
                List.of("minecraft:bone", "minecraft:arrow"),
                DumpNullItemStatus.DISCARDED.ordinal()
        );
        DumpNullPayloads.SaveItemFilterPayload filter = new DumpNullPayloads.SaveItemFilterPayload(
                "minecraft:trident",
                DumpNullItemStatus.ACCEPTED.ordinal(),
                80,
                List.of("minecraft:loyalty"),
                List.of("minecraft:vanishing_curse")
        );
        DumpNullPayloads.ClearItemFilterPayload clear = new DumpNullPayloads.ClearItemFilterPayload("minecraft:trident");
        DumpNullPayloads.ClearItemFiltersPayload clearMany = new DumpNullPayloads.ClearItemFiltersPayload(List.of("minecraft:bone", "minecraft:bow"));
        DumpNullData data = new DumpNullData(
                List.of(ResourceLocation.withDefaultNamespace("drowned")),
                List.of(DumpNullRule.basic(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("rotten_flesh"), 0, List.of(), List.of()))
        );
        List<DumpNullMobOption> mobOptions = List.of(new DumpNullMobOption(ResourceLocation.withDefaultNamespace("drowned"), "entity.minecraft.drowned", "MONSTER", true));
        List<DumpNullItemStatusSummary> stateSummaries = data.itemStatusSummaries();
        DumpNullPayloads.DumpNullStatePayload state = new DumpNullPayloads.DumpNullStatePayload(
                4,
                data,
                mobOptions,
                stateSummaries
        );
        DumpNullPayloads.RequestPresetPreviewPayload previewRequest = new DumpNullPayloads.RequestPresetPreviewPayload(
                4,
                "biome",
                List.of("minecraft:plains")
        );
        DumpNullPayloads.PresetPreviewPayload preview = new DumpNullPayloads.PresetPreviewPayload(
                4,
                "biome",
                List.of("minecraft:plains"),
                List.of("minecraft:cow"),
                List.of(new DumpNullItemStatusSummary(ResourceLocation.withDefaultNamespace("beef"), DumpNullItemStatus.ACCEPTED, false, false)),
                stateSummaries
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullPayloads.RequestMobDropsPayload.STREAM_CODEC.encode(buffer, request);
        DumpNullPayloads.MobDropsPayload.STREAM_CODEC.encode(buffer, response);
        DumpNullPayloads.SearchMobDropsPayload.STREAM_CODEC.encode(buffer, searchRequest);
        DumpNullPayloads.MobDropSearchPayload.STREAM_CODEC.encode(buffer, searchResponse);
        DumpNullPayloads.DumpNullStatePayload.STREAM_CODEC.encode(buffer, state);
        DumpNullPayloads.RequestPresetPreviewPayload.STREAM_CODEC.encode(buffer, previewRequest);
        DumpNullPayloads.PresetPreviewPayload.STREAM_CODEC.encode(buffer, preview);
        DumpNullPayloads.SetItemStatusPayload.STREAM_CODEC.encode(buffer, status);
        DumpNullPayloads.SetItemStatusesPayload.STREAM_CODEC.encode(buffer, statuses);
        DumpNullPayloads.SaveItemFilterPayload.STREAM_CODEC.encode(buffer, filter);
        DumpNullPayloads.ClearItemFilterPayload.STREAM_CODEC.encode(buffer, clear);
        DumpNullPayloads.ClearItemFiltersPayload.STREAM_CODEC.encode(buffer, clearMany);

        assertEquals(request, DumpNullPayloads.RequestMobDropsPayload.STREAM_CODEC.decode(buffer));
        assertEquals(response, DumpNullPayloads.MobDropsPayload.STREAM_CODEC.decode(buffer));
        assertEquals(searchRequest, DumpNullPayloads.SearchMobDropsPayload.STREAM_CODEC.decode(buffer));
        assertEquals(searchResponse, DumpNullPayloads.MobDropSearchPayload.STREAM_CODEC.decode(buffer));
        assertEquals(state, DumpNullPayloads.DumpNullStatePayload.STREAM_CODEC.decode(buffer));
        assertEquals(previewRequest, DumpNullPayloads.RequestPresetPreviewPayload.STREAM_CODEC.decode(buffer));
        assertEquals(preview, DumpNullPayloads.PresetPreviewPayload.STREAM_CODEC.decode(buffer));
        assertEquals(status, DumpNullPayloads.SetItemStatusPayload.STREAM_CODEC.decode(buffer));
        assertEquals(statuses, DumpNullPayloads.SetItemStatusesPayload.STREAM_CODEC.decode(buffer));
        assertEquals(filter, DumpNullPayloads.SaveItemFilterPayload.STREAM_CODEC.decode(buffer));
        assertEquals(clear, DumpNullPayloads.ClearItemFilterPayload.STREAM_CODEC.decode(buffer));
        assertEquals(clearMany, DumpNullPayloads.ClearItemFiltersPayload.STREAM_CODEC.decode(buffer));
    }

    @Test
    void automationPayloadsRoundTrip() {
        DumpNullPayloads.SetHeldDiscardModePayload held = new DumpNullPayloads.SetHeldDiscardModePayload(DumpNullHeldDiscardMode.GENERATE_FE.ordinal());
        DumpNullPayloads.SetDockDiscardModePayload dock = new DumpNullPayloads.SetDockDiscardModePayload(DumpNullDockDiscardMode.EXPORT_DISCARD_LANE.ordinal());
        DumpNullPayloads.SetSideModePayload side = new DumpNullPayloads.SetSideModePayload(Direction.EAST.get3DDataValue(), DumpNullSideMode.OUTPUT.ordinal());
        DumpNullPayloads.SetDiscardExportSidePayload discardSide = new DumpNullPayloads.SetDiscardExportSidePayload(Direction.NORTH.get3DDataValue(), true);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullPayloads.SetHeldDiscardModePayload.STREAM_CODEC.encode(buffer, held);
        DumpNullPayloads.SetDockDiscardModePayload.STREAM_CODEC.encode(buffer, dock);
        DumpNullPayloads.SetSideModePayload.STREAM_CODEC.encode(buffer, side);
        DumpNullPayloads.SetDiscardExportSidePayload.STREAM_CODEC.encode(buffer, discardSide);
        DumpNullPayloads.InstallPowerUpgradePayload.STREAM_CODEC.encode(buffer, DumpNullPayloads.InstallPowerUpgradePayload.INSTANCE);

        assertEquals(held, DumpNullPayloads.SetHeldDiscardModePayload.STREAM_CODEC.decode(buffer));
        assertEquals(dock, DumpNullPayloads.SetDockDiscardModePayload.STREAM_CODEC.decode(buffer));
        assertEquals(side, DumpNullPayloads.SetSideModePayload.STREAM_CODEC.decode(buffer));
        assertEquals(discardSide, DumpNullPayloads.SetDiscardExportSidePayload.STREAM_CODEC.decode(buffer));
        assertEquals(DumpNullPayloads.InstallPowerUpgradePayload.INSTANCE, DumpNullPayloads.InstallPowerUpgradePayload.STREAM_CODEC.decode(buffer));
    }

    @Test
    void itemCatalogPayloadsRoundTrip() {
        DumpNullPayloads.RequestItemCatalogPayload request = new DumpNullPayloads.RequestItemCatalogPayload(4, DumpNullItemCatalogView.ORES.id());
        DumpNullPayloads.ItemCatalogPayload response = new DumpNullPayloads.ItemCatalogPayload(
                4,
                DumpNullItemCatalogView.ORES.id(),
                List.of(
                        new DumpNullItemCatalogEntry(ResourceLocation.withDefaultNamespace("raw_iron"), "Raw Iron", "vanilla"),
                        new DumpNullItemCatalogEntry(ResourceLocation.withDefaultNamespace("iron_ore"), "Iron Ore", "tag/path")
                )
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullPayloads.RequestItemCatalogPayload.STREAM_CODEC.encode(buffer, request);
        DumpNullPayloads.ItemCatalogPayload.STREAM_CODEC.encode(buffer, response);

        assertEquals(request, DumpNullPayloads.RequestItemCatalogPayload.STREAM_CODEC.decode(buffer));
        assertEquals(response, DumpNullPayloads.ItemCatalogPayload.STREAM_CODEC.decode(buffer));
    }

    @Test
    void itemCatalogPriorityOrdersSeededVanillaEntriesFirst() {
        assertTrue(DumpNullItemCatalog.priorityIndex(DumpNullItemCatalogView.ORES, ResourceLocation.withDefaultNamespace("raw_iron"))
                < DumpNullItemCatalog.priorityIndex(DumpNullItemCatalogView.ORES, ResourceLocation.withDefaultNamespace("iron_ore")));
        assertTrue(DumpNullItemCatalog.priorityIndex(DumpNullItemCatalogView.BUILDING, ResourceLocation.withDefaultNamespace("oak_log"))
                < DumpNullItemCatalog.priorityIndex(DumpNullItemCatalogView.BUILDING, ResourceLocation.withDefaultNamespace("terracotta")));
        assertEquals(Integer.MAX_VALUE, DumpNullItemCatalog.priorityIndex(DumpNullItemCatalogView.QUARRY, ResourceLocation.fromNamespaceAndPath("modded", "cobblestone")));
    }

    @Test
    void dropSearchMatchesItemIdsPathsAndDisplayNames() {
        ResourceLocation rottenFlesh = ResourceLocation.withDefaultNamespace("rotten_flesh");
        ResourceLocation blazeRod = ResourceLocation.withDefaultNamespace("blaze_rod");

        assertTrue(DumpNullDropSearch.matches(rottenFlesh, "minecraft:rotten_flesh"));
        assertTrue(DumpNullDropSearch.matches(rottenFlesh, "rotten flesh"));
        assertTrue(DumpNullDropSearch.matches(blazeRod, "Blaze Rod", "Blaze Rod"));
        assertFalse(DumpNullDropSearch.matches(blazeRod, "bone", "Blaze Rod"));
    }

    @Test
    void statusGridUsesFourColumnsAtNormalDumpNullPanelWidths() {
        assertEquals(4, DumpNullScreen.statusGridColumnsForWidth(120));
        assertEquals(2, DumpNullScreen.statusGridColumnsForWidth(88));
    }

    @Test
    void automationViewHasOneRowPerDirection() {
        assertEquals(Direction.values().length, DumpNullScreen.automationSideRows());
    }

    @Test
    void scrollbarMathClampsTrackClicksAndHandleDrags() {
        assertEquals(50, DumpNullScreen.scrollbarHandleHeight(100, 100));
        assertEquals(35, DumpNullScreen.scrollbarHandleY(10, 100, 50, 100));
        assertEquals(0, DumpNullScreen.scrollbarScrollForTrackClick(10, 10, 100, 100));
        assertEquals(100, DumpNullScreen.scrollbarScrollForTrackClick(110, 10, 100, 100));
        assertEquals(50, DumpNullScreen.scrollbarScrollForHandleTop(25, 100, 100));
    }

    @Test
    void allDropTileAppearsOnlyForMobsWithMultipleDrops() {
        assertFalse(DumpNullScreen.shouldShowAllDropTile(0));
        assertFalse(DumpNullScreen.shouldShowAllDropTile(1));
        assertTrue(DumpNullScreen.shouldShowAllDropTile(2));
        assertTrue(DumpNullScreen.shouldShowAllDropTile(12));
    }

    @Test
    void catalogRightClickTargetDiscardsUnlessAlreadyDiscarded() {
        assertEquals(DumpNullItemStatus.DISCARDED, DumpNullItemStatus.NEUTRAL.catalogRightClickTarget());
        assertEquals(DumpNullItemStatus.DISCARDED, DumpNullItemStatus.ACCEPTED.catalogRightClickTarget());
        assertEquals(DumpNullItemStatus.ACCEPTED, DumpNullItemStatus.DISCARDED.catalogRightClickTarget());
    }

    @Test
    void withMobAddsOnceAndRemoves() {
        ResourceLocation drowned = ResourceLocation.withDefaultNamespace("drowned");

        DumpNullData selected = DumpNullData.EMPTY.withMob(drowned, true).withMob(drowned, true);
        DumpNullData removed = selected.withMob(drowned, false);

        assertEquals(List.of(drowned), selected.selectedMobs());
        assertEquals(List.of(), removed.selectedMobs());
    }

    @Test
    void dumpNullDataPreservesStaleMobIdsForCompatibility() {
        ResourceLocation staleModdedMob = ResourceLocation.fromNamespaceAndPath("missing_mod", "old_mob");
        DumpNullData original = DumpNullData.EMPTY.withMob(staleModdedMob, true);

        DumpNullData loaded = DumpNullData.load(original.save());

        assertEquals(List.of(staleModdedMob), loaded.selectedMobs());
    }

    @Test
    void removeRuleRemovesByIndex() {
        DumpNullData data = new DumpNullData(List.of(), List.of(
                DumpNullRule.basic(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("rotten_flesh"), 0, List.of(), List.of()),
                DumpNullRule.basic(DumpNullRuleAction.PASS, ResourceLocation.withDefaultNamespace("bone"), 0, List.of(), List.of())
        ));

        DumpNullData updated = data.removeRule(0);

        assertEquals(1, updated.rules().size());
        assertEquals(ResourceLocation.withDefaultNamespace("bone"), updated.rules().getFirst().itemId());
    }

    @Test
    void moveRuleReordersRulesAndIgnoresInvalidMoves() {
        DumpNullRule rottenFlesh = DumpNullRule.basic(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("rotten_flesh"), 0, List.of(), List.of());
        DumpNullRule bone = DumpNullRule.basic(DumpNullRuleAction.PASS, ResourceLocation.withDefaultNamespace("bone"), 0, List.of(), List.of());
        DumpNullData data = new DumpNullData(List.of(), List.of(rottenFlesh, bone));

        DumpNullData moved = data.moveRule(1, -1);
        DumpNullData invalid = moved.moveRule(0, -1);

        assertEquals(List.of(bone, rottenFlesh), moved.rules());
        assertEquals(moved, invalid);
    }

    @Test
    void itemStatusCycleWritesAndRemovesOnlySimpleRules() {
        ResourceLocation bone = ResourceLocation.withDefaultNamespace("bone");

        DumpNullData accepted = DumpNullData.EMPTY.withItemStatus(bone, DumpNullItemStatus.ACCEPTED);
        DumpNullData discarded = accepted.withItemStatus(bone, DumpNullItemStatus.DISCARDED);
        DumpNullData neutral = discarded.withItemStatus(bone, DumpNullItemStatus.NEUTRAL);

        assertEquals(DumpNullRuleAction.PASS, accepted.rules().getFirst().action());
        assertEquals(DumpNullRuleAction.VOID, discarded.rules().getFirst().action());
        assertEquals(List.of(), neutral.rules());
    }

    @Test
    void advancedRulesArePreservedWhenSimpleStatusChanges() {
        ResourceLocation trident = ResourceLocation.withDefaultNamespace("trident");
        DumpNullRule advanced = DumpNullRule.basic(
                DumpNullRuleAction.PASS,
                trident,
                80,
                List.of(ResourceLocation.withDefaultNamespace("loyalty")),
                List.of()
        );
        DumpNullRule fallback = DumpNullRule.basic(DumpNullRuleAction.VOID, trident, 0, List.of(), List.of());
        DumpNullData data = new DumpNullData(List.of(), List.of(advanced, fallback));

        DumpNullData updated = data.withItemStatus(trident, DumpNullItemStatus.ACCEPTED);

        assertEquals(2, updated.rules().size());
        assertEquals(advanced, updated.rules().getFirst());
        assertEquals(DumpNullRuleAction.PASS, updated.rules().get(1).action());
    }

    @Test
    void batchStatusChangesPreserveAdvancedRulesAndReplaceOnlySimpleRules() {
        ResourceLocation trident = ResourceLocation.withDefaultNamespace("trident");
        ResourceLocation bone = ResourceLocation.withDefaultNamespace("bone");
        ResourceLocation arrow = ResourceLocation.withDefaultNamespace("arrow");
        DumpNullRule advanced = DumpNullRule.basic(
                DumpNullRuleAction.PASS,
                trident,
                80,
                List.of(ResourceLocation.withDefaultNamespace("loyalty")),
                List.of()
        );
        DumpNullData data = new DumpNullData(List.of(), List.of(
                advanced,
                DumpNullRule.basic(DumpNullRuleAction.VOID, trident, 0, List.of(), List.of()),
                DumpNullRule.basic(DumpNullRuleAction.PASS, bone, 0, List.of(), List.of())
        ));

        DumpNullData updated = data.withItemStatuses(List.of(trident, bone, arrow, arrow), DumpNullItemStatus.DISCARDED);

        assertEquals(advanced, updated.rules().getFirst());
        assertEquals(DumpNullItemStatus.ACCEPTED, updated.statusFor(trident));
        assertEquals(DumpNullItemStatus.DISCARDED, updated.statusFor(bone));
        assertEquals(DumpNullItemStatus.DISCARDED, updated.statusFor(arrow));
        assertEquals(4, updated.rules().size());
    }

    @Test
    void clearItemFiltersRemovesAllRulesForMultipleItems() {
        ResourceLocation trident = ResourceLocation.withDefaultNamespace("trident");
        ResourceLocation bone = ResourceLocation.withDefaultNamespace("bone");
        ResourceLocation arrow = ResourceLocation.withDefaultNamespace("arrow");
        DumpNullRule advancedTrident = DumpNullRule.basic(
                DumpNullRuleAction.PASS,
                trident,
                80,
                List.of(ResourceLocation.withDefaultNamespace("loyalty")),
                List.of()
        );
        DumpNullData data = new DumpNullData(List.of(), List.of(
                advancedTrident,
                DumpNullRule.basic(DumpNullRuleAction.VOID, trident, 0, List.of(), List.of()),
                DumpNullRule.basic(DumpNullRuleAction.PASS, bone, 0, List.of(), List.of()),
                DumpNullRule.basic(DumpNullRuleAction.VOID, arrow, 0, List.of(), List.of())
        ));

        DumpNullData updated = data.clearItemFilters(List.of(trident, bone));

        assertEquals(List.of(DumpNullRule.basic(DumpNullRuleAction.VOID, arrow, 0, List.of(), List.of())), updated.rules());
        assertEquals(DumpNullItemStatus.NEUTRAL, updated.statusFor(trident));
        assertEquals(DumpNullItemStatus.NEUTRAL, updated.statusFor(bone));
        assertEquals(DumpNullItemStatus.DISCARDED, updated.statusFor(arrow));
    }

    @Test
    void presetGeneratedSimpleRulesCanBeOverriddenByClicks() {
        ResourceLocation bow = ResourceLocation.withDefaultNamespace("bow");
        DumpNullData data = new DumpNullData(List.of(), List.of(
                DumpNullRule.presetGenerated(DumpNullRuleAction.VOID, bow, "vanilla")
        ));

        DumpNullData accepted = data.withItemStatus(bow, DumpNullItemStatus.ACCEPTED);

        assertEquals(DumpNullItemStatus.DISCARDED, data.itemStatusSummaries().getFirst().status());
        assertTrue(data.itemStatusSummaries().getFirst().presetGenerated());
        assertEquals(DumpNullItemStatus.ACCEPTED, accepted.itemStatusSummaries().getFirst().status());
        assertFalse(accepted.itemStatusSummaries().getFirst().presetGenerated());
    }

    @Test
    void itemFilterCreatesAdvancedRuleBeforeFallbackRule() {
        ResourceLocation trident = ResourceLocation.withDefaultNamespace("trident");

        DumpNullData data = DumpNullData.EMPTY.withItemFilter(
                trident,
                DumpNullItemStatus.ACCEPTED,
                80,
                List.of(ResourceLocation.withDefaultNamespace("loyalty")),
                List.of()
        );

        assertEquals(2, data.rules().size());
        assertTrue(data.rules().getFirst().isAdvanced());
        assertEquals(DumpNullRuleAction.PASS, data.rules().getFirst().action());
        assertEquals(DumpNullRuleAction.VOID, data.rules().get(1).action());
        assertEquals(DumpNullItemStatus.ACCEPTED, data.itemStatusSummaries().getFirst().status());
        assertTrue(data.itemStatusSummaries().getFirst().advanced());
    }

    @Test
    void dumpNullDataRoundTripsRulesAndFutureConditions() {
        CompoundTag customCondition = new CompoundTag();
        customCondition.putString("Type", "future_score");
        customCondition.putInt("Minimum", 80);
        DumpNullData original = new DumpNullData(
                List.of(ResourceLocation.withDefaultNamespace("drowned")),
                List.of(new DumpNullRule(
                        DumpNullRuleAction.PASS,
                        ResourceLocation.withDefaultNamespace("trident"),
                        80,
                        List.of(ResourceLocation.withDefaultNamespace("loyalty")),
                        List.of(ResourceLocation.withDefaultNamespace("vanishing_curse")),
                        List.of(customCondition)
                ))
        );

        DumpNullData loaded = DumpNullData.load(original.save());

        assertEquals(original.selectedMobs(), loaded.selectedMobs());
        assertEquals(original.rules().getFirst().action(), loaded.rules().getFirst().action());
        assertEquals(original.rules().getFirst().itemId(), loaded.rules().getFirst().itemId());
        assertEquals(80, loaded.rules().getFirst().minDurabilityPercent());
        assertEquals("future_score", loaded.rules().getFirst().customConditions().getFirst().getString("Type"));
    }

    @Test
    void expandedDumpNullDataRoundTripsAutomationState() {
        ResourceLocation rottenFlesh = ResourceLocation.withDefaultNamespace("rotten_flesh");
        Map<Direction, DumpNullSideMode> sideModes = new EnumMap<>(Direction.class);
        sideModes.put(Direction.DOWN, DumpNullSideMode.INPUT);
        sideModes.put(Direction.UP, DumpNullSideMode.OUTPUT);
        DumpNullData original = new DumpNullData(
                List.of(ResourceLocation.withDefaultNamespace("zombie")),
                List.of(DumpNullRule.basic(DumpNullRuleAction.VOID, rottenFlesh, 0, List.of(), List.of())),
                true,
                DumpNullHeldDiscardMode.GENERATE_FE,
                DumpNullDockDiscardMode.EXPORT_DISCARD_LANE,
                sideModes,
                Set.of(Direction.EAST),
                List.of(new DumpNullBufferEntry(ResourceLocation.withDefaultNamespace("rotten_flesh"), 5)),
                List.of(new DumpNullBufferEntry(ResourceLocation.withDefaultNamespace("bone"), 7)),
                List.of(new DumpNullBufferEntry(ResourceLocation.withDefaultNamespace("bow"), 1)),
                Set.of(DumpNullUpgradeType.POWER),
                1234
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DumpNullData.write(buffer, original);
        DumpNullData networkLoaded = DumpNullData.read(buffer);
        DumpNullData nbtLoaded = DumpNullData.load(original.save());

        assertEquals(original, networkLoaded);
        assertEquals(original, nbtLoaded);
        assertEquals(DumpNullSideMode.INPUT, nbtLoaded.sideMode(Direction.DOWN));
        assertEquals(DumpNullSideMode.BOTH, nbtLoaded.sideMode(Direction.NORTH));
        assertTrue(nbtLoaded.isDiscardExportSide(Direction.EAST));
        assertTrue(nbtLoaded.hasUpgrade(DumpNullUpgradeType.POWER));
    }

    @Test
    void oldDumpNullDataDefaultsNewAutomationStateSafely() {
        CompoundTag root = new CompoundTag();
        ListTag mobs = new ListTag();
        mobs.add(StringTag.valueOf("minecraft:zombie"));
        root.put("SelectedMobs", mobs);
        ListTag rules = new ListTag();
        rules.add(DumpNullRule.basic(DumpNullRuleAction.VOID, ResourceLocation.withDefaultNamespace("rotten_flesh"), 0, List.of(), List.of()).save());
        root.put("Rules", rules);

        DumpNullData loaded = DumpNullData.load(root);

        assertFalse(loaded.active());
        assertEquals(DumpNullHeldDiscardMode.BLOCK_PICKUP, loaded.heldDiscardMode());
        assertEquals(DumpNullDockDiscardMode.VOID, loaded.dockDiscardMode());
        for (Direction direction : Direction.values()) {
            assertEquals(DumpNullSideMode.BOTH, loaded.sideMode(direction));
            assertFalse(loaded.isDiscardExportSide(direction));
        }
        assertTrue(loaded.inputBuffer().isEmpty());
        assertTrue(loaded.outputBuffer().isEmpty());
        assertTrue(loaded.discardBuffer().isEmpty());
        assertFalse(loaded.hasUpgrade(DumpNullUpgradeType.POWER));
        assertEquals(0, loaded.storedEnergy());
    }

    @Test
    void automationModesAndEnergyRewardOverridesNormalizeInvalidInput() {
        assertEquals(DumpNullHeldDiscardMode.BLOCK_PICKUP, DumpNullHeldDiscardMode.byId(99));
        assertEquals(DumpNullDockDiscardMode.VOID, DumpNullDockDiscardMode.byName("missing"));
        assertEquals(DumpNullSideMode.BOTH, DumpNullSideMode.byId(-1));

        Map<ResourceLocation, Integer> rewards = DeepNullConfig.parseDumpNullEnergyOverrides(List.of(
                "minecraft:diamond=250",
                "minecraft:bone=-4",
                "missing_equals",
                "deepnullreforged:bad=abc"
        ));

        assertEquals(250, rewards.get(ResourceLocation.withDefaultNamespace("diamond")));
        assertEquals(0, rewards.get(ResourceLocation.withDefaultNamespace("bone")));
        assertFalse(rewards.containsKey(ResourceLocation.fromNamespaceAndPath("deepnullreforged", "bad")));
    }
}
