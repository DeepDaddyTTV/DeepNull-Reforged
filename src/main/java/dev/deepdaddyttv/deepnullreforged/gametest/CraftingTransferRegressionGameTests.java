package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.integration.jei.DeepNullCraftingTransferSupport;
import dev.deepdaddyttv.deepnullreforged.integration.jei.ServerDeepNullJeiSession;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static net.minecraft.world.item.Items.CHEST;
import static net.minecraft.world.item.Items.CRAFTING_TABLE;
import static net.minecraft.world.item.Items.OAK_PLANKS;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class CraftingTransferRegressionGameTests {
    private CraftingTransferRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
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
        helper.assertTrue(menu.getSlot(menu.getResultSlotIndex()).getItem().is(CRAFTING_TABLE), "2x2 transfer should yield the crafting table result");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
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
        helper.assertTrue(menu.getSlot(menu.getResultSlotIndex()).getItem().is(CRAFTING_TABLE), "Main-inventory JEI transfer should yield the crafting table result");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
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
        helper.assertTrue(menu.getSlot(menu.getResultSlotIndex()).getItem().is(CHEST), "3x3 transfer should yield the chest result");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
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

    private static RecipeHolder<CraftingRecipe> recipe(Level level, String id) {
        return level.getRecipeManager()
                .byKey(ResourceLocation.parse(id))
                .filter(holder -> holder.value() instanceof CraftingRecipe)
                .map(holder -> (RecipeHolder<CraftingRecipe>) holder)
                .orElseThrow(() -> new AssertionError("Missing crafting recipe " + id));
    }
}
