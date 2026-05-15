package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import dev.deepdaddyttv.deepnullreforged.block.DeepNullDockBlock;
import dev.deepdaddyttv.deepnullreforged.block.entity.DeepNullDockBlockEntity;
import dev.deepdaddyttv.deepnullreforged.capability.DeepNullFluidHandler;
import dev.deepdaddyttv.deepnullreforged.event.CommonEvents;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullInventory;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullTier;
import dev.deepdaddyttv.deepnullreforged.inventory.DeepNullUpgradeType;
import dev.deepdaddyttv.deepnullreforged.inventory.ItemExtractionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import dev.deepdaddyttv.deepnullreforged.inventory.StoneworksMaterial;
import dev.deepdaddyttv.deepnullreforged.inventory.StyleGlassVariant;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferDirectionMode;
import dev.deepdaddyttv.deepnullreforged.inventory.TransferOutputMode;
import dev.deepdaddyttv.deepnullreforged.item.SynchronizerItem;
import dev.deepdaddyttv.deepnullreforged.menu.DeepNullMenu;
import dev.deepdaddyttv.deepnullreforged.player.DeepNullPlayerState;
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModCapabilities;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@GameTestHolder(DeepNullReforged.MODID)
@PrefixGameTestTemplate(false)
public final class DeepNullRegressionGameTests {
    private DeepNullRegressionGameTests() {
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void tier_support_matrix_and_defaults_stay_stable(GameTestHelper helper) {
        DeepNullInventory deepRedstone = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        DeepNullInventory deepIron = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.IRON);
        DeepNullInventory deepDiamond = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.DIAMOND);
        DeepNullInventory deepEmerald = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.EMERALD);
        DeepNullInventory dampRedstone = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);

        helper.assertFalse(deepRedstone.supportsUpgrade(DeepNullUpgradeType.FILTER), "Redstone DeepNull should not support Filter Upgrade");
        helper.assertTrue(deepRedstone.supportsUpgrade(DeepNullUpgradeType.FLUID), "Redstone DeepNull should support Fluid Upgrade");
        helper.assertFalse(deepRedstone.supportsUpgrade(DeepNullUpgradeType.ENERGY), "Redstone DeepNull should not support Energy Upgrade");
        helper.assertTrue(deepIron.supportsUpgrade(DeepNullUpgradeType.FILTER), "Iron DeepNull should support Filter Upgrade");
        helper.assertTrue(deepDiamond.supportsUpgrade(DeepNullUpgradeType.ENERGY), "Diamond DeepNull should support Energy Upgrade");
        helper.assertTrue(deepEmerald.supportsUpgrade(DeepNullUpgradeType.DEEP_ENERGY), "Emerald DeepNull should support Deep Energy Upgrade");
        helper.assertTrue(dampRedstone.supportsUpgrade(DeepNullUpgradeType.SPONGE), "Redstone DampNull should support Sponge Upgrade");
        helper.assertTrue(dampRedstone.supportsUpgrade(DeepNullUpgradeType.GAS), "Redstone DampNull should support Gas Upgrade");
        helper.assertFalse(dampRedstone.supportsUpgrade(DeepNullUpgradeType.FILTER), "DampNull should not expose DeepNull-only upgrades");

        helper.assertTrue(DeepNullConfig.voidFullItemsOnPickup(), "voidFullItemsOnPickup should default to true");
        helper.assertTrue(DeepNullConfig.voidFullFluidsOnSponge(), "voidFullFluidsOnSponge should default to true");
        helper.assertValueEqual(DeepNullTier.EMERALD.spongeRangeWidth(), 16, "Emerald sponge width");
        helper.assertValueEqual(DeepNullTier.EMERALD.spongeRangeHeight(), 12, "Emerald sponge height");
        helper.assertValueEqual(DeepNullTier.EMERALD.fluidCapacity(), 512_000, "Emerald DampNull tank capacity");
        helper.assertValueEqual(DeepNullTier.GOLD.dampNullTankCount(), 18, "Gold DampNull tank count");
        helper.assertValueEqual(DeepNullConfig.defaultStoneworksAmount(), 1, "Default Stoneworks amount");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void extraction_zero_apply_all_and_empty_reset_stay_stable(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        inventory.insertItem(0, new ItemStack(Items.COBBLESTONE, 8), false);
        inventory.insertItem(1, new ItemStack(Items.DIRT, 5), false);

        inventory.setCustomExtractionMinimum(0, 0);
        helper.assertValueEqual(inventory.getExtractionMode(0), ItemExtractionMode.KEEP_NONE, "Slot 0 extraction mode after setting zero");
        helper.assertValueEqual(inventory.getExtractionMinimum(0), 0, "Slot 0 extraction minimum after setting zero");

        helper.assertTrue(inventory.setCustomExtractionMinimumAllOccupied(16), "Apply-all extraction should report a change");
        helper.assertValueEqual(inventory.getExtractionMinimum(0), 16, "Occupied slot 0 extraction minimum");
        helper.assertValueEqual(inventory.getExtractionMinimum(1), 16, "Occupied slot 1 extraction minimum");
        helper.assertValueEqual(inventory.getExtractionMinimum(2), 1, "Empty slots should remain at Keep 1");

        inventory.setStackInSlot(0, ItemStack.EMPTY);
        helper.assertValueEqual(inventory.getExtractionMode(0), ItemExtractionMode.KEEP_1, "Cleared slot should reset to Keep 1");
        helper.assertValueEqual(inventory.getExtractionMinimum(0), 1, "Cleared slot minimum should reset to 1");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void style_defaults_variants_and_reset_round_trip(GameTestHelper helper) {
        DeepNullInventory deepInventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.EMERALD);
        int deepDefaultFrame = deepInventory.getFrameColor();
        int deepDefaultGlass = deepInventory.getGlassColor();

        helper.assertFalse(DeepNullInventory.hasCustomStyle(deepInventory.backingStack()), "Fresh DeepNull should not be marked as custom styled");
        deepInventory.setStyle(deepDefaultFrame, deepDefaultGlass, StyleGlassVariant.CREEPER);

        DeepNullInventory deepReload = new DeepNullInventory(DeepNullTier.EMERALD, deepInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(deepReload.getStyleVariant(), StyleGlassVariant.CREEPER, "DeepNull secret variant should persist");

        deepReload.setStyleColors(0x123456, 0x654321);
        DeepNullInventory recoloredDeep = new DeepNullInventory(DeepNullTier.EMERALD, deepInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(recoloredDeep.getStyleVariant(), StyleGlassVariant.CREEPER, "Recoloring should keep the DeepNull variant");
        helper.assertValueEqual(recoloredDeep.getFrameColor(), 0x123456, "DeepNull custom frame color");
        helper.assertValueEqual(recoloredDeep.getGlassColor(), 0x654321, "DeepNull custom glass color");

        recoloredDeep.resetStyleColors();
        DeepNullInventory resetDeep = new DeepNullInventory(DeepNullTier.EMERALD, deepInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertFalse(DeepNullInventory.hasCustomStyle(resetDeep.backingStack()), "Reset DeepNull should no longer be marked as custom styled");
        helper.assertValueEqual(resetDeep.getStyleVariant(), StyleGlassVariant.DEFAULT, "Reset DeepNull variant");
        helper.assertValueEqual(resetDeep.getFrameColor(), deepDefaultFrame, "Reset DeepNull frame color");
        helper.assertValueEqual(resetDeep.getGlassColor(), deepDefaultGlass, "Reset DeepNull glass color");

        DeepNullInventory dampInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.LAPIS);
        int dampDefaultFrame = dampInventory.getFrameColor();
        int dampDefaultGlass = dampInventory.getGlassColor();
        dampInventory.setStyle(dampDefaultFrame, dampDefaultGlass, StyleGlassVariant.FISH);

        DeepNullInventory dampReload = new DeepNullInventory(DeepNullTier.LAPIS, dampInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(dampReload.getStyleVariant(), StyleGlassVariant.FISH, "DampNull secret variant should persist");
        dampReload.resetStyleColors();
        DeepNullInventory resetDamp = new DeepNullInventory(DeepNullTier.LAPIS, dampInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(resetDamp.getStyleVariant(), StyleGlassVariant.DEFAULT, "Reset DampNull variant");
        helper.assertValueEqual(resetDamp.getFrameColor(), dampDefaultFrame, "Reset DampNull frame color");
        helper.assertValueEqual(resetDamp.getGlassColor(), dampDefaultGlass, "Reset DampNull glass color");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void pickup_overflow_void_logic_only_matches_existing_full_slots(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);

        ItemStack fullCobble = new ItemStack(Items.COBBLESTONE);
        fullCobble.setCount(inventory.getSlotLimit(0));
        inventory.setStackInSlot(0, fullCobble);

        helper.assertTrue(inventory.shouldVoidOverflowingPickup(new ItemStack(Items.COBBLESTONE)), "Matching full slot should void pickup overflow");
        helper.assertFalse(inventory.shouldVoidOverflowingPickup(new ItemStack(Items.DIRT)), "Unstored item should not void on pickup");

        ItemStack notFullCobble = new ItemStack(Items.COBBLESTONE);
        notFullCobble.setCount(inventory.getSlotLimit(0) - 1);
        inventory.setStackInSlot(0, notFullCobble);
        helper.assertFalse(inventory.shouldVoidOverflowingPickup(new ItemStack(Items.COBBLESTONE)), "Non-full matching slot should still accept pickup");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void pickup_event_voids_only_matching_full_slots(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        ItemStack fullCobble = new ItemStack(Items.COBBLESTONE);
        fullCobble.setCount(inventory.getSlotLimit(0));
        inventory.setStackInSlot(0, fullCobble);

        ItemEntity matchingOverflow = new ItemEntity(helper.getLevel(), 1.5D, 1.5D, 1.5D, new ItemStack(Items.COBBLESTONE, 5));
        helper.getLevel().addFreshEntity(matchingOverflow);
        ItemEntityPickupEvent.Pre matchingEvent = new ItemEntityPickupEvent.Pre(player, matchingOverflow);
        new CommonEvents().onItemPickup(matchingEvent);

        helper.assertValueEqual(matchingEvent.canPickup(), TriState.FALSE, "Matching overflow pickup should be blocked after DeepNull handles it");
        helper.assertTrue(matchingOverflow.isRemoved(), "Matching overflow item should be discarded after being voided");
        helper.assertValueEqual(inventory.getStackInSlot(0).getCount(), inventory.getSlotLimit(0), "Voiding overflow must not change the stored full stack");

        ItemEntity unrelatedPickup = new ItemEntity(helper.getLevel(), 2.5D, 1.5D, 1.5D, new ItemStack(Items.DIRT, 3));
        helper.getLevel().addFreshEntity(unrelatedPickup);
        ItemEntityPickupEvent.Pre unrelatedEvent = new ItemEntityPickupEvent.Pre(player, unrelatedPickup);
        new CommonEvents().onItemPickup(unrelatedEvent);

        helper.assertValueEqual(unrelatedEvent.canPickup(), TriState.DEFAULT, "Unrelated pickups should remain untouched by overflow void logic");
        helper.assertFalse(unrelatedPickup.isRemoved(), "Unrelated pickups should not be discarded");
        helper.assertValueEqual(unrelatedPickup.getItem().getCount(), 3, "Unrelated pickup stack should remain unchanged");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void player_tossed_items_get_a_ten_second_pickup_delay(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 8));

        ItemEntity tossed = new ItemEntity(helper.getLevel(), 1.5D, 1.5D, 1.5D, new ItemStack(Items.COBBLESTONE, 2));
        helper.getLevel().addFreshEntity(tossed);

        new CommonEvents().onItemToss(new ItemTossEvent(tossed, player));
        ItemEntityPickupEvent.Pre pickupEvent = new ItemEntityPickupEvent.Pre(player, tossed);
        new CommonEvents().onItemPickup(pickupEvent);

        CompoundTag saved = tossed.saveWithoutId(new CompoundTag());
        helper.assertValueEqual((int) saved.getShort("PickupDelay"), 200, "Player-tossed items should wait ten seconds before pickup");
        helper.assertTrue(saved.hasUUID("Thrower"), "Player-tossed items should keep the throwing player recorded");
        helper.assertTrue(player.getUUID().equals(saved.getUUID("Thrower")), "Player-tossed items should record the tossing player's UUID");
        helper.assertValueEqual(pickupEvent.canPickup(), TriState.DEFAULT, "Thrown items should not be absorbed by DeepNull while pickup delay is active");
        helper.assertFalse(tossed.isRemoved(), "Thrown item entity should remain while pickup delay is active");
        helper.assertValueEqual(tossed.getItem().getCount(), 2, "Thrown stack should remain unchanged while pickup delay is active");
        helper.assertValueEqual(inventory.getStackInSlot(0).getCount(), 8, "Stored DeepNull contents should not change during thrown-item pickup delay");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void incompatible_player_tossed_items_keep_vanilla_pickup_delay(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 8));

        ItemEntity tossed = new ItemEntity(helper.getLevel(), 1.5D, 1.5D, 1.5D, new ItemStack(Items.DIRT, 2));
        helper.getLevel().addFreshEntity(tossed);

        new CommonEvents().onItemToss(new ItemTossEvent(tossed, player));

        CompoundTag saved = tossed.saveWithoutId(new CompoundTag());
        helper.assertTrue(!saved.hasUUID("Thrower"), "Incompatible tossed items should not be claimed by DeepNull");
        helper.assertTrue(saved.getShort("PickupDelay") < 200, "Incompatible tossed items should keep the vanilla pickup delay");
        helper.assertValueEqual(inventory.getStackInSlot(0).getCount(), 8, "Stored DeepNull contents should not change when tossed item is incompatible");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void visible_item_capability_round_trip_preserves_large_stored_counts(GameTestHelper helper) throws ReflectiveOperationException {
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 250));

        Method createItemHandler = ModCapabilities.class.getDeclaredMethod("createItemHandler", ItemStack.class);
        createItemHandler.setAccessible(true);
        IItemHandler visibleHandler = (IItemHandler) createItemHandler.invoke(null, deepNullStack);

        helper.assertTrue(visibleHandler instanceof IItemHandlerModifiable, "Visible item capability should be mutable");
        helper.assertValueEqual(visibleHandler.getStackInSlot(0).getCount(), 250, "Visible item capability should expose the full stored stack count");

        ItemStack roundTripped = visibleHandler.getStackInSlot(0).copy();
        ((IItemHandlerModifiable) visibleHandler).setStackInSlot(0, roundTripped);

        DeepNullInventory reloaded = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(reloaded.getStackInSlot(0).getCount(), 250, "Round-tripping a visible stored stack should not collapse it to one vanilla stack");
        helper.assertValueEqual(visibleHandler.getSlotLimit(0), inventory.getSlotLimit(0), "Visible item capability should report the full DeepNull slot capacity");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void global_auto_pickup_toggle_blocks_item_absorption_for_that_player(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        ItemStack deepNullStack = DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE);
        player.getInventory().setItem(0, deepNullStack);

        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, deepNullStack, helper.getLevel().registryAccess(), null);
        inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 8));
        DeepNullPlayerState.setGlobalAutoPickupEnabled(player, false);

        ItemEntity dropped = new ItemEntity(helper.getLevel(), 1.5D, 1.5D, 1.5D, new ItemStack(Items.COBBLESTONE, 4));
        helper.getLevel().addFreshEntity(dropped);
        ItemEntityPickupEvent.Pre pickupEvent = new ItemEntityPickupEvent.Pre(player, dropped);
        new CommonEvents().onItemPickup(pickupEvent);

        helper.assertValueEqual(pickupEvent.canPickup(), TriState.DEFAULT, "Global auto-pickup disable should leave pickup handling untouched");
        helper.assertFalse(dropped.isRemoved(), "Global auto-pickup disable should not consume the dropped entity");
        helper.assertValueEqual(dropped.getItem().getCount(), 4, "Dropped stack should remain unchanged while global auto-pickup is disabled");
        helper.assertValueEqual(inventory.getStackInSlot(0).getCount(), 8, "Stored stack should not change when global auto-pickup is disabled");

        DeepNullPlayerState.setGlobalAutoPickupEnabled(player, true);
        helper.succeed();
    }

    public static void dampnull_fluid_storage_round_trip(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.IRON);
        helper.assertTrue(inventory.supportsFluidStorage(), "DampNull should support fluid storage");
        helper.assertValueEqual(inventory.getFluidSlotCount(), DeepNullTier.IRON.dampNullTankCount(), "DampNull tank count");

        int filled = inventory.fillFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME * 2), false);
        helper.assertValueEqual(filled, FluidType.BUCKET_VOLUME * 2, "Filled water amount");
        helper.assertValueEqual(inventory.getFluidInSlot(0).getAmount(), FluidType.BUCKET_VOLUME * 2, "Stored water amount");

        FluidStack drained = inventory.drainFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), false);
        helper.assertValueEqual(drained.getAmount(), FluidType.BUCKET_VOLUME, "Drained water amount");
        helper.assertValueEqual(inventory.getFluidInSlot(0).getAmount(), FluidType.BUCKET_VOLUME, "Remaining water amount");

        helper.assertValueEqual(inventory.getChemicalInSlot(0), StoredChemical.EMPTY, "Filling fluid should not populate chemical storage");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dampnull_item_fluid_capability_fills_only_selected_tank(GameTestHelper helper) {
        ItemStack dampNull = DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE);
        DeepNullInventory inventory = new DeepNullInventory(DeepNullTier.REDSTONE, dampNull, helper.getLevel().registryAccess(), null);
        inventory.setSelectedSlot(2);

        DeepNullFluidHandler handler = new DeepNullFluidHandler(inventory, dampNull, true);
        int filled = handler.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME * 3), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

        helper.assertValueEqual(filled, FluidType.BUCKET_VOLUME * 3, "Held DampNull capability should accept the fluid");
        helper.assertTrue(inventory.getFluidInSlot(0).isEmpty(), "Held DampNull capability should not spread the fill into tank 0");
        helper.assertTrue(inventory.getFluidInSlot(1).isEmpty(), "Held DampNull capability should not spread the fill into tank 1");
        helper.assertValueEqual(inventory.getFluidInSlot(2).getAmount(), FluidType.BUCKET_VOLUME * 3, "Held DampNull capability should fill only the selected tank");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dampnull_gas_upgrade_rejects_normal_fluid_insertion(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.REDSTONE);
        inventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.GAS.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.GAS));

        helper.assertFalse(inventory.acceptsNormalFluids(), "Gas-upgraded DampNulls should reject normal fluid insertion");
        helper.assertValueEqual(inventory.findFluidInsertSlot(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME)), -1, "Gas-upgraded DampNulls should not expose a fluid insert slot");
        helper.assertValueEqual(inventory.fillFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), false), 0, "Gas-upgraded DampNulls should not accept water fills");

        DeepNullFluidHandler handler = new DeepNullFluidHandler(inventory, inventory.backingStack(), true);
        helper.assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME)), "Held DampNull fluid capability should reject normal fluid insertion when Gas Upgrade is installed");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void docked_dampnull_accepts_fluid_container_right_click_transfer(GameTestHelper helper) {
        BlockPos relativeDockPos = new BlockPos(1, 1, 1);
        BlockPos absoluteDockPos = helper.absolutePos(relativeDockPos);
        helper.setBlock(relativeDockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(absoluteDockPos) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock block entity at " + relativeDockPos);
            return;
        }

        dock.setStoredDeepNull(DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE));
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null) {
            helper.fail("Docked DampNull inventory was not created");
            return;
        }
        inventory.setSelectedSlot(0);

        ItemStack emptiedBucket = DeepNullDockBlock.transferFluidContainerWithDockedDampNull(dock, new ItemStack(Items.WATER_BUCKET), false);
        helper.assertTrue(emptiedBucket != null && emptiedBucket.is(Items.BUCKET), "Docked DampNull should accept a water bucket and return an empty bucket");

        DeepNullInventory filledInventory = dock.createInventory();
        helper.assertValueEqual(filledInventory.getFluidInSlot(0).getAmount(), FluidType.BUCKET_VOLUME, "Docked DampNull should receive bucket fluid into the selected tank");

        ItemStack refilledBucket = DeepNullDockBlock.transferFluidContainerWithDockedDampNull(dock, new ItemStack(Items.BUCKET), false);
        helper.assertTrue(refilledBucket != null && refilledBucket.is(Items.WATER_BUCKET), "Docked DampNull should fill an empty bucket from the selected tank");

        DeepNullInventory drainedInventory = dock.createInventory();
        helper.assertTrue(drainedInventory.getFluidInSlot(0).isEmpty(), "Docked DampNull should drain the transferred fluid back out");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dock_fluid_capability_side_queries_share_live_state(GameTestHelper helper) {
        BlockPos relativeDockPos = new BlockPos(1, 1, 1);
        BlockPos absoluteDockPos = helper.absolutePos(relativeDockPos);
        helper.setBlock(relativeDockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(absoluteDockPos) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock block entity at " + relativeDockPos);
            return;
        }

        dock.setStoredDeepNull(DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE));
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null) {
            helper.fail("Docked DampNull inventory was not created");
            return;
        }
        inventory.fillFluid(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), false);

        IFluidHandler northHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absoluteDockPos, Direction.NORTH);
        IFluidHandler southHandler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, absoluteDockPos, Direction.SOUTH);
        helper.assertTrue(northHandler != null, "Dock should expose a fluid capability for the stored DampNull");
        helper.assertTrue(southHandler != null, "Dock should expose a fluid capability on multiple sides");

        FluidStack extracted = northHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(extracted.getAmount(), FluidType.BUCKET_VOLUME, "Dock fluid capability should allow extracting the stored fluid");
        helper.assertTrue(southHandler.getFluidInTank(0).isEmpty(), "Fluid capability queried from another side should reflect the committed drain");
        helper.assertTrue(southHandler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "A stale side handler must not duplicate already-drained fluid");
        helper.assertTrue(dock.createInventory().getFluidInSlot(0).isEmpty(), "Docked DampNull state should persist the drained fluid amount");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dock_item_capability_extracts_generator_buffer_through_block_capability(GameTestHelper helper) {
        BlockPos relativeDockPos = new BlockPos(1, 1, 1);
        BlockPos absoluteDockPos = helper.absolutePos(relativeDockPos);
        helper.setBlock(relativeDockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(absoluteDockPos) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock block entity at " + relativeDockPos);
            return;
        }

        dock.setStoredDeepNull(DeepNullGameTestSupport.dampNullStack(DeepNullTier.REDSTONE));
        DeepNullInventory inventory = dock.createInventory();
        if (inventory == null) {
            helper.fail("Docked DampNull inventory was not created");
            return;
        }
        inventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.STONE_GENERATOR.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.STONE_GENERATOR));
        if (!setGeneratorBuffer(dock, new ItemStack(Items.COBBLESTONE, 20))) {
            helper.fail("Unable to seed dock generator buffer");
            return;
        }

        IItemHandler northHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absoluteDockPos, Direction.NORTH);
        IItemHandler southHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, absoluteDockPos, Direction.SOUTH);
        helper.assertTrue(northHandler != null, "Dock should expose an item capability while the generator buffer is visible");
        helper.assertTrue(southHandler != null, "Dock should expose the generator buffer item capability on multiple sides");

        ItemStack extracted = northHandler.extractItem(0, 64, false);
        helper.assertValueEqual(extracted.getCount(), 20, "Block item capability should extract the full generator buffer contents");
        helper.assertTrue(dock.getGeneratorBuffer().isEmpty(), "Generator buffer should be empty after block capability extraction");
        helper.assertTrue(southHandler.extractItem(0, 64, false).isEmpty(), "A second side should not extract stale generator buffer contents");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void stoneworks_respects_literal_item_target_count(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        inventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.STONEWORKS.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.STONEWORKS));
        inventory.setStoneworksTargetStacks(4);

        ItemStack cobble = new ItemStack(Items.COBBLESTONE, 10);
        inventory.setStackInSlot(0, cobble);
        inventory.setStackInSlot(1, new ItemStack(Items.DIRT, 1));
        for (int slot = 2; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, new ItemStack(Items.STICK, 1));
        }
        for (StoneworksMaterial material : StoneworksMaterial.values()) {
            inventory.setStoneworksMonitoring(material, true);
        }

        helper.assertTrue(inventory.runStoneworksCycle(false), "Stoneworks should produce dirt while below the configured target");
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(inventory, Items.DIRT), 4, "Stoneworks should stop exactly at the configured item target");
        helper.assertFalse(inventory.runStoneworksCycle(false), "Stoneworks should stop once the target item count is reached");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void stoneworks_target_allows_full_integer_range(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.EMERALD);
        inventory.setStoneworksTargetStacks(Integer.MAX_VALUE);

        helper.assertValueEqual(inventory.getStoneworksTargetStacks(), Integer.MAX_VALUE, "Stoneworks target should allow the full integer range");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void returned_crafting_items_prefer_matching_slots(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 10));

        ItemStack leftoverIron = new ItemStack(Items.IRON_INGOT, 5);
        ItemStack ironRemainder = inventory.insertReturnedCraftingStack(leftoverIron, false);
        helper.assertTrue(ironRemainder.isEmpty(), "Matching crafting leftovers should fit back into the DeepNull");
        helper.assertValueEqual(inventory.getStackInSlot(0).getCount(), 15, "Matching leftovers should merge into the existing stored slot");

        ItemStack leftoverGold = new ItemStack(Items.GOLD_INGOT, 3);
        ItemStack goldRemainder = inventory.insertReturnedCraftingStack(leftoverGold, false);
        helper.assertTrue(goldRemainder.isEmpty(), "New crafting leftovers should use an empty slot when available");
        helper.assertValueEqual(inventory.getStackInSlot(1).getCount(), 3, "New leftovers should land in the first empty slot");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void transfer_output_mode_and_direction_respect_matching_push_and_pull_rules(GameTestHelper helper) {
        DeepNullInventory pushInventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        pushInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 70));
        pushInventory.setStackInSlot(1, new ItemStack(Items.DIRT, 6));
        pushInventory.setCustomExtractionMinimum(0, 0);
        pushInventory.setCustomExtractionMinimum(1, 0);
        pushInventory.setTransferOutputMode(TransferOutputMode.MATCHING);

        ItemStackHandler target = new ItemStackHandler(3);
        target.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 60));
        target.setStackInSlot(1, ItemStack.EMPTY);
        target.setStackInSlot(2, new ItemStack(Items.STICK, 1));

        helper.assertTrue(pushInventory.transferItemsToTarget(target), "Matching output mode should move matching stored items");
        helper.assertValueEqual(target.getStackInSlot(0).getCount(), 64, "Matching target stack should be filled first");
        helper.assertValueEqual(target.getStackInSlot(1).getCount(), 64, "Matching output mode should use empty target slots once the target already contains a matching item");
        helper.assertValueEqual(pushInventory.getStackInSlot(1).getCount(), 6, "Non-matching stored items should remain in the DeepNull");

        DeepNullInventory reloaded = new DeepNullInventory(DeepNullTier.REDSTONE, pushInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(reloaded.getTransferOutputMode(), TransferOutputMode.MATCHING, "Transfer output mode should persist");
        reloaded.setTransferDirectionMode(TransferDirectionMode.INSERT);
        DeepNullInventory directionReload = new DeepNullInventory(DeepNullTier.REDSTONE, pushInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertValueEqual(directionReload.getTransferDirectionMode(), TransferDirectionMode.INSERT, "Transfer direction mode should persist");

        DeepNullInventory pullInventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.REDSTONE);
        pullInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 1));

        ItemStackHandler pullSource = new ItemStackHandler(2);
        pullSource.setStackInSlot(0, new ItemStack(Items.DIRT, 3));
        pullSource.setStackInSlot(1, new ItemStack(Items.COBBLESTONE, 2));

        helper.assertTrue(pullInventory.transferItemsFromTargetMatching(pullSource), "Pull should still work for matching items");
        helper.assertValueEqual(DeepNullGameTestSupport.storedItemCount(pullInventory, Items.COBBLESTONE), 3, "Matching pulled items should be added");
        helper.assertValueEqual(pullSource.getStackInSlot(0).getCount(), 3, "Non-matching pull source contents should remain untouched");
        helper.assertTrue(pullSource.getStackInSlot(1).isEmpty(), "Matching pull source contents should be removed");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void stoneworks_dust_excludes_redstone_and_only_accepts_block_dust(GameTestHelper helper) {
        DeepNullInventory inventory = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.IRON);
        inventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.STONEWORKS.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.STONEWORKS));
        inventory.setStackInSlot(0, new ItemStack(Items.REDSTONE, 16));

        helper.assertTrue(inventory.getStoneworksDisplayStack(StoneworksMaterial.DUST).isEmpty(), "Redstone dust must not be treated as the Stoneworks dust output");
        helper.assertFalse(inventory.getVisibleStoneworksMaterials().contains(StoneworksMaterial.DUST), "Stoneworks dust output should stay hidden when only redstone dust is stored");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dock_and_workbench_require_pickaxe_and_break_faster_with_it(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        BlockPos dockPos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos workbenchPos = helper.absolutePos(new BlockPos(3, 1, 1));

        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.DEEP_NULL_DOCK.get());
        DeepNullGameTestSupport.placeWorkbench(helper, new BlockPos(3, 1, 1));

        assertPickaxeBlockBehavior(helper, player, dockPos, ModBlocks.DEEP_NULL_DOCK.get().defaultBlockState(), new ItemStack(ModBlocks.DEEP_NULL_DOCK.get().asItem()));
        assertPickaxeBlockBehavior(helper, player, workbenchPos, helper.getLevel().getBlockState(workbenchPos), new ItemStack(ModBlocks.NULL_WORKBENCH.get().asItem()));
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dock_automation_extracts_default_keep_one_fully_but_respects_explicit_keep_amounts(GameTestHelper helper) {
        BlockPos relativeDockPos = new BlockPos(1, 1, 1);
        BlockPos absoluteDockPos = helper.absolutePos(relativeDockPos);
        helper.setBlock(relativeDockPos, ModBlocks.DEEP_NULL_DOCK.get());
        if (!(helper.getLevel().getBlockEntity(absoluteDockPos) instanceof DeepNullDockBlockEntity dock)) {
            helper.fail("Expected DeepNull Dock block entity at " + relativeDockPos);
            return;
        }

        dock.setStoredDeepNull(DeepNullGameTestSupport.deepNullStack(DeepNullTier.REDSTONE));
        DeepNullInventory defaultInventory = dock.createInventory();
        if (defaultInventory == null) {
            helper.fail("Docked DeepNull inventory was not created");
            return;
        }
        defaultInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 20));

        IItemHandler dockHandler = dock.getAutomationHandler(null);
        ItemStack fullyExtracted = dockHandler.extractItem(0, 64, false);
        helper.assertValueEqual(fullyExtracted.getCount(), 20, "Dock automation should fully extract a default Keep 1 slot");
        helper.assertTrue(dock.createInventory().getStackInSlot(0).isEmpty(), "Dock automation should leave the slot empty after full extraction from a default Keep 1 slot");

        DeepNullInventory limitedInventory = dock.createInventory();
        if (limitedInventory == null) {
            helper.fail("Docked DeepNull inventory was not recreated");
            return;
        }
        limitedInventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 20));
        limitedInventory.setExtractionMode(0, ItemExtractionMode.KEEP_16);

        ItemStack partiallyExtracted = dockHandler.extractItem(0, 64, false);
        helper.assertValueEqual(partiallyExtracted.getCount(), 4, "Dock automation should still respect explicit Keep 16 extraction settings");
        helper.assertValueEqual(dock.createInventory().getStackInSlot(0).getCount(), 16, "Explicit Keep 16 extraction should leave the configured amount behind");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void upgrade_slot_mapping_matches_visible_placeholder_order(GameTestHelper helper) {
        ServerPlayer player = DeepNullGameTestSupport.fakePlayer(helper);
        player.getInventory().setItem(0, DeepNullGameTestSupport.deepNullStack(DeepNullTier.EMERALD));
        player.getInventory().setItem(1, DeepNullGameTestSupport.dampNullStack(DeepNullTier.EMERALD));

        DeepNullMenu deepMenu = DeepNullMenu.forItem(1, player.getInventory(), 0, DeepNullTier.EMERALD, DeepNullMenu.ViewMode.UPGRADES);
        DeepNullMenu dampMenu = DeepNullMenu.forItem(2, player.getInventory(), 1, DeepNullTier.EMERALD, DeepNullMenu.ViewMode.UPGRADES);

        assertUpgradeSlotMapping(helper, deepMenu, "DeepNull");
        assertUpgradeSlotMapping(helper, dampMenu, "DampNull");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void dampnull_sponge_toggle_persists(GameTestHelper helper) {
        DeepNullInventory dampInventory = DeepNullGameTestSupport.dampNullInventory(helper, DeepNullTier.EMERALD);
        dampInventory.getUpgradeHandler().setStackInSlot(DeepNullUpgradeType.SPONGE.slot(), DeepNullGameTestSupport.upgradeStack(DeepNullUpgradeType.SPONGE));

        helper.assertTrue(dampInventory.isSpongeEnabled(), "Sponge should default to enabled when the upgrade is installed");
        dampInventory.setSpongeEnabled(false);

        DeepNullInventory disabledReload = new DeepNullInventory(DeepNullTier.EMERALD, dampInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertFalse(disabledReload.isSpongeEnabled(), "Disabled sponge state should persist");

        disabledReload.setSpongeEnabled(true);
        DeepNullInventory enabledReload = new DeepNullInventory(DeepNullTier.EMERALD, dampInventory.backingStack(), helper.getLevel().registryAccess(), null);
        helper.assertTrue(enabledReload.isSpongeEnabled(), "Re-enabled sponge state should persist");
        helper.succeed();
    }

    @GameTest(template = DeepNullGameTestSupport.EMPTY_TEMPLATE)
    public static void synchronizer_configuration_round_trip(GameTestHelper helper) {
        DeepNullInventory source = DeepNullGameTestSupport.deepNullInventory(helper, DeepNullTier.GOLD);
        source.setAutoPickupEnabled(false);
        source.setStyle(0x0A0B0C, 0x1A1B1C, StyleGlassVariant.PICKAXE);
        CompoundTag configuration = source.exportConfiguration();

        ItemStack synchronizer = new ItemStack(ModItems.SYNCHRONIZER.get());
        SynchronizerItem.storeConfiguration(synchronizer, configuration, source.tier(), source.isFluidOnly());
        helper.assertTrue(SynchronizerItem.hasConfiguration(synchronizer), "Synchronizer should store a copied configuration");
        helper.assertTrue(SynchronizerItem.matchesNullType(synchronizer, false), "Stored synchronizer should match DeepNull type");
        helper.assertFalse(SynchronizerItem.matchesNullType(synchronizer, true), "DeepNull synchronizer data should not match DampNull type");
        helper.assertTrue(SynchronizerItem.getConfiguration(synchronizer) != null, "Stored configuration should be readable");

        SynchronizerItem.clearConfiguration(synchronizer);
        helper.assertFalse(SynchronizerItem.hasConfiguration(synchronizer), "Synchronizer should clear stored configuration");
        helper.succeed();
    }

    private static void assertPickaxeBlockBehavior(GameTestHelper helper, ServerPlayer player, BlockPos pos, BlockState state, ItemStack expectedDrop) {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        float handProgress = state.getDestroyProgress(player, helper.getLevel(), pos);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        float pickaxeProgress = state.getDestroyProgress(player, helper.getLevel(), pos);

        helper.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE), expectedDrop.getHoverName().getString() + " should be tagged as pickaxe-mineable");
        helper.assertTrue(state.requiresCorrectToolForDrops(), expectedDrop.getHoverName().getString() + " should advertise a pickaxe as the proper tool");
        helper.assertTrue(pickaxeProgress > handProgress * 2.0F, expectedDrop.getHoverName().getString() + " should break at least twice as fast with a pickaxe");

        BlockEntity blockEntity = helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(
                Block.getDrops(state, helper.getLevel(), pos, blockEntity).stream().anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, expectedDrop)),
                expectedDrop.getHoverName().getString() + " should drop itself when broken"
        );
    }

    private static void assertUpgradeSlotMapping(GameTestHelper helper, DeepNullMenu menu, String label) {
        for (int index = 0; index < menu.getUpgradeSlotCount(); index++) {
            helper.assertTrue(menu.slots.get(menu.getUpgradeSlotStartIndex() + index) instanceof DeepNullMenu.UpgradeSlot,
                    label + " upgrade slot " + index + " should be an upgrade slot");
            DeepNullMenu.UpgradeSlot slot = (DeepNullMenu.UpgradeSlot) menu.slots.get(menu.getUpgradeSlotStartIndex() + index);
            helper.assertValueEqual(slot.getUpgradeType(), menu.getUpgradeTypeAt(index),
                    label + " upgrade slot " + index + " should map to the same upgrade type as the visible placeholder order");
        }
    }

    private static boolean setGeneratorBuffer(DeepNullDockBlockEntity dock, ItemStack stack) {
        try {
            Field field = DeepNullDockBlockEntity.class.getDeclaredField("generatorBuffer");
            field.setAccessible(true);
            field.set(dock, stack.copy());
            dock.markStoredDeepNullChanged();
            return true;
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }
}
