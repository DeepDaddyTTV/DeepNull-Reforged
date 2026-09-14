package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.block.entity.NullWorkbenchBlockEntity;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.integration.jei.NullWorkbenchTransferSupport;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.item.EnderUpgradeItem;
import dev.deepdaddyttv.deepnullreforged.menu.NullWorkbenchMenu;
import dev.deepdaddyttv.deepnullreforged.recipe.NullWorkbenchRecipes;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class NullWorkbenchRegressionGameTests {
    private static final BlockPos WORKBENCH_POS = new BlockPos(1, 1, 1);

    private NullWorkbenchRegressionGameTests() {
    }

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

    public static void workbench_style_apply_and_reset_stay_in_null_slot(GameTestHelper helper) {
        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, DeepNullGameTestSupport.deepNullStack(DeepNullTier.DIAMOND));
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT, new ItemStack(Items.CREEPER_HEAD));

        helper.assertTrue(workbench.applyStyleColors(0x112233, 0x445566), "Workbench style apply should succeed");
        ItemStack styledOutput = workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT);
        helper.assertFalse(styledOutput.isEmpty(), "Style apply should keep the Null in its input slot");
        helper.assertTrue(workbench.getStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT).isEmpty(), "Style apply should not use the crafting output slot");

        DeepNullInventory styledInventory = new DeepNullInventory(DeepNullTier.DIAMOND, styledOutput, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(styledInventory.getFrameColor(), 0x112233, "Styled output frame color");
        helper.assertValueEqual(styledInventory.getGlassColor(), 0x445566, "Styled output glass color");
        helper.assertValueEqual(styledInventory.getStyleVariant(), StyleGlassVariant.CREEPER, "Styled output variant");
        helper.assertTrue(workbench.getStackInSlot(NullWorkbenchBlockEntity.STYLE_MODIFIER_SLOT).isEmpty(), "Style modifier should be consumed on apply");

        helper.assertTrue(workbench.resetStyleColors(), "Workbench style reset should succeed");

        ItemStack resetOutput = workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT);
        DeepNullInventory resetInventory = new DeepNullInventory(DeepNullTier.DIAMOND, resetOutput, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(resetInventory.getStyleVariant(), StyleGlassVariant.DEFAULT, "Style reset should clear the variant");
        helper.assertFalse(DeepNullInventory.hasCustomStyle(resetOutput), "Style reset should restore the default look");
        helper.assertTrue(workbench.getStackInSlot(NullWorkbenchBlockEntity.OUTPUT_SLOT).isEmpty(), "Style reset should leave the crafting output slot empty");
        helper.succeed();
    }

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

    public static void workbench_refreshes_an_inserted_ender_linked_null(GameTestHelper helper) {
        BlockPos relativeDockPos = new BlockPos(3, 1, 1);
        BlockPos absoluteDockPos = helper.absolutePos(relativeDockPos);
        helper.setBlock(relativeDockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(absoluteDockPos) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock block entity at " + relativeDockPos);
            return;
        }

        dock.setStoredDeepNull(DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE));
        DeepNullInventory dockInventory = dock.createInventory();
        if (dockInventory == null) {
            helper.fail("Docked DeepNull inventory was not created");
            return;
        }
        dockInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 4));
        dockInventory.setSelectedSlot(0);

        ItemStack linkedNull = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory linkedInventory = new DeepNullInventory(DeepNullTier.REDSTONE, linkedNull, helper.getLevel().registryAccess(), null);
        linkedInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        linkedInventory.setSelectedSlot(0);
        ItemStack enderUpgrade = DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.ENDER);
        EnderUpgradeItem.setLink(enderUpgrade, helper.getLevel().dimension(), absoluteDockPos, false, DeepNullTier.REDSTONE);
        linkedInventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.ENDER.slot(), enderUpgrade);

        NullWorkbenchBlockEntity workbench = DeepNullGameTestSupport.placeWorkbench(helper, WORKBENCH_POS);
        workbench.getItemHandler().setStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT, linkedNull);
        DeepNullInventory changedDock = dock.createInventory();
        changedDock.setStackInSlot(0, new ItemStack(Items.DIRT, 7));
        changedDock.setSelectedSlot(0);
        helper.assertTrue(
                DeepNullInventory.peekSelectedForRender(linkedNull, false).itemStack().is(Items.COBBLESTONE),
                "Workbench Null mirror should begin stale before its refresh tick"
        );

        BlockPos absoluteWorkbenchPos = helper.absolutePos(WORKBENCH_POS);
        long phase = Math.floorMod(absoluteWorkbenchPos.hashCode(), 5);
        long delay = Math.floorMod(phase - helper.getLevel().getGameTime(), 5);
        helper.runAfterDelay(delay == 0L ? 5L : delay, () -> {
            NullWorkbenchBlockEntity.serverTick(
                    helper.getLevel(),
                    absoluteWorkbenchPos,
                    helper.getLevel().getBlockState(absoluteWorkbenchPos),
                    workbench
            );
            DeepNullInventory.SelectedRenderPreview refreshed = DeepNullInventory.peekSelectedForRender(
                    workbench.getStackInSlot(NullWorkbenchBlockEntity.NULL_SLOT),
                    false
            );
            helper.assertTrue(refreshed.itemStack().is(Items.DIRT), "Workbench should refresh linked Null contents while it remains inserted");
            helper.assertValueEqual(refreshed.itemStack().getCount(), 7, "Workbench linked preview should preserve the docked count");
            helper.succeed();
        });
    }

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
}
