package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.dennull.DenNullData;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.integration.jei.NullWorkbenchTransferSupport;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetEntry;
import dev.deepdaddyttv.deepnullreforged.nullseed.ItemSetPresetOption;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedEntry;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedKind;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPlan;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPreset;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetCatalog;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSavedData;
import dev.deepdaddyttv.deepnullreforged.nullseed.NullSeedPresetSource;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class NullWorkbenchRegressionGameTests {
    private static final BlockPos WORKBENCH_POS = new BlockPos(1, 1, 1);

    private NullWorkbenchRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void null_workbench_recipe_catalog_stays_consistent(GameTestHelper helper) {
        List<NullWorkbenchRecipes.CraftRecipe> recipes = NullWorkbenchRecipes.all();
        helper.assertValueEqual(recipes.size(), 18, "Null Workbench recipe count");

        for (DeepNullTier tier : DeepNullTier.values()) {
            if (tier.creative()) {
                continue;
            }

            NullWorkbenchRecipes.CraftRecipe deepRecipe = findRecipeByResult(DeepNullGameTestSupport.deepNullStack(tier), recipes);
            assertIngredient(helper, deepRecipe, tierItem(tier), 16, tier + " DeepNull tier ingredient");
            assertIngredient(helper, deepRecipe, Items.COAL_BLOCK, 10, tier + " DeepNull coal requirement");
            assertIngredient(helper, deepRecipe, Items.GLASS, 1, tier + " DeepNull glass requirement");
            assertIngredient(helper, deepRecipe, tierDye(tier), 1, tier + " DeepNull dye requirement");

            NullWorkbenchRecipes.CraftRecipe panelRecipe = findRecipeByResult(DeepNullGameTestSupport.panelStack(tier, 5), recipes);
            helper.assertValueEqual(panelRecipe.result().getCount(), 5, tier + " panel output count");
            assertIngredient(helper, panelRecipe, tierItem(tier), 16, tier + " panel tier ingredient");
            assertIngredient(helper, panelRecipe, Items.COAL_BLOCK, 10, tier + " panel coal requirement");
            assertIngredient(helper, panelRecipe, Items.GLASS_PANE, 1, tier + " panel pane requirement");
            assertIngredient(helper, panelRecipe, tierDye(tier), 1, tier + " panel dye requirement");

            NullWorkbenchRecipes.CraftRecipe dampRecipe = findRecipeByResult(DeepNullGameTestSupport.dampNullStack(tier), recipes);
            assertIngredient(helper, dampRecipe, DeepNullGameTestSupport.deepNullStack(tier).getItem(), 1, tier + " DampNull DeepNull ingredient");
            assertIngredient(helper, dampRecipe, ModItems.FLUID_UPGRADE.get(), 1, tier + " DampNull fluid upgrade ingredient");
        }
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_style_apply_and_reset_output_behave(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, DeepNullGameTestSupport.deepNullStack(DeepNullTier.DIAMOND));
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT, new ItemStack(Items.CREEPER_HEAD));

        helper.assertTrue(workbench.applyStyleColors(0x112233, 0x445566), "Workbench style apply should succeed");
        ItemStack styledOutput = workbench.getStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT);
        helper.assertFalse(styledOutput.isEmpty(), "Style apply should place the Null into the output slot");

        DeepNullInventory styledInventory = new DeepNullInventory(DeepNullTier.DIAMOND, styledOutput, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(styledInventory.getFrameColor(), 0x112233, "Styled output frame color");
        helper.assertValueEqual(styledInventory.getGlassColor(), 0x445566, "Styled output glass color");
        helper.assertValueEqual(styledInventory.getStyleVariant(), StyleGlassVariant.CREEPER, "Styled output variant");
        helper.assertTrue(workbench.getStackInSlot(NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT).isEmpty(), "Style modifier should be consumed on apply");

        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, styledOutput);
        helper.assertTrue(workbench.resetStyleColors(), "Workbench style reset should succeed");

        ItemStack resetOutput = workbench.getStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT);
        DeepNullInventory resetInventory = new DeepNullInventory(DeepNullTier.DIAMOND, resetOutput, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(resetInventory.getStyleVariant(), StyleGlassVariant.DEFAULT, "Style reset should clear the variant");
        helper.assertFalse(DeepNullInventory.hasCustomStyle(resetOutput), "Style reset should restore the default look");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_backup_and_restore_round_trip(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        ItemStack sourceNull = DeepNullGameTestSupport.deepNullStack(DeepNullTier.GOLD);
        DeepNullInventory sourceInventory = new DeepNullInventory(DeepNullTier.GOLD, sourceNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setAutoPickupEnabled(false);
        sourceInventory.setStyle(0x223344, 0x556677, StyleGlassVariant.PICKAXE);

        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, sourceNull);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT, new ItemStack(ModItems.SYNCHRONIZER.get()));
        helper.assertTrue(workbench.startBackup(), "Workbench backup should start");
        DeepNullGameTestSupport.tickWorkbench(helper, WORKBENCH_POS, workbench.getSyncDuration());

        ItemStack backedUpNull = workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT);
        ItemStack backedUpSynchronizer = workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT);
        helper.assertFalse(backedUpNull.isEmpty(), "Backup should output the original Null");
        helper.assertTrue(SynchronizerItem.hasConfiguration(backedUpSynchronizer), "Backup should output a configured Synchronizer");

        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT, ItemStack.EMPTY);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.SYNC_SYNCHRONIZER_OUTPUT_SLOT, ItemStack.EMPTY);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, DeepNullGameTestSupport.deepNullStack(DeepNullTier.GOLD));
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.SYNCHRONIZER_SLOT, backedUpSynchronizer);
        helper.assertTrue(workbench.startRestore(), "Workbench restore should start");
        DeepNullGameTestSupport.tickWorkbench(helper, WORKBENCH_POS, workbench.getSyncDuration());

        ItemStack restoredNull = workbench.getStackInSlot(NullWorkbenchBlockEntity.SYNC_NULL_OUTPUT_SLOT);
        helper.assertFalse(restoredNull.isEmpty(), "Restore should output the configured Null");
        DeepNullInventory restoredInventory = new DeepNullInventory(DeepNullTier.GOLD, restoredNull, helper.getLevel().registryAccess(), null);
        helper.assertFalse(restoredInventory.isAutoPickupEnabled(), "Restore should preserve auto-pickup state");
        helper.assertValueEqual(restoredInventory.getFrameColor(), 0x223344, "Restore should preserve frame color");
        helper.assertValueEqual(restoredInventory.getGlassColor(), 0x556677, "Restore should preserve glass color");
        helper.assertValueEqual(restoredInventory.getStyleVariant(), StyleGlassVariant.PICKAXE, "Restore should preserve style variant");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_transfer_support_moves_recipe_inputs_from_player_inventory(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        player.getInventory().clearContent();

        NullWorkbenchMenu menu = new NullWorkbenchMenu(0, player.getInventory(), workbench);
        NullWorkbenchRecipes.CraftRecipe recipe = NullWorkbenchRecipes.all().stream()
                .filter(candidate -> candidate.result().is(ModItems.REDSTONE_PANEL.get()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing redstone panel workbench recipe"));

        player.getInventory().setItem(0, new ItemStack(Items.REDSTONE, 16));
        player.getInventory().setItem(1, new ItemStack(Items.COAL_BLOCK, 10));
        player.getInventory().setItem(2, new ItemStack(Items.GLASS_PANE, 1));
        player.getInventory().setItem(3, new ItemStack(Items.RED_DYE, 1));

        helper.assertTrue(NullWorkbenchTransferSupport.canTransfer(menu, player, recipe), "Workbench JEI transfer support should detect available player ingredients");
        helper.assertTrue(NullWorkbenchTransferSupport.executeTransfer(menu, player, recipe), "Workbench JEI transfer support should move items into the workbench");
        helper.assertValueEqual(workbench.getStackInSlot(NullWorkbenchBlockEntity.INPUT_SLOT_START).getCount(), 16, "Workbench input 0 should receive the tier ingredient");
        helper.assertValueEqual(workbench.getStackInSlot(NullWorkbenchBlockEntity.INPUT_SLOT_START + 1).getCount(), 10, "Workbench input 1 should receive coal blocks");
        helper.assertValueEqual(workbench.getStackInSlot(NullWorkbenchBlockEntity.INPUT_SLOT_START + 2).getCount(), 1, "Workbench input 2 should receive the pane");
        helper.assertValueEqual(workbench.getStackInSlot(NullWorkbenchBlockEntity.INPUT_SLOT_START + 3).getCount(), 1, "Workbench input 3 should receive the dye");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Workbench transfer should consume moved player ingredients");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_seeder_applies_reservations_without_overwriting_real_items(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        ItemStack sourceNull = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory sourceInventory = new DeepNullInventory(DeepNullTier.REDSTONE, sourceNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 12));

        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, sourceNull);
        NullSeedPlan plan = workbench.applySeedConfig(List.of(
                id(Items.COBBLESTONE),
                id(Items.IRON_INGOT),
                id(Items.DIAMOND),
                id(Items.EMERALD),
                id(Items.REDSTONE),
                id(Items.COAL),
                id(Items.GOLD_INGOT),
                id(Items.LAPIS_LAZULI),
                id(Items.QUARTZ),
                id(Items.GUNPOWDER)
        ), true);

        ItemStack configuredNull = workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT);
        DeepNullInventory configuredInventory = new DeepNullInventory(DeepNullTier.REDSTONE, configuredNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(plan.selectedCount(), 10, "Seeder should plan every selected item");
        helper.assertValueEqual(plan.overflowCount(), 1, "Redstone DeepNull should overflow the tenth selected seed item");
        helper.assertTrue(configuredInventory.getStackInSlot(0).is(Items.COBBLESTONE), "Seeder should not overwrite real stored items");
        helper.assertTrue(configuredInventory.getReservedStack(1).is(Items.IRON_INGOT), "Seeder should reserve the second selected item in slot 1");
        helper.assertTrue(configuredInventory.getReservedStack(2).is(Items.DIAMOND), "Seeder should reserve the third selected item in slot 2");

        workbench.clearSeedReservations();
        DeepNullInventory clearedInventory = new DeepNullInventory(DeepNullTier.REDSTONE, configuredNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(clearedInventory.getReservedSlotCount(), 0, "Clearing reservations should remove ghost templates");
        helper.assertTrue(clearedInventory.getStackInSlot(0).is(Items.COBBLESTONE), "Clearing reservations should leave real stored items intact");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_seed_applies_damp_fluid_templates_without_filling_tanks(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        ItemStack dampNull = DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, dampNull, helper.getLevel().registryAccess(), null);
        inventory.fillFluid(0, new FluidStack(Fluids.WATER, 1000), false);

        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, dampNull);
        NullSeedPlan plan = workbench.applySeedEntries(List.of(
                NullSeedEntry.fluid(ResourceLocation.withDefaultNamespace("lava"), 0, 1000),
                NullSeedEntry.fluid(ResourceLocation.withDefaultNamespace("lava"), 1, 2000)
        ), true);

        DeepNullInventory configured = new DeepNullInventory(DeepNullTier.REDSTONE, workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(plan.blockedCount(), 1, "Seed should report lava-over-water as blocked");
        helper.assertValueEqual(plan.appliedCount(), 1, "Seed should reserve the compatible empty tank");
        helper.assertTrue(configured.getFluidInSlot(0).getFluid().isSame(Fluids.WATER), "Existing water tank should not be overwritten");
        helper.assertTrue(configured.getFluidInSlot(1).isEmpty(), "Seed apply should not create real lava contents");
        helper.assertTrue(configured.getReservedFluidTemplate(1).getFluid().isSame(Fluids.LAVA), "Empty tank should receive a planned lava template");
        helper.assertValueEqual(configured.getReservedFluidTemplate(1).getAmount(), 2000, "Planned lava template amount");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void workbench_seed_applies_den_entity_templates_without_captures(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        ItemStack denNull = DeepNullGameTestSupport.denNullStack(DeepNullTier.REDSTONE);

        helper.assertTrue(workbench.getItemHandler().isItemValid(NullWorkbenchBlockEntity.NULL_SLOT, denNull), "Workbench Null slot should accept DenNull for Seed");
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, denNull);
        NullSeedPlan plan = workbench.applySeedEntries(List.of(
                NullSeedEntry.entity(ResourceLocation.withDefaultNamespace("cow"), 0),
                NullSeedEntry.entity(ResourceLocation.withDefaultNamespace("pig"), 1)
        ), true);

        DenNullData data = DenNullData.get(workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT));
        helper.assertValueEqual(plan.appliedCount(), 2, "Den seed templates should apply");
        helper.assertValueEqual(data.entries().size(), 0, "Den seed templates should not create captured entities");
        helper.assertValueEqual(data.templates().size(), 2, "Den seed should write entity templates");
        helper.assertValueEqual(data.templateAt(0).entityType(), ResourceLocation.withDefaultNamespace("cow"), "Cow template target");
        helper.assertValueEqual(data.templateAt(1).entityType(), ResourceLocation.withDefaultNamespace("pig"), "Pig template target");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void reserved_slots_route_dock_insertion_into_configured_slots(GameTestHelper helper) {
        BlockPos dockPos = new BlockPos(2, 1, 1);
        helper.setBlock(dockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(dockPos)) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("DeepNull Dock block entity was not created");
            return;
        }

        ItemStack deepNull = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNull, helper.getLevel().registryAccess(), null);
        inventory.setReservedStack(2, new ItemStack(Items.IRON_INGOT));
        dock.setStoredDeepNull(deepNull);

        IItemHandler handler = dock.getAutomationHandler(null);
        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.IRON_INGOT, 5), false);
        DeepNullInventory routedInventory = dock.createInventory();

        helper.assertTrue(remainder.isEmpty(), "Dock automation should accept reserved matching items");
        helper.assertFalse(routedInventory == null, "Dock should expose the configured DeepNull inventory");
        helper.assertTrue(routedInventory.getStackInSlot(0).isEmpty(), "Dock insertion should not bypass a reserved matching slot");
        helper.assertTrue(routedInventory.getStackInSlot(2).is(Items.IRON_INGOT), "Dock insertion should route into the reserved iron slot");
        helper.assertValueEqual(routedInventory.getStackInSlot(2).getCount(), 5, "Reserved iron slot count");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void reserved_item_configuration_round_trips_and_persists_after_extract(GameTestHelper helper) {
        ItemStack backingStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.IRON);
        DeepNullInventory source = new DeepNullInventory(DeepNullTier.IRON, backingStack, helper.getLevel().registryAccess(), null);
        source.setReservedStack(4, new ItemStack(Items.DIAMOND));
        source.insertItem(4, new ItemStack(Items.DIAMOND), false);

        ItemStack extracted = source.extractItemIgnoreExtractionMode(4, 1, false);
        DeepNullInventory reloaded = new DeepNullInventory(DeepNullTier.IRON, backingStack, helper.getLevel().registryAccess(), null);
        DeepNullInventory imported = new DeepNullInventory(DeepNullTier.IRON, DeepNullGameTestSupport.deepNullStack(DeepNullTier.IRON), helper.getLevel().registryAccess(), null);
        imported.importConfiguration(reloaded.exportConfiguration());

        helper.assertTrue(extracted.is(Items.DIAMOND), "Reserved slot should extract real stored items normally");
        helper.assertTrue(reloaded.getStackInSlot(4).isEmpty(), "Extraction should drain the real stack");
        helper.assertTrue(reloaded.getReservedStack(4).is(Items.DIAMOND), "Reservation should survive extraction to zero");
        helper.assertTrue(imported.getReservedStack(4).is(Items.DIAMOND), "Synchronizer-style configuration export/import should preserve reservations");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void seed_preset_catalog_contains_representative_vanilla_entries(GameTestHelper helper) {
        assertPresetContains(helper, ItemSetPresetOption.ORES, Items.IRON_ORE, Items.RAW_IRON, Items.IRON_INGOT, Items.DIAMOND);
        assertPresetContains(helper, ItemSetPresetOption.FARMING, Items.OAK_SAPLING, Items.WHEAT_SEEDS, Items.WHEAT, Items.BREAD);
        assertPresetContains(helper, ItemSetPresetOption.REDSTONE, Items.REDSTONE, Items.HOPPER, Items.PISTON, Items.OBSERVER);
        assertPresetContains(helper, ItemSetPresetOption.MOB_DROPS, Items.ROTTEN_FLESH, Items.BONE, Items.GUNPOWDER, Items.ENDER_PEARL);
        List<NullSeedPreset> dampPresets = NullSeedPresetCatalog.all(helper.getLevel(), NullSeedKind.FLUID);
        helper.assertTrue(dampPresets.stream().anyMatch(preset -> preset.presetId().equals(NullSeedPresetCatalog.BUILT_IN_FLUIDS)
                && preset.entries().stream().anyMatch(entry -> entry.kind() == NullSeedKind.FLUID && entry.id().equals(ResourceLocation.withDefaultNamespace("water")))), "Fluid seed presets should include water");
        List<NullSeedPreset> denPresets = NullSeedPresetCatalog.all(helper.getLevel(), NullSeedKind.ENTITY);
        helper.assertTrue(denPresets.stream().anyMatch(preset -> preset.presetId().equals(NullSeedPresetCatalog.BUILT_IN_ENTITIES)
                && preset.entries().stream().anyMatch(entry -> entry.kind() == NullSeedKind.ENTITY && entry.id().equals(ResourceLocation.withDefaultNamespace("cow")))), "Entity seed presets should include cows");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void user_seed_preset_saved_data_is_visible_to_catalog(GameTestHelper helper) {
        NullSeedPreset preset = new NullSeedPreset(
                "user:shared_test",
                "Shared Test",
                NullSeedPresetSource.USER,
                "test",
                List.of(NullSeedKind.ITEM),
                List.of(NullSeedEntry.item(id(Items.DIAMOND), 0))
        );
        NullSeedPresetSavedData.get(helper.getLevel()).savePreset(preset);

        List<NullSeedPreset> presets = NullSeedPresetCatalog.all(helper.getLevel(), NullSeedKind.ITEM);
        helper.assertTrue(presets.stream().anyMatch(candidate -> candidate.presetId().equals("user:shared_test")
                && candidate.entries().stream().anyMatch(entry -> entry.id().equals(id(Items.DIAMOND)))), "Saved user preset should be visible in the same world catalog");
        helper.succeed();
    }

    private static NullWorkbenchRecipes.CraftRecipe findRecipeByResult(ItemStack result, List<NullWorkbenchRecipes.CraftRecipe> recipes) {
        return recipes.stream()
                .filter(recipe -> ItemStack.isSameItemSameComponents(recipe.result(), result) && recipe.result().getCount() == result.getCount())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing workbench recipe for " + result));
    }

    private static void assertIngredient(GameTestHelper helper, NullWorkbenchRecipes.CraftRecipe recipe, Item item, int count, String label) {
        helper.assertTrue(
                recipe.ingredients().stream().anyMatch(ingredient -> ingredient.stack().is(item) && ingredient.stack().getCount() == count),
                "Missing ingredient for " + label
        );
    }

    private static Item tierItem(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> Items.REDSTONE;
            case LAPIS -> Items.LAPIS_LAZULI;
            case IRON -> Items.IRON_INGOT;
            case GOLD -> Items.GOLD_INGOT;
            case DIAMOND -> Items.DIAMOND;
            case EMERALD -> Items.EMERALD;
            case CREATIVE -> throw new IllegalArgumentException("Creative tier has no crafting recipe");
        };
    }

    private static Item tierDye(DeepNullTier tier) {
        return switch (tier) {
            case REDSTONE -> Items.RED_DYE;
            case LAPIS -> Items.BLUE_DYE;
            case IRON -> Items.WHITE_DYE;
            case GOLD -> Items.YELLOW_DYE;
            case DIAMOND -> Items.CYAN_DYE;
            case EMERALD -> Items.GREEN_DYE;
            case CREATIVE -> throw new IllegalArgumentException("Creative tier has no crafting recipe");
        };
    }

    private static ResourceLocation id(Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
    }

    private static void assertPresetContains(GameTestHelper helper, String presetId, Item... items) {
        List<ResourceLocation> ids = ItemSetPresetCatalog.build(presetId).stream()
                .map(ItemSetPresetEntry::itemId)
                .toList();
        for (Item item : items) {
            helper.assertTrue(ids.contains(id(item)), presetId + " should include " + id(item));
        }
    }
}
