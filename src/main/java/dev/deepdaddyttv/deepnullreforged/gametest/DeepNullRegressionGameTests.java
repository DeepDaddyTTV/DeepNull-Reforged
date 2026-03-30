package dev.deepdaddyttv.deepnullreforged.gametest;

import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
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
import dev.deepdaddyttv.deepnullreforged.registry.ModBlocks;
import dev.deepdaddyttv.deepnullreforged.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

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
}
