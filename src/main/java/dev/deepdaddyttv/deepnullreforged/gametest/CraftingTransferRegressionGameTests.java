package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.integration.craftingtweaks.CraftingTweaksCompat;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferHandler;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullJeiPlugin;
import dev.deepdaddyttv.deepnullreforged.integration.jei.ServerDeepNullJeiSession;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.item.Items.CHEST;
import static net.minecraft.world.item.Items.CRAFTING_TABLE;
import static net.minecraft.world.item.Items.OAK_PLANKS;

public final class CraftingTransferRegressionGameTests {
    private CraftingTransferRegressionGameTests() {
    }

    public static void jei_transfer_detects_carried_deepnull_in_player_two_by_two(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        var inventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                deepNullStack,
                helper.getLevel().registryAccess(),
                null
        );
        inventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 4));
        inventory.setCustomExtractionMinimum(0, 0);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.planTransfer(menu, player, recipe, false) != null, "2x2 JEI transfer should detect carried DeepNull ingredients");
        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "2x2 JEI transfer should fill the crafting grid from the carried DeepNull");

        int plankSlots = 0;
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            ItemStack stack = menu.getCraftSlots().getItem(i);
            if (stack.is(OAK_PLANKS)) {
                plankSlots++;
                helper.assertValueEqual(stack.getCount(), 1, "Each 2x2 crafting slot should receive one plank");
            }
        }
        helper.assertValueEqual(plankSlots, 4, "All four 2x2 slots should be filled");
        helper.assertTrue(menu.getResultSlot().getItem().is(CRAFTING_TABLE), "2x2 transfer should yield the crafting table result");
        helper.succeed();
    }

    public static void jei_plugin_registers_exact_menu_handlers_for_carried_deepnulls(GameTestHelper helper) {
        RecordingTransferRegistration registration = new RecordingTransferRegistration();
        new DeepNullJeiPlugin().registerRecipeTransferHandlers(registration);

        List<DeepNullCraftingTransferHandler> handlers = registration.specificHandlers.stream()
                .map(SpecificRegistration::handler)
                .filter(DeepNullCraftingTransferHandler.class::isInstance)
                .map(DeepNullCraftingTransferHandler.class::cast)
                .toList();

        helper.assertValueEqual(handlers.size(), 2, "JEI should register exactly two carried DeepNull crafting handlers");

        DeepNullCraftingTransferHandler inventoryHandler = handlers.stream()
                .filter(handler -> handler.getContainerClass() == InventoryMenu.class)
                .findFirst()
                .orElse(null);
        helper.assertTrue(inventoryHandler != null, "JEI should register a carried DeepNull handler for the player 2x2 inventory menu");
        if (inventoryHandler == null) {
            return;
        }
        helper.assertTrue(inventoryHandler.getMenuType().isEmpty(), "InventoryMenu JEI handler should not require a menu type");

        DeepNullCraftingTransferHandler craftingHandler = handlers.stream()
                .filter(handler -> handler.getContainerClass() == CraftingMenu.class)
                .findFirst()
                .orElse(null);
        helper.assertTrue(craftingHandler != null, "JEI should register a carried DeepNull handler for the crafting table menu");
        if (craftingHandler == null) {
            return;
        }
        helper.assertTrue(craftingHandler.getMenuType().equals(Optional.of(MenuType.CRAFTING)), "CraftingMenu JEI handler should require MenuType.CRAFTING");
        helper.succeed();
    }

    public static void jei_transfer_detects_carried_deepnull_in_main_inventory_slot(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(12, deepNullStack);

        var inventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                deepNullStack,
                helper.getLevel().registryAccess(),
                null
        );
        inventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 4));
        inventory.setCustomExtractionMinimum(0, 0);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.planTransfer(menu, player, recipe, false) != null, "2x2 JEI transfer should detect a carried DeepNull from the main inventory");
        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "2x2 JEI transfer should pull ingredients from a carried DeepNull in the main inventory");

        int plankSlots = 0;
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            ItemStack stack = menu.getCraftSlots().getItem(i);
            if (stack.is(OAK_PLANKS)) {
                plankSlots++;
                helper.assertValueEqual(stack.getCount(), 1, "Each 2x2 crafting slot should receive one plank from a carried DeepNull in the main inventory");
            }
        }
        helper.assertValueEqual(plankSlots, 4, "All four 2x2 slots should be filled when the carried DeepNull is in the main inventory");
        helper.assertTrue(menu.getResultSlot().getItem().is(CRAFTING_TABLE), "Main-inventory JEI transfer should yield the crafting table result");
        helper.succeed();
    }

    public static void jei_transfer_handles_large_stored_counts_from_carried_deepnull(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(12, deepNullStack);

        var inventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                deepNullStack,
                helper.getLevel().registryAccess(),
                null
        );
        inventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 196));
        inventory.setCustomExtractionMinimum(0, 0);

        CraftingMenu menu = new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(net.minecraft.core.BlockPos.ZERO)));
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:chest");

        DeepNullCraftingTransferSupport.TransferPlan plan = DeepNullCraftingTransferSupport.planTransfer(menu, player, recipe, true);
        helper.assertTrue(plan != null, "3x3 JEI transfer should handle large stored DeepNull counts");
        if (plan == null) {
            helper.fail("Expected a transfer plan for large stored DeepNull counts");
            return;
        }
        helper.assertValueEqual(plan.craftCount(), 24, "Max-transfer planning should use the full large stored count from the carried DeepNull");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, true), "3x3 JEI transfer should populate the crafting grid from a large stored DeepNull count");

        DeepNullCraftingTransferSupport.CraftingContext context = DeepNullCraftingTransferSupport.resolveContext(menu);
        helper.assertTrue(context != null, "3x3 crafting context should resolve after large-count JEI transfer");
        if (context == null) {
            helper.fail("3x3 crafting context did not resolve after large-count transfer");
            return;
        }
        int plankSlots = 0;
        for (int i = 0; i < context.craftMatrix().getContainerSize(); i++) {
            ItemStack stack = context.craftMatrix().getItem(i);
            if (stack.is(OAK_PLANKS)) {
                plankSlots++;
                helper.assertValueEqual(stack.getCount(), 24, "Each required 3x3 slot should receive the planned large-count transfer amount");
            }
        }
        helper.assertValueEqual(plankSlots, 8, "Eight 3x3 slots should be filled for the chest recipe using the large-count DeepNull");
        helper.assertTrue(menu.getResultSlot().getItem().is(CHEST), "Large-count transfer should still yield the chest result");
        helper.succeed();
    }

    public static void jei_transfer_detects_carried_deepnull_in_crafting_table_three_by_three(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        var inventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                deepNullStack,
                helper.getLevel().registryAccess(),
                null
        );
        inventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));
        inventory.setCustomExtractionMinimum(0, 0);

        CraftingMenu menu = new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(net.minecraft.core.BlockPos.ZERO)));
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:chest");

        helper.assertTrue(DeepNullCraftingTransferSupport.planTransfer(menu, player, recipe, false) != null, "3x3 JEI transfer should detect carried DeepNull ingredients");
        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "3x3 JEI transfer should fill the crafting grid from the carried DeepNull");

        DeepNullCraftingTransferSupport.CraftingContext context = DeepNullCraftingTransferSupport.resolveContext(menu);
        helper.assertTrue(context != null, "3x3 crafting context should resolve after JEI transfer");
        if (context == null) {
            helper.fail("3x3 crafting context did not resolve");
            return;
        }
        int plankSlots = 0;
        for (int i = 0; i < context.craftMatrix().getContainerSize(); i++) {
            ItemStack stack = context.craftMatrix().getItem(i);
            if (stack.is(OAK_PLANKS)) {
                plankSlots++;
                helper.assertValueEqual(stack.getCount(), 1, "Each 3x3 crafting slot should receive one plank");
            }
        }
        helper.assertValueEqual(plankSlots, 8, "Eight 3x3 slots should be filled for the chest recipe");
        helper.assertTrue(menu.getResultSlot().getItem().is(CHEST), "3x3 transfer should yield the chest result");
        helper.succeed();
    }

    public static void jei_leftovers_return_to_matching_deepnull_slots_on_clear(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        ItemStack secondaryDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);
        player.getInventory().setItem(1, secondaryDeepNull);

        var sourceInventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                sourceDeepNull,
                helper.getLevel().registryAccess(),
                null
        );
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 1));

        var secondaryInventory = new dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory(
                dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE,
                secondaryDeepNull,
                helper.getLevel().registryAccess(),
                null
        );
        secondaryInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 10));

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            menu.getCraftSlots().setItem(i, new ItemStack(OAK_PLANKS, 1));
        }

        ServerDeepNullJeiSession.mark(player, menu, java.util.List.of(0));
        helper.assertTrue(DeepNullCraftingTransferSupport.returnCurrentCraftingContents(menu, player), "Clearing a JEI-filled grid should return items");
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            helper.assertTrue(menu.getCraftSlots().getItem(i).isEmpty(), "Crafting grid should be emptied when leftovers are returned");
        }
        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        DeepNullInventory reloadedSecondary = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, secondaryDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 5, "Returned crafting leftovers should prefer the original carried DeepNull source");
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSecondary, OAK_PLANKS), 10, "Returned crafting leftovers should not spill into other DeepNulls first");
        helper.succeed();
    }

    public static void jei_leftovers_return_to_preferred_deepnull_even_when_source_stack_was_fully_drained(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            menu.getCraftSlots().setItem(i, new ItemStack(OAK_PLANKS, 1));
        }

        ServerDeepNullJeiSession.mark(player, menu, java.util.List.of(0));
        helper.assertTrue(DeepNullCraftingTransferSupport.returnCurrentCraftingContents(menu, player), "Clearing a JEI-filled grid should return items even when the preferred DeepNull no longer contains that item");
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            helper.assertTrue(menu.getCraftSlots().getItem(i).isEmpty(), "Crafting grid should be emptied when leftovers are returned to a fully drained preferred DeepNull");
        }

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 4, "Returned crafting leftovers should repopulate the preferred DeepNull even if it was fully drained by the transfer");
        helper.succeed();
    }

    public static void jei_transfer_prefers_player_inventory_before_carried_deepnull_stock(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);
        player.getInventory().setItem(9, new ItemStack(OAK_PLANKS, 4));

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "2x2 JEI transfer should succeed when both the DeepNull and player inventory contain matching items");

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 8, "JEI transfer should leave the carried DeepNull untouched while player inventory has enough matching items");
        helper.assertTrue(player.getInventory().getItem(9).isEmpty(), "JEI transfer should consume matching player inventory items first");
        helper.succeed();
    }

    public static void jei_leftovers_return_to_partially_drained_source_deepnull(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "2x2 JEI transfer should succeed before leftovers are returned");
        helper.assertTrue(DeepNullCraftingTransferSupport.returnCurrentCraftingContents(menu, player), "Clearing a JEI-filled grid should return items to the partially drained source DeepNull");

        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            helper.assertTrue(menu.getCraftSlots().getItem(i).isEmpty(), "Crafting grid should be emptied when leftovers are returned to the partially drained source DeepNull");
        }

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 8, "Returned crafting leftovers should restore the partially drained source DeepNull");
        helper.succeed();
    }

    public static void jei_leftovers_prefer_matching_deepnull_even_when_player_inventory_was_used_first(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);
        player.getInventory().setItem(9, new ItemStack(OAK_PLANKS, 4));

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "2x2 JEI transfer should succeed when player inventory provides the ingredients first");
        helper.assertTrue(DeepNullCraftingTransferSupport.returnCurrentCraftingContents(menu, player), "Clearing a JEI-filled grid should still return items to the matching DeepNull");

        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            helper.assertTrue(menu.getCraftSlots().getItem(i).isEmpty(), "Crafting grid should be emptied when leftovers are returned after player-inventory-first transfer");
        }

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 12, "Returned crafting leftovers should still prefer the matching carried DeepNull even when player inventory supplied the recipe");
        helper.assertTrue(player.getInventory().getItem(9).isEmpty(), "Returning leftovers should not dump them back into player inventory when a matching carried DeepNull can accept them");
        helper.succeed();
    }

    public static void jei_transfer_respects_deepnull_extraction_rules(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 5));
        sourceInventory.setCustomExtractionMinimum(0, 1);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "JEI transfer should use extractable DeepNull stock when extraction rules allow it");

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 1, "JEI transfer should respect Keep 1 extraction rules instead of draining the slot fully");

        ItemStack blockedDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(1, blockedDeepNull);
        DeepNullInventory blockedInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, blockedDeepNull, helper.getLevel().registryAccess(), null);
        blockedInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 4));
        blockedInventory.setCustomExtractionMinimum(0, 1);

        InventoryMenu blockedMenu = new InventoryMenu(player.getInventory(), false, player);
        helper.assertTrue(DeepNullCraftingTransferSupport.planTransfer(blockedMenu, player, recipe, false) == null, "JEI transfer planning should fail when only non-extractable DeepNull stock remains");
        helper.succeed();
    }

    public static void jei_close_return_snapshot_reclaims_items_after_vanilla_moved_them_to_inventory(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));
        sourceInventory.setCustomExtractionMinimum(0, 0);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "JEI transfer should populate the crafting grid before close-return fallback is exercised");

        List<ItemStack> snapshot = new ArrayList<>();
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            ItemStack stack = menu.getCraftSlots().getItem(i).copy();
            snapshot.add(stack);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack.copy());
                menu.getCraftSlots().setItem(i, ItemStack.EMPTY);
            }
        }

        helper.assertTrue(DeepNullCraftingTransferSupport.returnCraftingSnapshotContents(menu, player, snapshot), "Snapshot fallback should return vanilla-cleared crafting leftovers to the preferred DeepNull");

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 8, "Snapshot fallback should restore the DeepNull after vanilla already moved leftovers into player inventory");
        helper.assertFalse(player.getInventory().hasAnyOf(java.util.Set.of(OAK_PLANKS)), "Snapshot fallback should pull the returned planks back out of player inventory");
        helper.succeed();
    }

    public static void jei_close_return_payload_reclaims_items_after_menu_closed(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 8));
        sourceInventory.setCustomExtractionMinimum(0, 0);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "JEI transfer should populate the crafting grid before the close-return payload is exercised");

        List<ItemStack> snapshot = new ArrayList<>();
        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            ItemStack stack = menu.getCraftSlots().getItem(i).copy();
            snapshot.add(stack);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack.copy());
                menu.getCraftSlots().setItem(i, ItemStack.EMPTY);
            }
        }

        player.containerMenu = player.inventoryMenu;

        try {
            Method handleCraftingReturn = DeepNullPayloads.class.getDeclaredMethod(
                    "handleCraftingReturn",
                    DeepNullPayloads.CraftingReturnPayload.class,
                    net.minecraft.server.level.ServerPlayer.class
            );
            handleCraftingReturn.setAccessible(true);
            handleCraftingReturn.invoke(null, new DeepNullPayloads.CraftingReturnPayload(menu.containerId, snapshot), player);
        } catch (ReflectiveOperationException exception) {
            helper.fail("Unable to invoke crafting return payload handler: " + exception.getMessage());
            return;
        }

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 8, "Close-return payload should restore the DeepNull even after the server swapped back to the inventory menu");
        helper.assertFalse(player.getInventory().hasAnyOf(java.util.Set.of(OAK_PLANKS)), "Close-return payload should reclaim the returned planks from player inventory");
        helper.assertFalse(ServerDeepNullJeiSession.shouldReturn(player, menu.containerId), "Close-return payload should clear the JEI return session after reclaiming leftovers");
        helper.succeed();
    }

    public static void craftingtweaks_clear_handler_returns_jei_grid_to_deepnull_and_clears_session(GameTestHelper helper) {
        var player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack sourceDeepNull = DeepNullGameTestSupport.deepNullStack(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, sourceDeepNull);

        DeepNullInventory sourceInventory = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        sourceInventory.setStackInSlot(0, new ItemStack(OAK_PLANKS, 4));
        sourceInventory.setCustomExtractionMinimum(0, 0);

        InventoryMenu menu = new InventoryMenu(player.getInventory(), false, player);
        RecipeHolder<CraftingRecipe> recipe = recipe(helper.getLevel(), "minecraft:crafting_table");

        helper.assertTrue(DeepNullCraftingTransferSupport.executeTransfer(menu, player, recipe, false), "JEI transfer should populate the crafting grid before the Crafting Tweaks clear handler runs");
        helper.assertTrue(ServerDeepNullJeiSession.shouldReturn(player, menu), "JEI transfer should mark a preferred return session before the Crafting Tweaks clear handler runs");
        helper.assertTrue(CraftingTweaksCompat.handleJeiAwareClear(menu, player), "Crafting Tweaks clear handling should return JEI ingredients to the DeepNull");

        for (int i = 0; i < menu.getCraftSlots().getContainerSize(); i++) {
            helper.assertTrue(menu.getCraftSlots().getItem(i).isEmpty(), "Crafting Tweaks clear handling should empty the crafting grid after returning items to the DeepNull");
        }

        DeepNullInventory reloadedSource = new DeepNullInventory(dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier.REDSTONE, sourceDeepNull, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(reloadedSource, OAK_PLANKS), 4, "Crafting Tweaks clear handling should restore the original stored DeepNull count");
        helper.assertFalse(ServerDeepNullJeiSession.shouldReturn(player, menu), "Crafting Tweaks clear handling should clear the active JEI return session");
        helper.succeed();
    }

    private static RecipeHolder<CraftingRecipe> recipe(ServerLevel level, String id) {
        return level.getServer()
                .getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, Identifier.parse(id)))
                .filter(holder -> holder.value() instanceof CraftingRecipe)
                .map(holder -> (RecipeHolder<CraftingRecipe>) holder)
                .orElseThrow(() -> new AssertionError("Missing crafting recipe " + id));
    }

    private static final class RecordingTransferRegistration implements IRecipeTransferRegistration {
        private final IJeiHelpers jeiHelpers = proxy(IJeiHelpers.class);
        private final IRecipeTransferHandlerHelper transferHelper = proxy(IRecipeTransferHandlerHelper.class);
        private final List<SpecificRegistration> specificHandlers = new ArrayList<>();

        @Override
        public IJeiHelpers getJeiHelpers() {
            return jeiHelpers;
        }

        @Override
        public IRecipeTransferHandlerHelper getTransferHelper() {
            return transferHelper;
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<? extends C> containerClass, MenuType<C> menuType, IRecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
        }

        @Override
        public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, IRecipeType<R> recipeType) {
            specificHandlers.add(new SpecificRegistration(recipeTransferHandler, recipeType));
        }

        @Override
        public <C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(IUniversalRecipeTransferHandler<C> recipeTransferHandler) {
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null);
        }
    }

    private record SpecificRegistration(IRecipeTransferHandler<?, ?> handler, IRecipeType<?> recipeType) {
    }
}
